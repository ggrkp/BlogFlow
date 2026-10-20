package com.ggeorg.service;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Post;
import com.ggeorg.domain.PostStatus;
import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.dto.request.post.UpdatePostDTO;
import com.ggeorg.dto.response.PostResponse;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.exception.UnauthorizedAccessException;
import com.ggeorg.repository.PostRepository;
import com.ggeorg.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostService postService;

    private Author author;
    private Tag java;
    private Post post;

    @BeforeEach
    void setUp() {
        author = Author.builder().id(1L).username("test_user").email("test@example.com").build();
        java = new Tag(10L, "java", "Java language", null);
        post = Post.builder()
                .id(5L)
                .title("A post")
                .content("Some content here")
                .status(PostStatus.PUBLISHED)
                .author(author)
                .build();
        post.getTags().add(java);
    }

    @Test
    void getPostResponseById_mapsPostAndAuthor() {
        when(postRepository.findPostWithTagsAndAuthor(5L)).thenReturn(Optional.of(post));

        PostResponse result = postService.getPostResponseById(5L);

        assertEquals(5L, result.getId());
        assertEquals("A post", result.getTitle());
        assertEquals(PostStatus.PUBLISHED, result.getStatus());
        assertEquals("test_user", result.getAuthor().getUsername());
        assertEquals(1, result.getTags().size());
    }

    @Test
    void getPostResponseById_throwsWhenMissing() {
        when(postRepository.findPostWithTagsAndAuthor(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostResponseById(99L));
    }

    @Test
    void getAllPostResponses_mapsEveryPost() {
        when(postRepository.findAllWithTagsAndAuthor()).thenReturn(List.of(post));

        List<PostResponse> result = postService.getAllPostResponses();

        assertEquals(1, result.size());
        assertEquals("A post", result.get(0).getTitle());
    }

    @Test
    void create_savesDraftWithTagsAndGivenAuthor() {
        CreatePostDTO request = CreatePostDTO.builder()
                .title("New post")
                .content("Long enough content")
                .tagIds(Set.of(10L))
                .build();
        when(tagRepository.findAllById(Set.of(10L))).thenReturn(List.of(java));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post saved = postService.create(request, author);

        assertEquals(PostStatus.DRAFT, saved.getStatus());
        assertEquals("New post", saved.getTitle());
        assertEquals(author, saved.getAuthor());
        assertEquals(Set.of(java), saved.getTags());
    }

    @Test
    void updatePost_changesOnlyProvidedFields() {
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        Post result = postService.updatePost(5L, UpdatePostDTO.builder().title("Renamed").build(), author);

        assertEquals("Renamed", result.getTitle());
        assertEquals("Some content here", result.getContent());
        verify(tagRepository, never()).findAllById(any());
    }

    @Test
    void updatePost_replacesTagsWhenIdsProvided() {
        Tag redis = new Tag(11L, "redis", "Cache", null);
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(tagRepository.findAllById(Set.of(11L))).thenReturn(List.of(redis));
        when(postRepository.save(post)).thenReturn(post);

        Post result = postService.updatePost(5L, UpdatePostDTO.builder().tagIds(Set.of(11L)).build(), author);

        assertEquals(new HashSet<>(List.of(redis)), result.getTags());
    }

    @Test
    void updatePost_throwsWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> postService.updatePost(99L, UpdatePostDTO.builder().title("X").build(), author));
        verify(postRepository, never()).save(any());
    }

    @Test
    void deleteById_deletesAndReturnsPost() {
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        Post deleted = postService.deleteById(5L, author);

        assertEquals(5L, deleted.getId());
        verify(postRepository).deleteById(5L);
    }

    @Test
    void deleteById_throwsWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.deleteById(99L, author));
        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void updatePost_rejectsNonOwner() {
        Author other = Author.builder().id(2L).username("someone_else").build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        assertThrows(UnauthorizedAccessException.class,
                () -> postService.updatePost(5L, UpdatePostDTO.builder().title("Hijacked").build(), other));
        assertEquals("A post", post.getTitle());
        verify(postRepository, never()).save(any());
    }

    @Test
    void deleteById_rejectsNonOwner() {
        Author other = Author.builder().id(2L).username("someone_else").build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        assertThrows(UnauthorizedAccessException.class, () -> postService.deleteById(5L, other));
        verify(postRepository, never()).deleteById(any());
    }
}
