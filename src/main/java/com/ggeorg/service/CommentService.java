package com.ggeorg.service;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Comment;
import com.ggeorg.domain.Post;
import com.ggeorg.dto.request.comment.CreateCommentDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.AuthorRepository;
import com.ggeorg.repository.CommentRepository;
import com.ggeorg.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final Long DEFAULT_AUTHOR_ID = 1L;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;


    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, AuthorRepository authorRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.authorRepository = authorRepository;
    }

    public Page<Comment> getCommentsForPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdWithAuthor(postId, pageable);
    }

    public Comment getCommentForPostById(Long postId, Long commentId) {
        return commentRepository.findByIdAndPostIdWithAuthor(postId, commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found for post."));
    }

    public Comment createComment(Long postId, CreateCommentDTO createRequest) {
        Author author = authorRepository.findById(DEFAULT_AUTHOR_ID)
                .orElseThrow(() -> new RuntimeException("Default author not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post for comment creation not found."));
        return commentRepository.save(Comment.builder().content(createRequest.getContent()).isApproved(true).post(post).author(author).build());
    }

}
