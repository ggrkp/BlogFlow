package com.ggeorg.config;

import com.ggeorg.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisTest extends IntegrationTestBase {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void storesAndReadsValue() {
        redisTemplate.opsForValue().set("email", "gingy@test.com");

        assertThat(redisTemplate.opsForValue().get("email")).isEqualTo("gingy@test.com");
    }
}
