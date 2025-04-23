package org.example.dto.player;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.example.Position;

@Data
@Builder
public class PlayerCreate {
    @NotBlank(message = "The name can't be empty")
    private String name;
    @NotBlank(message = "The skill can't be empty")
    private Integer skill;
    @NotBlank(message = "The position can't be empty")
    private Position position;
    @NotBlank(message = "If is titular or not can't be empty")
    private boolean hasPlayed;

}
