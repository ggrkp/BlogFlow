package com.ggeorg.controller;

import com.ggeorg.IntegrationTestBase;
import com.ggeorg.domain.Author;
import com.ggeorg.domain.Post;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.repository.AuthorRepository;
import com.ggeorg.repository.PostRepository;
import com.ggeorg.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser
class PostCacheHttpTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostService postService;

    @Autowired
    private AuthorRepository authorRepository;

    // Spy so we can count how often the database is actually queried.
    @SpyBean
    private PostRepository postRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Long postId;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        authorRepository.deleteAll();
        cacheManager.getCache("posts").clear();

        Author author = authorRepository.save(Author.builder()
                .username("cache-test-author")
                .email("cache-test@example.com")
                .password("not-used")
                .build());

        CreatePostDTO request = new CreatePostDTO();
        request.setTitle("Cached post");
        request.setContent("Served from Redis on the second request");
        Post post = postService.create(request, author);
        postId = post.getId();

        clearInvocations(postRepository);
    }

    @Test
    void cachedResponseIsIdenticalToDatabaseResponse() throws Exception {
        // First request: cache miss, reads from the database
        String fromDatabase = mockMvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Second request: must be served from Redis
        String fromCache = mockMvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(redisTemplate.hasKey("posts::" + postId)).isTrue();
        verify(postRepository, times(1)).findPostWithTagsAndAuthor(postId);
        assertThat(fromCache).isEqualTo(fromDatabase);
    }

    @Test
    void cachedResponseKeepsClientFacingShape() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{id}", postId)).andExpect(status().isOk());

        // Second request comes from the cache; check the JSON the client receives
        mockMvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Cached post"))
                .andExpect(jsonPath("$.author.username").value("cache-test-author"))
                // Dates must be ISO strings, not number arrays
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.updatedAt").isString());

        verify(postRepository, times(1)).findPostWithTagsAndAuthor(postId);
    }
}
