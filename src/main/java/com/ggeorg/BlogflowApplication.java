package com.ggeorg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BlogflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogflowApplication.class, args);
    }
}