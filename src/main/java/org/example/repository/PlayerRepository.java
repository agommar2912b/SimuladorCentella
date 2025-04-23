package org.example.repository;

import org.example.entity.PlayerEntity;
import org.example.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    List<PlayerEntity> findAllByNameAndTeamId(String name, Long teamId);
    List<PlayerEntity> findAllByTeamId(Long teamId);
}
