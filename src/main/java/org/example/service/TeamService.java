package org.example.service;


import lombok.RequiredArgsConstructor;
import org.example.entity.TeamEntity;
import org.example.entity.UserEntity;
import org.example.exceptions.TeamNameExistException;
import org.example.exceptions.TeamNotFoundException;
import org.example.exceptions.UserNotFoundException;
import org.example.repository.TeamRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamEntity patchTeam(Long userId, Long teamId, String name, String profilePictureUrl) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new TeamNotFoundException(userId));

        if (!user.getTeams().contains(team)) {
            throw new TeamNotFoundException(teamId);
        }

        List<TeamEntity> teamWithName = getByName(userId, name).stream()
                .filter(t -> !t.getId().equals(teamId))
                .toList();

        if (!teamWithName.isEmpty()){
            throw new TeamNameExistException(name);
        }
        if (name != null && !name.trim().isEmpty()) {
            team.setName(name);
        }
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) {
            team.setProfilePictureUrl(profilePictureUrl);
        }

        return teamRepository.save(team);
    }

    public TeamEntity createTeam(Long userId, String name, String profilePictureUrl) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<TeamEntity> teamWithName = getByName(userId, name);
        if (teamWithName.isEmpty()) {
            TeamEntity team = new TeamEntity(name);
            team.setProfilePictureUrl(profilePictureUrl);
            team.setUser(user);
            return teamRepository.save(team);
        } else {
            throw new TeamNameExistException(name);
        }


    }

    public TeamEntity deleteTeam(Long teamId, Long userId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.getTeams().contains(team)) {
            throw new TeamNotFoundException(teamId);
        }
        user.getTeams().remove(team);
        team.setUser(null);
        teamRepository.delete(team);
        return team;
    }

    public List<TeamEntity> getByName(Long userId, String name) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return teamRepository.findAllByNameAndUserId(name, userId);
    }

    public List<TeamEntity> getAllTeams(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return teamRepository.findAllByUserId(userId);
    }
}
