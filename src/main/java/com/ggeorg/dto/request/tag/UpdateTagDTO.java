package com.ggeorg.dto.request.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
public class UpdateTagDTO {

    @Size(min = 3, max = 20, message = "Tag name must be between 3 and 10 characters")
    private String name;

    @Size(min = 1, max = 100, message = "Tag description must be between 1 and 100 characters")
    private String description;

}
