package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Game {
    Team teamA;
    Team teamB;
    Random rand = new Random();
    int scoreA = 0;
    int scoreB = 0;
    List<String> events = new ArrayList<>();
    Player mvp;
    boolean firstCalculate = true;

    public Game(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;
    }

    private void calculateWinProbabilities() {
        teamA.setSkillAverage(teamA.calculateAverageSkill());
        teamB.setSkillAverage(teamB.calculateAverageSkill());
        if (firstCalculate) {
            System.out.println(teamA.getName() + ": " + teamA.getSkillAverage());
            System.out.println(teamB.getName() + ": " + teamB.getSkillAverage());
            firstCalculate = false;
        }
        double advantage = (Math.abs(teamA.getSkillAverage() - teamB.getSkillAverage()) * 15);

        teamA.setWinProbability(50 + (teamA.getSkillAverage() > teamB.getSkillAverage() ? advantage : -advantage));
        teamA.setWinProbability(Math.min(Math.max(teamA.getWinProbability(), 10), 90));

        teamB.setWinProbability(100 - teamA.getWinProbability());

        events.add("Probabilidades de victoria: " + teamA.getName() + " " +
            String.format("%.2f", teamA.getWinProbability()) + "%, " +
            teamB.getName() + " " +
            String.format("%.2f", teamB.getWinProbability()) + "%");
    }

    private double calculateWinProbabilities_players(Player atacker, Player defender) {
        return Math.abs(atacker.getSkill() * 5 - defender.getSkill() * 5);
    }

    public void simulate() {
        calculateWinProbabilities();
        for (int minute = 10; minute <= 90; minute = minute + 5) {
            simulateMinute(minute);
        }
        listMvp();
        events.add("Resultado final: " + teamA.getName() + " " + scoreA + " - " + scoreB + " " + teamB.getName());

    }

    private void simulateMinute(int minute) {
        int eventChance = rand.nextInt(100);
        int subChance = rand.nextInt(100);
        Team attackingTeam = selectAttackingTeam();
        Team defendTeam = (attackingTeam == teamA) ? teamB : teamA;

        events.add("Minuto: " + minute);

        if (eventChance < 2) {
            simulateInjury(attackingTeam);
        } else if (eventChance < 70) {
            simulate_Ball_In_Middle(attackingTeam, defendTeam);
        } else if (eventChance < 90) {
            simulateCorner(attackingTeam, defendTeam);
        } else {
            simulateFoul(attackingTeam, defendTeam);
        }
        if (subChance < 40 && attackingTeam.getChangesMade() < 3 && minute > 55) {
            events.add(" ");
            simulateSubstitution(attackingTeam);
        }
    }


    private void simulateInjury(Team injuredTeam) {
        Player injuredPlayer;
        if (rand.nextInt(100) > 10) {
            injuredPlayer = injuredTeam.getPlayers().get(rand.nextInt(injuredTeam.getPlayers().size()));
        } else {
            injuredPlayer = injuredTeam.getGoalie();
        }
        events.add("\t¡Mala noticia para " + injuredTeam.getName() + "! " + injuredPlayer.getName() + " ha sufrido una lesión.");
        if (rand.nextInt(100) > 50) {
            events.add("\t Parece que solo ha sido un golpe pero por seguridad van a realizar el cambio");
        } else {
            events.add("\tParece que si ha sido una lesión fuerte");
        }

        Player substitute = injuredTeam.makeSubstitutionForInjury(injuredPlayer);
        if (substitute != null) {
            events.add("\tEntra " + substitute.getName() + " en lugar de " + injuredPlayer.getName() + " para " + injuredTeam.getName() + ".");
        } else {
            events.add("\t" + injuredTeam.getName() + " no tiene más cambios disponibles. Jugarán con uno menos.");
        }
    }


    private void simulateCorner(Team attackingTeam, Team defenderTeam) {
        Player cornerTaker = attackingTeam.selectCornerTaker(rand);
        Player receiver = attackingTeam.getPlayers().get(rand.nextInt(attackingTeam.getPlayers().size()));

        if (cornerTaker == receiver) {
            events.add("\t" + cornerTaker.getName() + " saca el corner en corto.");
            cornerTaker.setPoints(cornerTaker.getPoints() + 0.2);
            simulateOccasion(attackingTeam, cornerTaker, defenderTeam, receiver);
        } else {
            events.add("\t " + attackingTeam.getName() + " tiene un córner. Lo ejecuta " + cornerTaker.getName() + ".");
            cornerTaker.setPoints(cornerTaker.getPoints() + 0.2);
            receiver.setPoints(receiver.getPoints() + 0.2);
            simulateOccasion(attackingTeam, receiver, defenderTeam, cornerTaker);
        }
    }

    private void simulatePass(Team attackingTeam, Player pasador, Team defenderTeam) {
        Player receiver = attackingTeam.selectPlayerByProbabilityOffensivePass(rand);
        Player defender = defenderTeam.selectPlayerByProbabilityDefenderPass(rand);
        double atc_win_prob, skill_diff;
        skill_diff = calculateWinProbabilities_players(pasador, defender);
        atc_win_prob = 50 + skill_diff;

        if (pasador == receiver) {
            if (rand.nextDouble(100) < atc_win_prob) {
                events.add("\t" + pasador.getName() + " hace un autopase y pasa a" + defender.getName() + " entrando en zona peligrosa.");
                pasador.setPoints(pasador.getPoints() + 1);
                defender.setPoints(defender.getPoints() - 0.25);
                simulateOccasion(attackingTeam, pasador, defenderTeam, pasador);
            } else {
                events.add("\t" + pasador.getName() + " intenta un autopase, pero lo intercepta " + defender.getName() + ".");
                pasador.setPoints(pasador.getPoints() - 0.5);
                defender.setPoints(defender.getPoints() + 1);
            }
        } else {
            if (rand.nextInt(100) < 40) {
                if (rand.nextDouble(100) < atc_win_prob) {
                    events.add("\t" + pasador.getName() + " realiza un pase exitoso a " + receiver.getName() + ".");
                    pasador.setPoints(pasador.getPoints() + 1);
                    receiver.setPoints(receiver.getPoints() + 0.5);
                    defender.setPoints(defender.getPoints() - 0.25);
                    events.add("\t" + receiver.getName() + " recibe el pase y se encuentra en una buena posición.");
                    simulateOccasion(attackingTeam, receiver, defenderTeam, pasador);
                } else {
                    events.add("\t" + pasador.getName() + " intenta un pase, pero lo intercepta " + defender.getName() + ".");
                    pasador.setPoints(pasador.getPoints() - 0.25);
                    defender.setPoints(defender.getPoints() + 1);
                }
            } else {
                events.add("\t" + pasador.getName() + " realiza un pase hacia atras para seguir buscando la oportunidad " + receiver.getName() + ".");
                pasador.setPoints(pasador.getPoints() + 0.5);
                defender.setPoints(defender.getPoints() - 0.25);
                simulate_Ball_In_Middle(attackingTeam, defenderTeam);
            }

        }
    }

    private void simulate_Ball_In_Middle(Team attackingTeam, Team defendingTeam) {
        double atc_win_prob, skill_diff;

        Player attacker = attackingTeam.selectPlayerByProbabilityBallMiddle(rand);
        Player assist = attackingTeam.selectPlayerByProbabilityBallMiddle(rand);
        Player defender = defendingTeam.selectPlayerByProbabilityBallMiddle(rand);

        skill_diff = calculateWinProbabilities_players(attacker, defender);
        atc_win_prob = 50 + skill_diff;
        events.add("\t" + attackingTeam.getName() + " esta moviendo el balon en el centro del campo buscando espacios");

        if (rand.nextDouble(100) < atc_win_prob) {
            if (rand.nextInt(100) < 50) {
                events.add("\t" + attacker.getName() + " quiere dar un pase");
                simulatePass(attackingTeam, attacker, defendingTeam);
            } else {
                events.add("\t" + attacker.getName() + " consigue pasar a " + defender.getName() + " y crea una gran ocasión para su equipo");
                attacker.setPoints(attacker.getPoints() + 1);
                defender.setPoints(defender.getPoints() - 0.25);
                simulateOccasion(attackingTeam, attacker, defendingTeam, assist);
            }
        } else {
            events.add("\t" + defender.getName() + " logra detener a " + attacker.getName() + " e interceptar un gran intento de ataque");
            defender.setPoints(defender.getPoints() + 1);
            attacker.setPoints(attacker.getPoints() - 0.25);

            if (rand.nextInt(100) > 60) {
                events.add("\t" + defendingTeam.getName() + " realiza una contra tras robar el balón");
                simulateCounterAttack(defendingTeam, attackingTeam);
            } else {
                events.add("\t" + defendingTeam.getName() + " mantiene la posesión y busca construir juego.");
            }
        }
    }

    private void simulateCounterAttack(Team counterAttackingTeam, Team defendTeam) {
        double skill_diff, atc_win_prob;
    

        Player attacker = counterAttackingTeam.selectPlayerByProbabilityOffensivePass(rand);
        Player assist = counterAttackingTeam.selectPlayerByProbabilityOffensivePass(rand);
        Player defender = defendTeam.selectPlayerByProbabilityBallMiddle(rand);


        events.add("\t" + attacker.getName() + " lidera el contraataque a gran velocidad.");

        skill_diff = calculateWinProbabilities_players(attacker, defender);
        atc_win_prob = 50 + skill_diff;

        if (rand.nextDouble(100) < atc_win_prob) {
            events.add("\tEl contraataque es exitoso y " + attacker.getName() + " tras pasar a " + defender.getName() + " se encuentra en una gran posición.");
            attacker.setPoints(attacker.getPoints() + 1);
            defender.setPoints(defender.getPoints() - 0.25);
            simulateOccasion(counterAttackingTeam, attacker, defendTeam, assist);
        } else {
            events.add("\tLa defensa logra reagruparse liderada por " + defender.getName() + "y frustra el contraataque de" + attacker.getName() + ".");
            attacker.setPoints(attacker.getPoints() - 0.25);
            defender.setPoints(defender.getPoints() + 1);
        }
    }


    private void simulateOccasion(Team attackingTeam, Player attacker, Team defendTeam, Player assist) {
        double occasion_chance = rand.nextDouble(100);
        Player defender = defendTeam.selectPlayerByProbabilityDefenderPass(rand);
        Player goalkeeper = defendTeam.getGoalie();

        double attackVsDefense = attacker.getSkill() - defender.getSkill();
        double attackVsGoalkeeper = attacker.getSkill() - goalkeeper.getSkill();

        double chanceToBeatDefender = 50 + (attackVsDefense);
        double chanceToBeatGoalkeeper = 40 + (attackVsGoalkeeper);

        if (occasion_chance < chanceToBeatDefender) {
            events.add("\t" + attacker.getName() + " supera a " + defender.getName() + " y avanza hacia la portería.");
            attacker.setPoints(attacker.getPoints() + 1);
            defender.setPoints(defender.getPoints() - 0.25);
            if (occasion_chance < chanceToBeatGoalkeeper) {
                simulateGoal(attackingTeam, attacker, assist);
                goalkeeper.setPoints(goalkeeper.getPoints() - 0.75);
            } else {
                events.add("\t" + goalkeeper.getName() + " realiza una gran parada y evita el gol.");
                goalkeeper.setPoints(goalkeeper.getPoints() + 2.5);
            }
        } else if (occasion_chance < 30) {
            simulatePass(attackingTeam, attacker, defendTeam);
        } else {
            events.add("\t" + defender.getName() + " detiene a " + attacker.getName() + " y frustra el ataque.");
            defender.setPoints(defender.getPoints() + 2);
        }
    }

    private void simulateGoal(Team attackingTeam, Player attacker, Player assist) {
        if (attacker != assist) {
            if (attackingTeam == teamA) scoreA++;
            else scoreB++;
            events.add("\t¡Gol de " + attacker.getName() + " para " + attackingTeam.getName() + "! Asistencia de " + assist.getName() + ".");
            attacker.setPoints(attacker.getPoints() + 3);
            assist.setPoints(assist.getPoints() + 2);
        } else {
            if (attackingTeam == teamA) scoreA++;
            else scoreB++;
            events.add("\t¡Gol de " + attacker.getName() + " para " + attackingTeam.getName() + ".");
            attacker.setPoints(attacker.getPoints() + 3);
        }
    }

    private void simulateFreeKick(Team beenFouledTeam, Team defendFreekickTeam) {
        events.add("\tFalta para " + beenFouledTeam.getName());

        Player freeKickTaker = beenFouledTeam.selectFreeKickTaker(rand);
        Player goalkeeper = defendFreekickTeam.getGoalie();
        double skillDifference = freeKickTaker.getSkill() - goalkeeper.getSkill();
        double chanceToScore = 40 + skillDifference;
        double chanceForGreatOpportunity = 70;

        if (rand.nextDouble(100) < chanceToScore) {
            if (beenFouledTeam == teamA) scoreA++;
            else scoreB++;
            events.add("\t¡Gol de tiro libre de " + freeKickTaker.getName() + " para " + beenFouledTeam.getName() + "!");
            freeKickTaker.setPoints(freeKickTaker.getPoints() + 3);
        } else if (rand.nextInt(100) < chanceForGreatOpportunity) {
            simulatePass(beenFouledTeam, freeKickTaker, defendFreekickTeam);
        } else {
            events.add("\tTiro libre ejecutado, pero " + goalkeeper.getName() + " logra detenerlo.");
            goalkeeper.setPoints(goalkeeper.getPoints() + 1);
        }
    }

    private void simulatePenalty(Team beenFouledTeam, Team defendTeam) {
        Player penaltyTaker = beenFouledTeam.selectPenaltyTaker(rand);
        Player goalkeeper = defendTeam.getGoalie();

        double skillDifference = penaltyTaker.getSkill() - goalkeeper.getSkill();
        double chanceToScore = 70 + skillDifference;

        events.add("\tPenalty para " + beenFouledTeam.getName());
        if (rand.nextDouble(100) < chanceToScore) {
            if (beenFouledTeam == teamA) scoreA++;
            else scoreB++;
            events.add("\t¡Gol de Penalty de " + penaltyTaker.getName() + " para " + beenFouledTeam.getName() + "!");
            penaltyTaker.setPoints(penaltyTaker.getPoints() + 3);
        } else {
            events.add("\tPenalty ejecutado, pero el portero logra detenerlo.");
            penaltyTaker.setPoints(penaltyTaker.getPoints() - 2);
            goalkeeper.setPoints(goalkeeper.getPoints() + 4);

        }
    }

    private void simulateFoul(Team attackingTeam, Team defendTeam) {
        Player beenfoul = attackingTeam.getPlayers().get(rand.nextInt(attackingTeam.getPlayers().size()));
        Player fouler = defendTeam.getPlayers().get(rand.nextInt(defendTeam.getPlayers().size()));
        int random = rand.nextInt(100) + 1;

        if (random < 20) {
            if (!fouler.isHasYellowCard()) {
                events.add("\t " + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");
                events.add("\tTarjeta amarilla para " + fouler.getName() + " de " + defendTeam.getName() + ".");
                fouler.setHasYellowCard(true);
            } else {
                events.add("\t " + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");
                events.add("\tTarjeta roja para " + fouler.getName() + " de " + defendTeam.getName() + " por doble amarilla.");
                fouler.setPoints(fouler.getPoints() - 1.5);
                defendTeam.removePlayer(fouler);
                adjustWinProbabilityAfterExpulsion(defendTeam, fouler);
            }
        } else if (random < 95) {
            events.add("\tFalta realizada por " + fouler.getName() + " de " + defendTeam.getName() + " a " + beenfoul.getName() + ".");
        } else {
            events.add("\t " + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");
            events.add("\tTarjeta roja para " + fouler.getName() + " de " + defendTeam.getName() + ".");
            fouler.setPoints(fouler.getPoints() - 1.5);
            defendTeam.removePlayer(fouler);
            adjustWinProbabilityAfterExpulsion(defendTeam, fouler);
        }

        if (rand.nextInt(100) > 20) {
            simulateFreeKick(attackingTeam, defendTeam);
        } else {
            simulatePenalty(attackingTeam, defendTeam);
        }
    }

    private void adjustWinProbabilityAfterExpulsion(Team foulingTeam, Player expelledPlayer) {
        int totalSkill = foulingTeam.getPlayers().stream().mapToInt(Player::getSkill).sum() + foulingTeam.getGoalie().getSkill();
        int adjustedTotalSkill = totalSkill - expelledPlayer.getSkill();
        foulingTeam.setSkillAverage((double) adjustedTotalSkill / 11);

        events.add("\t¡" + expelledPlayer.getName() + " ha sido expulsado! Se recalcula la media del equipo.");
        events.add("\tNueva media de " + foulingTeam.getName() + ": " + foulingTeam.getSkillAverage());

        calculateWinProbabilities();
    }

    public void simulateSubstitution(Team subsTeam) {
        Player playerOut;
        Player playerIn;
        if (rand.nextInt(100) > 10) {
            List<Player> worstPlayers = subsTeam.getPlayers().stream()
                    .sorted(Comparator.comparingDouble(Player::getPoints))
                    .limit(3)
                    .toList();

            playerOut = worstPlayers.get(rand.nextInt(worstPlayers.size()));

            playerIn = subsTeam.makeSubstitution(playerOut);
        } else {
            playerOut = subsTeam.getGoalie();

            playerIn = subsTeam.makeSubstitution(playerOut);
        }
        subsTeam.setChangesMade(subsTeam.getChangesMade() + 1);
        events.add("\tCambio en " + subsTeam.getName() + ": Sale " + playerOut.getName() + ", entra " + playerIn.getName() + ".");
    }


    private Team selectAttackingTeam() {
        int randomValue = rand.nextInt(100);
        return (randomValue < teamA.getWinProbability()) ? teamA : teamB;
    }

    private void listMvp() {
        List<Player> allPlayers = new ArrayList<>();

        allPlayers.addAll(teamA.getPlayers());
        allPlayers.add(teamA.getGoalie());
        allPlayers.addAll(teamA.getSubstitutes());

        allPlayers.addAll(teamB.getPlayers());
        allPlayers.add(teamB.getGoalie());
        allPlayers.addAll(teamB.getSubstitutes());

        List<Player> mvpPlayer = allPlayers.stream()
                .sorted((p1, p2) -> Double.compare(p2.getPoints(), p1.getPoints()))
                .toList();

        StringBuilder top11 = new StringBuilder("Top 11 jugadores del partido:\n");
        int count = 0;
        for (Player player : mvpPlayer) {
            if (player.isHasPlayed()) {
                if (mvp == null) {
                    mvp = player;
                }
                if (count < 11) {
                    top11.append(String.format("%d. %s (%.2f puntos)\n", count + 1, player.getName(), player.getPoints()));
                    count++;
                }
            }
        }
        events.add(top11.toString().trim());
        if (mvp != null) {
            events.add("MVP del partido: " + mvp.getName() + " (" + String.format("%.2f", mvp.getPoints()) + " puntos)");
        }
    }


    public List<String> showEvents() {
        return events;
    }
}




