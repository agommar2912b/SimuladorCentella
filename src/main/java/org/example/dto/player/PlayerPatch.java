package org.example.dto.player;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.example.Position;
import org.example.anotation.NotBlankOrEmpty;

@Data
@Builder
public class PlayerPatch {
    @NotBlankOrEmpty
    private String name;
    @NotBlankOrEmpty
    private int skill;
    @NotBlankOrEmpty
    private Position position;
    @NotBlankOrEmpty
    private boolean hasPlayed;
}
