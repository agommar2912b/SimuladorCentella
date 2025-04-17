package org.example.dto.team;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private String profilePictureUrl;
}
