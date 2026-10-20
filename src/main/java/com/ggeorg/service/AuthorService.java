package com.ggeorg.service;

import com.ggeorg.config.JwtProvider;
import com.ggeorg.domain.Author;
import com.ggeorg.dto.request.author.LoginDTO;
import com.ggeorg.dto.request.author.RegisterDTO;
import com.ggeorg.dto.response.AuthorResponse;
import com.ggeorg.repository.AuthorRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.authorRepository = authorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public AuthorResponse login(LoginDTO loginRequest) {
        Author author = authorRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), author.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(author);
        return AuthorResponse.builder()
                .id(author.getId())
                .username(author.getUsername())
                .email(author.getEmail())
                .token(token)
                .build();
    }

    public Author register(RegisterDTO registrationRequest) {
        boolean authorExistsByUsername = authorRepository.findByUsername(registrationRequest.getUsername()).isPresent();
        boolean authorExistsByMail = authorRepository.findByEmail(registrationRequest.getEmail()).isPresent();
        if (authorExistsByUsername || authorExistsByMail) throw new EntityExistsException("Registration failed.");

        Author author = Author.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .build();

        return authorRepository.save(author);
    }

    public Author getUserById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));
    }

    public Author getCurrentAuthor() {
        return null;
    }

}
