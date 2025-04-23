package org.example.dto.user;

import lombok.Builder;
import lombok.Data;
import org.example.anotation.NotBlankOrEmpty;

@Data
@Builder
public class UserPatch {
    @NotBlankOrEmpty
    private String name;
    @NotBlankOrEmpty
    private String password;
}
