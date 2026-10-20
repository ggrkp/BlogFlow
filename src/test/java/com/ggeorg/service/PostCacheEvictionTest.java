package com.ggeorg.service;

import com.ggeorg.IntegrationTestBase;
import com.ggeorg.domain.Author;
import com.ggeorg.domain.Post;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.dto.request.post.UpdatePostDTO;
import com.ggeorg.dto.response.PostResponse;
import com.ggeorg.repository.AuthorRepository;
import com.ggeorg.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class PostCacheEvictionTest extends IntegrationTestBase {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Author author;
    private Long postId;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        authorRepository.deleteAll();
        cacheManager.getCache("posts").clear();

        author = authorRepository.save(Author.builder()
                .username("evict-test-author")
                .email("evict-test@example.com")
                .password("not-used")
                .build());

        CreatePostDTO request = new CreatePostDTO();
        request.setTitle("Original title");
        request.setContent("Original content");
        postId = postService.create(request, author).getId();
    }

    @Test
    void updateEvictsCachedPostsAndReturnsFreshData() {
        // Prime both cache entries
        postService.getPostResponseById(postId);
        postService.getAllPostResponses();
        assertThat(redisTemplate.hasKey("posts::" + postId)).isTrue();
        assertThat(redisTemplate.hasKey("posts::all")).isTrue();

        UpdatePostDTO update = new UpdatePostDTO();
        update.setTitle("Updated title");
        postService.updatePost(postId, update, author);

        assertThat(redisTemplate.hasKey("posts::" + postId)).isFalse();
        assertThat(redisTemplate.hasKey("posts::all")).isFalse();
        assertThat(postService.getPostResponseById(postId).getTitle()).isEqualTo("Updated title");
    }

    @Test
    void deleteEvictsCachedPosts() {
        postService.getPostResponseById(postId);
        postService.getAllPostResponses();
        assertThat(redisTemplate.hasKey("posts::" + postId)).isTrue();
        assertThat(redisTemplate.hasKey("posts::all")).isTrue();

        postService.deleteById(postId, author);

        assertThat(redisTemplate.hasKey("posts::" + postId)).isFalse();
        assertThat(redisTemplate.hasKey("posts::all")).isFalse();
        assertThat(postService.getAllPostResponses()).isEmpty();
    }
}
