package com.ggeorg.controller;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Comment;
import com.ggeorg.dto.request.comment.CreateCommentDTO;
import com.ggeorg.dto.request.comment.UpdateCommentDTO;
import com.ggeorg.dto.response.AuthorSummaryDTO;
import com.ggeorg.dto.response.CommentResponseDTO;
import com.ggeorg.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("api/v1/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    @GetMapping
    public ResponseEntity<Page<CommentResponseDTO>> getComments(
            @PathVariable("postId") Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Comment> comments = commentService.getCommentsForPost(postId, PageRequest.of(page, size));
        return ResponseEntity.ok(comments.map(this::toCommentResponseDTO));
    }

    @PostMapping()
    public ResponseEntity<CommentResponseDTO> createComment(@PathVariable("postId") Long postId,
                                                            @Valid @RequestBody CreateCommentDTO request) {
        Comment newComment = commentService.createComment(postId, request);
        URI location = URI.create("/api/v1/posts/" + postId + "/comments/" + newComment.getId());
        return ResponseEntity.created(location).body(toCommentResponseDTO(newComment));
    }

//    @PutMapping("/{commentId}")
//    public ResponseEntity<CommentResponseDTO> updateComment(@PathVariable("postId") Long postId,
//                                                            @PathVariable("commentId") Long commentId,
//                                                            @Valid @RequestBody UpdateCommentDTO request) {
//
//
//    }

    private CommentResponseDTO toCommentResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(toAuthorSummaryDTO(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isApproved(comment.isApproved())
                .build();
    }

    private AuthorSummaryDTO toAuthorSummaryDTO(Author author) {
        return AuthorSummaryDTO.builder()
                .id(author.getId())
                .username(author.getUsername())
                .email(author.getEmail())
                .build();
    }

}