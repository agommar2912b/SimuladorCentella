package org.example.dto.team;

import lombok.Builder;
import lombok.Data;
import org.example.anotation.NotBlankOrEmpty;

@Data
@Builder
public class TeamPatch {
    @NotBlankOrEmpty
    private String name;
    @NotBlankOrEmpty
    private String profilePictureUrl;
}
