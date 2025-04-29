package org.example.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.anotation.NotBlankOrEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserPatch {
    @NotBlankOrEmpty
    private String name;
    @NotBlankOrEmpty
    private String password;
}
