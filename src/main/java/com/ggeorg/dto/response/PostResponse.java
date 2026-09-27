package com.ggeorg.dto.response;

import com.ggeorg.domain.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;

    private String title;

    private String content;

    private PostStatus status;

    private int viewCount;

    private AuthorSummary author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<TagSummary> tags = new HashSet<>();

}

