package com.ggeorg.controller;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Post;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.dto.request.post.UpdatePostDTO;
import com.ggeorg.dto.response.AuthorSummaryDTO;
import com.ggeorg.dto.response.PostResponseDTO;
import com.ggeorg.dto.response.TagDTO;
import com.ggeorg.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/posts")
public class PostController {

    private final PostService postService;


    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody CreatePostDTO request) {
        Post newPost = postService.create(request);
        URI location = URI.create("/api/v1/posts/" + newPost.getId());
        return ResponseEntity.created(location).body(toPostResponseDTO(newPost));
    }

    @GetMapping("{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable("id") Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(toPostResponseDTO(post));
    }

    @GetMapping()
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts.stream().map(this::toPostResponseDTO).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(@PathVariable("id") Long id, @Valid @RequestBody UpdatePostDTO request) {
        Post updatedPost = postService.updatePost(id, request);
        return ResponseEntity.ok(toPostResponseDTO(updatedPost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(@PathVariable("id") Long id) {
        postService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PostResponseDTO toPostResponseDTO(Post post) {
        Author author = post.getAuthor();
        AuthorSummaryDTO authorSummary = AuthorSummaryDTO.builder()
                .id(author.getId())
                .username(author.getUsername())
                .email(author.getEmail())
                .build();

        Set<TagDTO> tagDTOs = post.getTags().stream()
                .map(tag -> TagDTO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .build())
                .collect(Collectors.toSet());

        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .viewCount(0)
                .author(authorSummary)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(tagDTOs)
                .build();
    }

//    @GetMapping
//
//    @GetMapping
//
//    @DeleteMapping
//
//    @PutMapping
}
