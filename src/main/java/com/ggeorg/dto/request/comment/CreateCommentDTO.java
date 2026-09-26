package com.ggeorg.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {

    @Size(min = 1, max = 250, message = "Comment content must be between 1 and 250 characters")
    @NotBlank(message = "Content is required")
    private String content;

}
