package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.player.PlayerCreate;
import org.example.dto.player.PlayerPatch;
import org.example.dto.player.PlayerResponse;
import org.example.entity.PlayerEntity;
import org.example.exceptions.UserNotFoundNameException;
import org.example.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/teams/{teamId}/players")
public class PlayerController {
    private final PlayerService playerService;

    @PatchMapping("/{playerId}")
    public PlayerResponse patchPlayer(@PathVariable Long playerId, @PathVariable Long teamId, @PathVariable Long userId, @Valid @RequestBody PlayerPatch player) {
        PlayerEntity playerPatched = playerService.patchPlayer(userId, teamId, playerId, player.getName(), player.getSkill(), player.getPosition(), player.isHasPlayed());

        return PlayerResponse.builder()
                .id(playerPatched.getId())
                .name(playerPatched.getName())
                .skill(playerPatched.getSkill())
                .position(playerPatched.getPosition())
                .hasPlayed(playerPatched.isHasPlayed())
                .build();

    }

    @PostMapping
    public PlayerResponse createPlayer(@PathVariable Long teamId, @PathVariable Long userId, @Valid @RequestBody PlayerCreate playerCreate) {
        PlayerEntity createdTeam = playerService.createPlayer(userId, teamId, playerCreate.getName(), playerCreate.getSkill(), playerCreate.getPosition(), playerCreate.isHasPlayed());

        return PlayerResponse.builder()
                .id(createdTeam.getId())
                .name(createdTeam.getName())
                .skill(createdTeam.getSkill())
                .position(createdTeam.getPosition())
                .hasPlayed(createdTeam.isHasPlayed())
                .build();

    }

    @DeleteMapping("/{playerId}")
    public PlayerResponse deletePlayer(@PathVariable Long playerId, @PathVariable Long userId, @PathVariable Long teamId) {
        PlayerEntity deletedPlayer = playerService.deletePlayer(teamId, userId, playerId);

        return PlayerResponse.builder()
                .id(deletedPlayer.getId())
                .name(deletedPlayer.getName())
                .skill(deletedPlayer.getSkill())
                .position(deletedPlayer.getPosition())
                .hasPlayed(deletedPlayer.isHasPlayed())
                .build();

    }

    @GetMapping
    public List<PlayerResponse> getPlayers(@PathVariable Long userId, @PathVariable Long teamId, @RequestParam(required = false) String name) {
        List<PlayerEntity> players;

        if (name != null && !name.isBlank()) {
            players = playerService.getByName(name, userId, teamId);
            if (players.isEmpty()) {
                throw new UserNotFoundNameException(name);
            }
        } else {
            players = playerService.getAllPlayers(userId, teamId);
        }
        return players.stream()
                .map(user -> PlayerResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .skill(user.getSkill())
                        .position(user.getPosition())
                        .hasPlayed(user.isHasPlayed())
                        .build())
                .toList();
    }

}
