package com.ggeorg.service;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Comment;
import com.ggeorg.domain.Post;
import com.ggeorg.dto.request.comment.CreateCommentDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.CommentRepository;
import com.ggeorg.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private Author author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = Author.builder().id(1L).username("test_user").build();
        post = Post.builder().id(5L).title("A post").build();
    }

    @Test
    void createComment_savesApprovedCommentOnPostByGivenAuthor() {
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment saved = commentService.createComment(5L, new CreateCommentDTO("Nice post"), author);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertEquals("Nice post", saved.getContent());
        assertTrue(saved.isApproved());
        assertEquals(post, saved.getPost());
        assertEquals(author, saved.getAuthor());
    }

    @Test
    void createComment_throwsWhenPostMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(99L, new CreateCommentDTO("Hi"), author));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentForPostById_returnsCommentOnThatPost() {
        Comment comment = Comment.builder().id(7L).content("Hi").post(post).author(author).build();
        when(commentRepository.findByIdAndPostIdWithAuthor(7L, 5L)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentForPostById(5L, 7L);

        assertEquals(7L, result.getId());
    }

    @Test
    void getCommentForPostById_throwsWhenNotOnThatPost() {
        when(commentRepository.findByIdAndPostIdWithAuthor(7L, 5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentForPostById(5L, 7L));
    }
}
