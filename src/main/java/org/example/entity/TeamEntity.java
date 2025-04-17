package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.Position;

import java.util.List;
import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString

public class TeamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PlayerEntity> players;
    @OneToOne
    private PlayerEntity goalie;
    @OneToMany
    private List<PlayerEntity> substitutes;
    private double skillAverage;
    private double winProbability;
    private int changesMade = 0;
    @OneToMany
    private List<PlayerEntity> freekickKickers;
    @OneToMany
    private List<PlayerEntity> cornerKickers;
    @OneToMany
    private List<PlayerEntity> penaltyKickers;
    private String profilePictureUrl;

    public TeamEntity(String name) {
        this.name = name;
    }

    public double calculateAverageSkill() {
        int total = players.stream().mapToInt(PlayerEntity::getSkill).sum() + goalie.getSkill();
        return (double) total / (players.size() + 1);
    }

    public void removePlayer(PlayerEntity player) {
        players.remove(player);
    }

    public PlayerEntity makeSubstitution(PlayerEntity playerToSub) {
        Random rand = new Random();
        PlayerEntity chosenSub = null;

        List<PlayerEntity> matchingSubs = substitutes.stream()
                .filter(sub -> sub.getPosition() == playerToSub.getPosition() && !players.contains(sub)&&!sub.isHasPlayed())
                .toList();

        List<PlayerEntity> anySubs = substitutes.stream()
                .filter(sub -> !players.contains(sub) && !sub.isHasPlayed() && sub.getPosition()!=Position.GOALKEEPER)
                .toList();

        if (!matchingSubs.isEmpty()) {
            chosenSub = matchingSubs.get(rand.nextInt(matchingSubs.size()));
        } else if (!anySubs.isEmpty()) {
            chosenSub = anySubs.get(rand.nextInt(anySubs.size()));
        }


        if (playerToSub.getPosition()== Position.GOALKEEPER){
            substitutes.remove(chosenSub);
            goalie=chosenSub;
            chosenSub.setHasPlayed(true);
            substitutes.add(playerToSub);
        }else{
            substitutes.remove(chosenSub);
            players.add(chosenSub);
            players.remove(playerToSub);
            substitutes.add(playerToSub);
            chosenSub.setHasPlayed(true);
        }
        return chosenSub;
    }

    public PlayerEntity makeSubstitutionForInjury(PlayerEntity injuredPlayer) {
        PlayerEntity substitute = makeSubstitution(injuredPlayer);
        if (substitute != null) {
            players.remove(injuredPlayer);
            return substitute;
        }
        return null;
    }

}

