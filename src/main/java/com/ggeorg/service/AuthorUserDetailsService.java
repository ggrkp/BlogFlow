package com.ggeorg.service;

import com.ggeorg.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorUserDetailsService implements UserDetailsService {

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorUserDetailsService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Author not found: " + username));
    }
}
