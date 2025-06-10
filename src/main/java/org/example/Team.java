package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

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
        this.players = new ArrayList<>(players); 
        this.goalie = goalie;
        this.substitutes = new ArrayList<>(substitutes); 
        this.skillAverage = calculateAverageSkill();
    }

    public void setPlayers(List<Player> players) {
        this.players = new ArrayList<>(players);
    }

    public void setSubstitutes(List<Player> substitutes) {
        this.substitutes = new ArrayList<>(substitutes);
    }

    public void setFreekickKickers(List<Player> freekickKickers) {
        this.freekickKickers = new ArrayList<>(freekickKickers);
    }

    public void setCornerKickers(List<Player> cornerKickers) {
        this.cornerKickers = new ArrayList<>(cornerKickers);
    }

    public void setPenaltyKickers(List<Player> penaltyKickers) {
        this.penaltyKickers = new ArrayList<>(penaltyKickers);
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

    public List<Player> getCornerKickers() {
        return cornerKickers != null ? cornerKickers : new ArrayList<>();
    }

    public List<Player> getFreekickKickers() {
        return freekickKickers != null ? freekickKickers : new ArrayList<>();
    }

    public List<Player> getPenaltyKickers() {
        return penaltyKickers != null ? penaltyKickers : new ArrayList<>();
    }

    public Player selectFreeKickTaker(Random rand) {
        List<Player> eligible = getFreekickKickers().stream()
            .filter(players::contains)
            .toList();
        if (!eligible.isEmpty()) {
            return eligible.get(rand.nextInt(eligible.size()));
        }
        return players.get(rand.nextInt(players.size()));
    }

    public Player selectPenaltyTaker(Random rand) {
        List<Player> eligible = getPenaltyKickers().stream()
            .filter(players::contains)
            .toList();
        if (!eligible.isEmpty()) {
            return eligible.get(rand.nextInt(eligible.size()));
        }
        return players.get(rand.nextInt(players.size()));
    }

    public Player selectCornerTaker(Random rand) {
        List<Player> eligible = getCornerKickers().stream()
            .filter(players::contains)
            .toList();
        if (!eligible.isEmpty()) {
            return eligible.get(rand.nextInt(eligible.size()));
        }
        return players.get(rand.nextInt(players.size()));
    }


public Player selectPlayerByProbabilityBallMiddle(Random rand) {
    List<Player> midfielders = players.stream()
            .filter(p -> p.getPosition() == Position.MIDFIELDER)
            .toList();
    List<Player> forwardsAndDefenders = players.stream()
            .filter(p -> p.getPosition() == Position.FORWARD || p.getPosition() == Position.DEFENDER)
            .toList();

    int randValue = rand.nextInt(100) + 1;
    if (randValue <= 65 && !midfielders.isEmpty()) {
        return midfielders.get(rand.nextInt(midfielders.size()));
    } else if (randValue <= 95 && !forwardsAndDefenders.isEmpty()) {
        return forwardsAndDefenders.get(rand.nextInt(forwardsAndDefenders.size()));
    } else {
        return goalie;
    }
}

public Player selectPlayerByProbabilityDefenderPass(Random rand) {
    List<Player> defenders = players.stream()
            .filter(p -> p.getPosition() == Position.DEFENDER)
            .toList();
    List<Player> midfielders = players.stream()
            .filter(p -> p.getPosition() == Position.MIDFIELDER)
            .toList();
    List<Player> forwards = players.stream()
            .filter(p -> p.getPosition() == Position.FORWARD)
            .toList();

    int randValue = rand.nextInt(100) + 1;
    if (randValue <= 70 && !defenders.isEmpty()) {
        return defenders.get(rand.nextInt(defenders.size()));
    } else if (randValue <= 90 && !midfielders.isEmpty()) {
        return midfielders.get(rand.nextInt(midfielders.size()));
    } else {
        if (!forwards.isEmpty() && rand.nextBoolean()) {
            return forwards.get(rand.nextInt(forwards.size()));
        } else {
            return goalie;
        }
    }
}

public Player selectPlayerByProbabilityOffensivePass(Random rand) {
    List<Player> forwards = players.stream()
            .filter(p -> p.getPosition() == Position.FORWARD)
            .toList();
    List<Player> midfielders = players.stream()
            .filter(p -> p.getPosition() == Position.MIDFIELDER)
            .toList();
    List<Player> defenders = players.stream()
            .filter(p -> p.getPosition() == Position.DEFENDER)
            .toList();

    int randValue = rand.nextInt(100) + 1;
    if (randValue <= 60 && !forwards.isEmpty()) {
        return forwards.get(rand.nextInt(forwards.size()));
    } else if (randValue <= 85 && !midfielders.isEmpty()) {
        return midfielders.get(rand.nextInt(midfielders.size()));
    } else if (randValue <= 95 && !defenders.isEmpty()) {
        return defenders.get(rand.nextInt(defenders.size()));
    } else {
        return goalie;
    }
}

}

