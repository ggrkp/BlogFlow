package com.ggeorg.service;

import com.ggeorg.domain.Author;
import com.ggeorg.dto.request.CreateAuthorDTO;
import com.ggeorg.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Author create(CreateAuthorDTO request) {
        Author author = Author.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        return authorRepository.save(author);
    }

    public Author getById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
    }

    public Author getByUsername(String username) {
        return authorRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Author not found with username: " + username));
    }
}
