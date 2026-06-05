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

    public Player selectQuickCounterPlayer(Random rand) {
        // Seleccionar jugador para contraataques rápidos priorizando velocidad y habilidad
        List<Player> attackers = players.stream()
                .filter(p -> p.getPosition() == Position.FORWARD || p.getPosition() == Position.MIDFIELDER)
                .sorted((p1, p2) -> Double.compare(p2.getSkill(), p1.getSkill()))
                .toList();
        
        if (!attackers.isEmpty()) {
            return attackers.get(rand.nextInt(Math.min(3, attackers.size())));
        }
        return players.get(rand.nextInt(players.size()));
    }

    public Player selectPressurePlayer(Random rand) {
        // Seleccionar jugador para presionar al rival
        List<Player> pressurePlayers = players.stream()
                .filter(p -> p.getPosition() != Position.GOALKEEPER)
                .sorted((p1, p2) -> Double.compare(p2.getSkill(), p1.getSkill()))
                .toList();
        
        if (!pressurePlayers.isEmpty()) {
            return pressurePlayers.get(rand.nextInt(Math.min(5, pressurePlayers.size())));
        }
        return players.get(rand.nextInt(players.size()));
    }

    public Player selectSetPieceDefender(Random rand) {
        // Seleccionar defensor para jugadas a balón parado
        List<Player> defenders = players.stream()
                .filter(p -> p.getPosition() == Position.DEFENDER)
                .sorted((p1, p2) -> Double.compare(p2.getSkill(), p1.getSkill()))
                .toList();
        
        if (!defenders.isEmpty()) {
            return defenders.get(rand.nextInt(Math.min(4, defenders.size())));
        }
        return players.get(rand.nextInt(players.size()));
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
    List<Player> eligiblePlayers;
    int randValue = rand.nextInt(100) + 1;
    
    if (randValue <= 70) {
        // Mediocampistas tienen más probabilidad en el centro
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.MIDFIELDER)
                .toList();
    } else if (randValue <= 90) {
        // Defensores en la construcción
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.DEFENDER)
                .toList();
    } else {
        // Delanteros bajan a recibir
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.FORWARD)
                .toList();
    }

    if (eligiblePlayers.isEmpty()) {
        return players.get(rand.nextInt(players.size()));
    }

    // Suma total de habilidades para calcular probabilidades
    double totalSkill = eligiblePlayers.stream().mapToInt(Player::getSkill).sum();
    double randomPoint = rand.nextDouble() * totalSkill;
    double currentSum = 0;

    // Selección ponderada por habilidad
    for (Player player : eligiblePlayers) {
        currentSum += player.getSkill();
        if (currentSum >= randomPoint) {
            return player;
        }
    }

    return eligiblePlayers.get(0);
}

public Player selectPlayerByProbabilityDefenderPass(Random rand) {
    List<Player> eligiblePlayers;
    int randValue = rand.nextInt(100) + 1;
    
    if (randValue <= 80) {
        // Los defensores tienen mayor probabilidad en defensa
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.DEFENDER)
                .toList();
    } else if (randValue <= 95) {
        // Los mediocampistas ayudan en la presión
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.MIDFIELDER)
                .toList();
    } else {
        // Algunos delanteros presionan
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.FORWARD)
                .toList();
    }

    if (eligiblePlayers.isEmpty()) {
        return players.get(rand.nextInt(players.size()));
    }

    // Suma total de habilidades para calcular probabilidades
    double totalSkill = eligiblePlayers.stream().mapToInt(Player::getSkill).sum();
    double randomPoint = rand.nextDouble() * totalSkill;
    double currentSum = 0;

    // Selección ponderada por habilidad
    for (Player player : eligiblePlayers) {
        currentSum += player.getSkill();
        if (currentSum >= randomPoint) {
            return player;
        }
    }

    return eligiblePlayers.get(0);
}

public Player selectPlayerByProbabilityOffensivePass(Random rand) {
    List<Player> eligiblePlayers;
    int randValue = rand.nextInt(100) + 1;
    
    if (randValue <= 80) {
        // Los delanteros son principales en ataque
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.FORWARD)
                .toList();
    } else if (randValue <= 95) {
        // Los mediocampistas participan mucho en ataque
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.MIDFIELDER)
                .toList();
    } else {
        // Ocasionalmente los defensas suben al ataque
        eligiblePlayers = players.stream()
                .filter(p -> p.getPosition() == Position.DEFENDER)
                .toList();
    }

    if (eligiblePlayers.isEmpty()) {
        return players.get(rand.nextInt(players.size()));
    }

    // Suma total de habilidades para calcular probabilidades
    double totalSkill = eligiblePlayers.stream().mapToInt(Player::getSkill).sum();
    double randomPoint = rand.nextDouble() * totalSkill;
    double currentSum = 0;

    // Selección ponderada por habilidad
    for (Player player : eligiblePlayers) {
        currentSum += player.getSkill();
        if (currentSum >= randomPoint) {
            return player;
        }
    }

    return eligiblePlayers.get(0);
}

}

