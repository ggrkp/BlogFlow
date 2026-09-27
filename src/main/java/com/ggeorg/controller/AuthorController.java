package com.ggeorg.controller;

import com.ggeorg.domain.Author;
import com.ggeorg.dto.request.author.CreateAuthorDTO;
import com.ggeorg.dto.response.AuthorSummary;
import com.ggeorg.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<AuthorSummary> createAuthor(@Valid @RequestBody CreateAuthorDTO request) {
        Author newAuthor = authorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthorSummaryDTO(newAuthor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorSummary> getAuthor(@PathVariable Long id) {
        Author author = authorService.getById(id);
        return ResponseEntity.ok(toAuthorSummaryDTO(author));
    }

    private AuthorSummary toAuthorSummaryDTO(Author author) {
        return AuthorSummary.builder()
                .id(author.getId())
                .username(author.getUsername())
                .email(author.getEmail())
                .build();
    }
}
