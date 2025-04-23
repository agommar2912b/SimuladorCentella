package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.team.TeamCreate;
import org.example.dto.team.TeamPatch;
import org.example.dto.team.TeamResponse;
import org.example.entity.TeamEntity;
import org.example.exceptions.TeamNameExistException;
import org.example.exceptions.UserNotFoundNameException;
import org.example.service.TeamService;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/teams")
public class TeamController {
    private final TeamService teamService;

    @PatchMapping("/{teamId}")
    public TeamResponse patchTeam(@PathVariable Long teamId,@PathVariable Long userId, @Valid @RequestBody TeamPatch team){
        TeamEntity patchedTeam = teamService.patchTeam(userId, teamId, team.getName() , team.getProfilePictureUrl());

        return TeamResponse.builder()
                .id(patchedTeam.getId())
                .name(patchedTeam.getName())
                .profilePictureUrl(patchedTeam.getProfilePictureUrl())
                .build();

    }

    @PostMapping
    public TeamResponse createTeam(@PathVariable Long userId, @Valid @RequestBody TeamCreate teamCreate){
        TeamEntity createdTeam = teamService.createTeam(userId,teamCreate.getName(),teamCreate.getProfilePictureUrl());

        return TeamResponse.builder()
                .id(createdTeam.getId())
                .name(createdTeam.getName())
                .profilePictureUrl(createdTeam.getProfilePictureUrl())
                .build();

    }

    @DeleteMapping("/{teamId}")
    public TeamResponse deleteTeam(@PathVariable Long userId , @PathVariable Long teamId){
        TeamEntity deletedTeam = teamService.deleteTeam(teamId , userId);

        return TeamResponse.builder()
                .id(deletedTeam.getId())
                .name(deletedTeam.getName())
                .profilePictureUrl(deletedTeam.getProfilePictureUrl())
                .build();

    }

    @GetMapping
    public List<TeamResponse> getTeams(@PathVariable Long userId ,@RequestParam(required = false)String name) {
        List<TeamEntity> teams;

        if (name != null && !name.isBlank()) {
            teams = teamService.getByName(userId,name);
            if (teams.isEmpty()) {
                throw new TeamNameExistException(name);
            }
        } else {
            teams = teamService.getAllTeams(userId);
        }
        return teams.stream()
                .map(user -> TeamResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .build())
                .toList();
    }

}
