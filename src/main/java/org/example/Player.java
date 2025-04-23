package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Player {
    String name;
    int skill;
    boolean hasYellowCard = false;
    Position position;
    double points;
    boolean hasPlayed;

    public Player(String name, int skill, Position position , Boolean hasPlayed) {
        this.name = name;
        this.skill = skill;
        this.position = position;
        this.hasPlayed=hasPlayed;
    }
}

