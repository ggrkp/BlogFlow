package com.ggeorg.controller;

import com.ggeorg.domain.Post;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.dto.request.post.UpdatePostDTO;
import com.ggeorg.dto.response.PostResponse;
import com.ggeorg.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostDTO request) {
        Post newPost = postService.create(request);
        PostResponse response = postService.getPostResponseById(newPost.getId());
        URI location = URI.create("/api/v1/posts/" + newPost.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable("id") Long id) {
        PostResponse response = postService.getPostResponseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> responses = postService.getAllPostResponses();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable("id") Long id, @Valid @RequestBody UpdatePostDTO request) {
        postService.updatePost(id, request);
        PostResponse response = postService.getPostResponseById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostResponse> deletePost(@PathVariable("id") Long id) {
        postService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
