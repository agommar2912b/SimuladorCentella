package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.Position;
import org.example.entity.PlayerEntity;
import org.example.entity.TeamEntity;
import org.example.entity.UserEntity;
import org.example.exceptions.*;
import org.example.repository.PlayerRepository;
import org.example.repository.TeamRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PlayerService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PlayerEntity patchPlayer(Long userId, Long teamId, Long playerId, String name, Integer skill, Position position, Boolean titular) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));


        if (!user.getTeams().contains(team) || !team.getPlayers().contains(player)) {
            throw new PlayerNotFoundException(playerId);
        }

        List<PlayerEntity> playerWithName = getByName(name, userId ,teamId).stream()
                .filter(u->!u.getId().equals(playerId))
                .toList();

        if (!playerWithName.isEmpty()){
            throw new TeamNameExistException(name);
        }

        if (name != null && !name.trim().isEmpty()) {
            player.setName(name);
        }

        if (skill != null) {
            if (skill >= 1 && skill <= 99) {
                player.setSkill(skill);
            } else {
                throw new IllegalArgumentException("Skill must be between 1 and 99.");
            }
        }
        if (position != null) {
            player.setPosition(position);
        }

        if (titular != null) {
            player.setHasPlayed(titular);
        }

        return playerRepository.save(player);
    }


    public PlayerEntity createPlayer(Long userId, Long teamId, String name, int skill, Position position, Boolean titular) {
        System.out.println("Datos recibidos: " + name + ", " + skill + ", " + position + ", " + titular);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        List<PlayerEntity> playerWithName = getByName(name, userId, teamId);
        if (playerWithName.isEmpty()) {
            PlayerEntity player = new PlayerEntity(name, skill, position, titular);
            player.setTeam(team);
            return playerRepository.save(player);
        } else {
            throw new PlayerNameExistExcepcion(name);
        }

    }

    public PlayerEntity deletePlayer(Long teamId, Long userId , Long playerId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        if (!user.getTeams().contains(team) || !team.getPlayers().contains(player)) {
            throw new PlayerNotFoundException(playerId);
        }
        team.getPlayers().remove(player);
        player.setTeam(null);
        playerRepository.delete(player);
        return player;
    }

    public List<PlayerEntity> getByName(String name , Long userId , Long teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return playerRepository.findAllByNameAndTeamId(name, teamId);
    }

    public List<PlayerEntity> getAllPlayers(Long userId , Long teamId) {

        teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return playerRepository.findAllByTeamId(teamId);
    }
}