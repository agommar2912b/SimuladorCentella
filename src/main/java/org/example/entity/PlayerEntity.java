package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.Position;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "player")
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer skill;
    @Enumerated(EnumType.STRING)
    private Position position;
    private boolean hasPlayed;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private TeamEntity team;


    public PlayerEntity(String name, int skill, Position position , Boolean hasPlayed) {
        this.name = name;
        this.skill = skill;
        this.position = position;
        this.hasPlayed=hasPlayed;
    }
}
