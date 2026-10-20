package com.ggeorg.service;

import com.ggeorg.IntegrationTestBase;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.dto.response.TagResponse;
import com.ggeorg.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TagCacheTest extends IntegrationTestBase {

    // Spy so we can count how often the database is actually queried.
    @SpyBean
    private TagRepository tagRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void resetState() {
        tagRepository.deleteAll();
        cacheManager.getCache("tags").clear();
    }

    @Test
    void secondLookupIsServedFromCacheWithoutQueryingDatabase() {
        tagService.create(new CreateTagDTO("Spring", "Spring Framework"));
        clearInvocations(tagRepository);

        TagResponse first = tagService.getByName("Spring");
        TagResponse second = tagService.getByName("Spring");

        assertThat(first.getName()).isEqualTo("Spring");
        assertThat(second.getName()).isEqualTo("Spring");
        verify(tagRepository, times(1)).findByName("Spring");
    }

    @Test
    void cachedEntryIsStoredInRedisWithConfiguredTtl() {
        tagService.create(new CreateTagDTO("Spring", "Spring Framework"));
        tagService.getByName("Spring");

        String key = "tags::Spring";
        assertThat(redisTemplate.hasKey(key)).isTrue();

        // spring.cache.redis.time-to-live is 600000 ms, i.e. 600 seconds
        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttlSeconds).isBetween(1L, 600L);

        // Round-trip through the cache API proves the value deserializes back to a TagResponse
        Cache tags = cacheManager.getCache("tags");
        TagResponse cached = tags.get("Spring", TagResponse.class);
        assertThat(cached).isNotNull();
        assertThat(cached.getName()).isEqualTo("Spring");
    }

    @Test
    void createEvictsCachedTags() {
        tagService.create(new CreateTagDTO("Spring", "Spring Framework"));
        tagService.getByName("Spring");
        assertThat(redisTemplate.hasKey("tags::Spring")).isTrue();

        tagService.create(new CreateTagDTO("Java", "Java Programming"));

        assertThat(redisTemplate.hasKey("tags::Spring")).isFalse();
        clearInvocations(tagRepository);
        tagService.getByName("Spring");
        verify(tagRepository, times(1)).findByName("Spring");
    }

    @Test
    void updateEvictsCachedTagsAndReturnsFreshData() {
        Long id = tagService.create(new CreateTagDTO("Spring", "Spring Framework")).getId();
        tagService.getByName("Spring");
        tagService.getAll();
        assertThat(redisTemplate.hasKey("tags::Spring")).isTrue();
        assertThat(redisTemplate.hasKey("tags::all")).isTrue();

        tagService.updateTag(id, UpdateTagDTO.builder().description("Updated description").build());

        assertThat(redisTemplate.hasKey("tags::Spring")).isFalse();
        assertThat(redisTemplate.hasKey("tags::all")).isFalse();
        assertThat(tagService.getByName("Spring").getDescription()).isEqualTo("Updated description");
    }

    @Test
    void deleteEvictsCachedTags() {
        Long id = tagService.create(new CreateTagDTO("Spring", "Spring Framework")).getId();
        tagService.getByName("Spring");
        tagService.getAll();
        assertThat(redisTemplate.hasKey("tags::Spring")).isTrue();
        assertThat(redisTemplate.hasKey("tags::all")).isTrue();

        tagService.deleteById(id);

        assertThat(redisTemplate.hasKey("tags::Spring")).isFalse();
        assertThat(redisTemplate.hasKey("tags::all")).isFalse();
        assertThat(tagService.getAll()).isEmpty();
    }
}
