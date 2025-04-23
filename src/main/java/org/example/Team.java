package org.example;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Team {
    String name;
    List<Player> players;
    Player goalie;
    List<Player> substitutes;
    double skillAverage;
    double winProbability;
    int changesMade = 0;
    List<Player> freekickKickers;
    List<Player> cornerKickers;
    List<Player> penaltyKickers;

    public Team(String name, List<Player> players, Player goalie, List<Player> substitutes) {
        this.name = name;
        this.players = players;
        this.goalie = goalie;
        this.substitutes = substitutes;
        this.skillAverage = calculateAverageSkill();
    }

    public double calculateAverageSkill() {
        int total = players.stream().mapToInt(p -> p.skill).sum() + goalie.skill;
        return (double) total / (players.size() + 1);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public Player makeSubstitution(Player playerToSub) {
        Random rand = new Random();
        Player chosenSub = null;

        List<Player> matchingSubs = substitutes.stream()
                .filter(sub -> sub.position == playerToSub.position && !players.contains(sub)&&!sub.hasPlayed)
                .toList();

        List<Player> anySubs = substitutes.stream()
                .filter(sub -> !players.contains(sub) && !sub.hasPlayed)
                .toList();

        if (!matchingSubs.isEmpty()) {
            chosenSub = matchingSubs.get(rand.nextInt(matchingSubs.size()));
        } else if (!anySubs.isEmpty()) {
            chosenSub = anySubs.get(rand.nextInt(anySubs.size()));
        }


        if (playerToSub.position==Position.GOALKEEPER){
            substitutes.remove(chosenSub);
            goalie=chosenSub;
            substitutes.add(playerToSub);
        }else{
            substitutes.remove(chosenSub);
            players.add(chosenSub);
            players.remove(playerToSub);
            substitutes.add(playerToSub);
            chosenSub.hasPlayed = true;
        }
        return chosenSub;
    }

    public Player makeSubstitutionForInjury(Player injuredPlayer) {
        Player substitute = makeSubstitution(injuredPlayer);
        if (substitute != null) {
            players.remove(injuredPlayer);
            return substitute;
        }
        return null;
    }

}

