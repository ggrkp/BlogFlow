package com.ggeorg.service;

import com.ggeorg.config.JwtProvider;
import com.ggeorg.domain.Author;
import com.ggeorg.dto.request.author.LoginDTO;
import com.ggeorg.dto.request.author.RegisterDTO;
import com.ggeorg.dto.response.AuthorResponse;
import com.ggeorg.repository.AuthorRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthorService authorService;

    private Author stored;

    @BeforeEach
    void setUp() {
        stored = Author.builder()
                .id(1L)
                .username("test_user")
                .email("test@example.com")
                .password("hashed-secret")
                .build();
    }

    @Test
    void register_storesEncodedPasswordNotPlaintext() {
        RegisterDTO request = new RegisterDTO("new_user", "new@example.com", "secret123");
        when(authorRepository.findByUsername("new_user")).thenReturn(Optional.empty());
        when(authorRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Author saved = authorService.register(request);

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        assertEquals("new_user", captor.getValue().getUsername());
        assertEquals("hashed-secret", captor.getValue().getPassword());
        assertEquals("hashed-secret", saved.getPassword());
    }

    @Test
    void register_rejectsDuplicateUsername() {
        RegisterDTO request = new RegisterDTO("test_user", "other@example.com", "secret123");
        when(authorRepository.findByUsername("test_user")).thenReturn(Optional.of(stored));
        when(authorRepository.findByEmail("other@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityExistsException.class, () -> authorService.register(request));
        verify(authorRepository, never()).save(any());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterDTO request = new RegisterDTO("other_user", "test@example.com", "secret123");
        when(authorRepository.findByUsername("other_user")).thenReturn(Optional.empty());
        when(authorRepository.findByEmail("test@example.com")).thenReturn(Optional.of(stored));

        assertThrows(EntityExistsException.class, () -> authorService.register(request));
        verify(authorRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        when(authorRepository.findByUsername("test_user")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("secret123", "hashed-secret")).thenReturn(true);
        when(jwtProvider.generateToken(stored)).thenReturn("jwt-token");

        AuthorResponse response = authorService.login(new LoginDTO("test_user", "secret123"));

        assertEquals(1L, response.getId());
        assertEquals("test_user", response.getUsername());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_rejectsWrongPassword() {
        when(authorRepository.findByUsername("test_user")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("wrong-pass", "hashed-secret")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authorService.login(new LoginDTO("test_user", "wrong-pass")));
        verify(jwtProvider, never()).generateToken(any());
    }

    @Test
    void login_rejectsUnknownUser() {
        when(authorRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> authorService.login(new LoginDTO("ghost", "secret123")));
    }
}
