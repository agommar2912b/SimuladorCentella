package org.example.repository;

import org.example.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    List<TeamEntity> findAllByNameAndUserId(String name, Long userId);
    List<TeamEntity> findAllByUserId(Long userId);
}