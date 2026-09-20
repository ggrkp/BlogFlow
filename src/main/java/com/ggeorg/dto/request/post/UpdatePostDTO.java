package com.ggeorg.dto.request.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostDTO {

    @Length(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Length(min = 10, max = 50000, message = "Content must be between 10 and 50000 characters")
    private String content;

    private Set<Long> tagIds;
}
