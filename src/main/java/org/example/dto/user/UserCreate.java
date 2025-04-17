package org.example.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreate {
    @NotBlank(message = "The name can't be empty")
    private String name;
    @NotBlank(message = "The image can't be empty")
    private String profilePictureUrl;
}
