package com.ggeorg.dto.request.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTagDTO {

    @NotBlank(message = "Tag name is required")
    @Size(min = 3, max = 20, message = "Tag name must be between 3 and 10 characters")
    private String name;

    @NotBlank(message = "Tag description is required")
    @Size(min = 1, max = 100, message = "Tag description must be between 1 and 100 characters")
    private String description;

}
