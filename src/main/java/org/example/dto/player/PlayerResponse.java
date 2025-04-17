package org.example.dto.player;

import lombok.Builder;
import lombok.Data;
import org.example.Position;

@Data
@Builder
public class PlayerResponse {
    private Long id;
    private String name;
    private int skill;
    private Position position;
    private boolean hasPlayed;
}
