package com.ggeorg.controller;

import com.ggeorg.domain.Author;
import com.ggeorg.dto.request.author.LoginDTO;
import com.ggeorg.dto.request.author.RegisterDTO;
import com.ggeorg.dto.response.AuthorResponse;
import com.ggeorg.dto.response.AuthorSummary;
import com.ggeorg.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthorService authorService;

    @Autowired
    public AuthController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthorSummary> register(@Valid @RequestBody RegisterDTO request) {
        Author newAuthor = authorService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthorSummaryDTO(newAuthor));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthorResponse> login(@Valid @RequestBody LoginDTO request) {
        AuthorResponse response = authorService.login(request);
        return ResponseEntity.ok(response);
    }

    private AuthorSummary toAuthorSummaryDTO(Author author) {
        return AuthorSummary.builder()
                .id(author.getId())
                .username(author.getUsername())
                .email(author.getEmail())
                .build();
    }
}
