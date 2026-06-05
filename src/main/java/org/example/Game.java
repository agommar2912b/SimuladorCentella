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
    List<Player> expelledPlayers = new ArrayList<>();

    private static final int PASS_FOUL = 4;
    private static final int OCCASION_FOUL = 12;
    private static final int CORNER_FOUL = 12;
    private static final int COUNTER_FOUL = 6;
    private static final int TRANSITION_FOUL = 7;
    private static final int HIGH_PRESSURE_FOUL = 12;

    private static final double POINTS_GOAL = 5.0;
    private static final double POINTS_ASSIST = 3.5;
    private static final double POINTS_PASS_SUCCESS = 0.5;
    private static final double POINTS_PASS_FAIL = -0.25;
    private static final double POINTS_AUTOPASE_SUCCESS = 1.0;
    private static final double POINTS_AUTOPASE_FAIL = -0.5;
    private static final double POINTS_RECEIVER_PASS = 0.5;
    private static final double POINTS_PASS_BACK = 0.5;
    private static final double POINTS_GOALKEEPER_SAVE = 3.0;
    private static final double POINTS_GOALKEEPER_SAVE_PENALTY = 6.0;
    private static final double POINTS_PRESSURE_SUCCESS = 1.0;
    private static final double POINTS_PRESSURE_FAIL = -0.5;
    private static final double POINTS_QUICK_TRANSITION_INIT = 1.5;
    private static final double POINTS_QUICK_TRANSITION_DEFENSE = 1.0;
    private static final double POINTS_CORNER_TAKER = 0.2;
    private static final double POINTS_CORNER_SHORT_TAKER = 0.2;
    private static final double POINTS_BALL_MIDDLE_ATTACKER = 1.0;
    private static final double POINTS_BALL_MIDDLE_DEFENDER = 1.0;
    private static final double POINTS_DEFENDER_INTERCEPTION = 1.0;
    private static final double POINTS_BALL_MIDDLE_FAIL = -0.25;
    private static final double POINTS_COUNTER_ATTACKER = 1.0;
    private static final double POINTS_COUNTER_DEFENDER = 1.0;
    private static final double POINTS_OCCASIONAL_BEAT = 1.0;
    private static final double POINTS_OCCASIONAL_DEFENSE = 2.0;
    private static final double POINTS_YELLOW_CARD = -0.25;
    private static final double POINTS_RED_CARD_DIRECT = -2.0;
    private static final double POINTS_RED_CARD_DOUBLE = -1.5;
    private static final double POINTS_PENALTY_FAIL = -2.0;
    private static final double POINTS_OFFSIDE_ATTEMPT = -0.5;

    private static final double POINTS_GOAL_0_GOALKEEPER = 3.0;
    private static final double POINTS_GOAL_0_DEFENDER = 2.0;
    private static final double POINTS_GOAL_0_MIDFIELDER = 1.0;

    private static final double POINTS_MORE_4_GOALS_GOALKEEPER = -2.0;
    private static final double POINTS_MORE_4_GOALS_DEFENDER = -2.0;
    private static final double POINTS_MORE_4_GOALS_MIDFIELDER = -1.0;





    public Game(Team teamA, Team teamB, Boolean copa) {
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
        double homeAdvantage = 0;
        
        double baseProb = 50 + (teamA.getSkillAverage() > teamB.getSkillAverage() ? advantage : -advantage);
        teamA.setWinProbability(baseProb + homeAdvantage);
        teamA.setWinProbability(Math.min(Math.max(teamA.getWinProbability(), 10), 90));
        
        teamB.setWinProbability(100 - teamA.getWinProbability());
        // Aseguramos que el equipo visitante tenga al menos 10% de probabilidad
        teamB.setWinProbability(Math.max(teamB.getWinProbability(), 10));

        events.add("Probabilidades de victoria: " + teamA.getName() + " " +
            String.format("%.2f", teamA.getWinProbability()) + "%, " +
            teamB.getName() + " " +
            String.format("%.2f", teamB.getWinProbability()) + "%");
    }

    private double calculateWinProbabilities_players(Player atacker, Player defender) {
        return Math.abs(atacker.getSkill() * 5 - defender.getSkill() * 5);
    }

    private void expelPlayer(Team team, Player player) {
        team.removePlayer(player);
        expelledPlayers.add(player);
    }

    public void simulate(Boolean copa) {
        calculateWinProbabilities();

        if (copa) {
            // Partido hasta el minuto 90
            for (int minute = 10; minute <= 90; minute = minute + 5) {
                simulateMinute(minute);
            }
            // Si hay empate, prórroga
            if (scoreA == scoreB) {
                events.add("\n--- Prórroga ---");
                for (int minute = 95; minute <= 120; minute = minute + 5) {
                    simulateMinute(minute);
                }
            }
            // Si sigue empate, penaltis
            if (scoreA == scoreB) {
                events.add("\n--- Penaltis ---");
                int penalesA = 0;
                int penalesB = 0;
                for (int i = 0; i < 5; i++) {
                    if (simulatePenaltyShootout(teamA, teamB, i)) penalesA++;
                    if (simulatePenaltyShootout(teamB, teamA, i)) penalesB++;
                    events.add("Ronda " + (i+1) + ": " + teamA.getName() + " " + penalesA + " - " + penalesB + " " + teamB.getName());
                }
                // Muerte súbita si sigue empate
                int ronda = 6;
                int lanzadorIndex = 5;
                while (penalesA == penalesB) {
                    boolean golA = simulatePenaltyShootout(teamA, teamB, lanzadorIndex);
                    boolean golB = simulatePenaltyShootout(teamB, teamA, lanzadorIndex);
                    if (golA != golB) {
                        if (golA) penalesA++;
                        if (golB) penalesB++;
                        events.add("Ronda " + ronda + " (muerte súbita): " + teamA.getName() + " " + penalesA + " - " + penalesB + " " + teamB.getName());
                        break;
                    }
                    ronda++;
                    lanzadorIndex++;
                }
                events.add("Penaltis final: " + teamA.getName() + " " + penalesA + " - " + penalesB + " " + teamB.getName());
            }
            // Apply end game points
            if (scoreB == 0) { // teamA clean sheet
                teamA.getGoalie().setPoints(teamA.getGoalie().getPoints() + POINTS_GOAL_0_GOALKEEPER);
                for (Player p : teamA.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_MIDFIELDER);
                    }
                }
            }
            if (scoreA == 0) { // teamB clean sheet
                teamB.getGoalie().setPoints(teamB.getGoalie().getPoints() + POINTS_GOAL_0_GOALKEEPER);
                for (Player p : teamB.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_MIDFIELDER);
                    }
                }
            }
            if (scoreB >= 4) { // teamA conceded more than 4
                teamA.getGoalie().setPoints(teamA.getGoalie().getPoints() + POINTS_MORE_4_GOALS_GOALKEEPER);
                for (Player p : teamA.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_MIDFIELDER);
                    }
                }
            }
            if (scoreA >= 4) { // teamB conceded more than 4
                teamB.getGoalie().setPoints(teamB.getGoalie().getPoints() + POINTS_MORE_4_GOALS_GOALKEEPER);
                for (Player p : teamB.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_MIDFIELDER);
                    }
                }
            }
            listMvp();
            events.add("Resultado final: " + teamA.getName() + " " + scoreA + " - " + scoreB + " " + teamB.getName());
        } else {
            for (int minute = 10; minute <= 90; minute = minute + 5) {
                simulateMinute(minute);
            }
            // Apply end game points
            if (scoreB == 0) { // teamA clean sheet
                teamA.getGoalie().setPoints(teamA.getGoalie().getPoints() + POINTS_GOAL_0_GOALKEEPER);
                for (Player p : teamA.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_MIDFIELDER);
                    }
                }
            }
            if (scoreA == 0) { // teamB clean sheet
                teamB.getGoalie().setPoints(teamB.getGoalie().getPoints() + POINTS_GOAL_0_GOALKEEPER);
                for (Player p : teamB.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_GOAL_0_MIDFIELDER);
                    }
                }
            }
            if (scoreB >= 4) { // teamA conceded more than 4
                teamA.getGoalie().setPoints(teamA.getGoalie().getPoints() + POINTS_MORE_4_GOALS_GOALKEEPER);
                for (Player p : teamA.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_MIDFIELDER);
                    }
                }
            }
            if (scoreA >= 4) { // teamB conceded more than 4
                teamB.getGoalie().setPoints(teamB.getGoalie().getPoints() + POINTS_MORE_4_GOALS_GOALKEEPER);
                for (Player p : teamB.getPlayers()) {
                    if (p.getPosition() == Position.DEFENDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_DEFENDER);
                    } else if (p.getPosition() == Position.MIDFIELDER) {
                        p.setPoints(p.getPoints() + POINTS_MORE_4_GOALS_MIDFIELDER);
                    }
                }
            }
            listMvp();
            events.add("Resultado final: " + teamA.getName() + " " + scoreA + " - " + scoreB + " " + teamB.getName());
        }
    }

    // Simulación de penaltis para copa
    private boolean simulatePenaltyShootout(Team atacking, Team defending, int lanzadorIndex) {
        // Obtener titulares ordenados por habilidad descendente
        List<Player> titulares = atacking.getPlayers().stream()
            .filter(Player::isHasPlayed)
            .sorted((a, b) -> Integer.compare(b.getSkill(), a.getSkill()))
            .toList();

        // Si hay menos de 1 titular, usar el portero
        Player penaltyTaker = titulares.size() > lanzadorIndex ? titulares.get(lanzadorIndex) : atacking.getGoalie();
        Player goalkeeper = defending.getGoalie();
        double skillDifference = penaltyTaker.getSkill() - goalkeeper.getSkill();
        double chanceToScore = 70 + skillDifference;
        boolean gol = rand.nextDouble(100) < chanceToScore;
        String resultado = gol ? "¡Gol!" : "Falla";
        events.add("\tPenalti: " + penaltyTaker.getName() + " (" + atacking.getName() + ") vs " + goalkeeper.getName() + " (" + defending.getName() + ") → " + resultado);
        if (gol) {
            penaltyTaker.setPoints(penaltyTaker.getPoints() + 1);
        } else {
            goalkeeper.setPoints(goalkeeper.getPoints() + 2);
            penaltyTaker.setPoints(penaltyTaker.getPoints() - 2);
        }
        return gol;
    }

    private void simulateMinute(int minute) {
        int eventChance = rand.nextInt(100);
        int subChance = rand.nextInt(100);
        Team attackingTeam = selectAttackingTeam();
        Team defendTeam = (attackingTeam == teamA) ? teamB : teamA;

        events.add("Minuto: " + minute);

        // Más variedad de situaciones de juego
        if (eventChance < 65) {
            simulate_Ball_In_Middle(attackingTeam, defendTeam);
        } else if (eventChance < 75) {
            simulateHighPressure(attackingTeam, defendTeam);
        } else if (eventChance < 80) {
            simulateQuickTransition(attackingTeam, defendTeam);
        } else if (eventChance < 95) {
            simulateCorner(attackingTeam, defendTeam);
        } else {
            simulateFoul(attackingTeam, defendTeam);
        }

        // Gestión de sustituciones
        if (subChance < 40 && attackingTeam.getChangesMade() < 3 && minute > 55) {
            events.add(" ");
            simulateSubstitution(attackingTeam);
        }
    }

    private void simulateHighPressure(Team pressingTeam, Team possessionTeam) {
        Player pressingPlayer = pressingTeam.selectPressurePlayer(rand);
        Player pressuredPlayer = possessionTeam.selectPlayerByProbabilityBallMiddle(rand);
        
        events.add("\t" + pressingTeam.getName() + " realiza una presión alta.");
        events.add("\t" + pressingPlayer.getName() + " presiona intensamente a " + pressuredPlayer.getName());

        double pressureSuccess = (pressingPlayer.getSkill() - pressuredPlayer.getSkill() + 50);
        
        if (rand.nextDouble(100) < pressureSuccess) {
            events.add("\t¡La presión tiene éxito! " + pressingPlayer.getName() + " roba el balón.");
            pressingPlayer.setPoints(pressingPlayer.getPoints() + POINTS_PRESSURE_SUCCESS);
            pressuredPlayer.setPoints(pressuredPlayer.getPoints() + POINTS_PRESSURE_FAIL);
            // Tras la recuperación puede producirse una falta por entrada
            if (rand.nextInt(100) < HIGH_PRESSURE_FOUL) {
                Player fouler = possessionTeam.getPlayers().get(rand.nextInt(possessionTeam.getPlayers().size()));
                simulateFoulWithContext(pressingTeam, possessionTeam, false, pressingPlayer, fouler, "presión alta");
                return;
            }
            simulateQuickTransition(pressingTeam, possessionTeam);
        } else {
            events.add("\t" + pressuredPlayer.getName() + " mantiene la posesión y supera la presión.");
            pressuredPlayer.setPoints(pressuredPlayer.getPoints() + POINTS_PRESSURE_FAIL);
            simulate_Ball_In_Middle(possessionTeam, pressingTeam);
        }
    }

    private void simulateQuickTransition(Team attackingTeam, Team defendingTeam) {
        Player transitionPlayer = attackingTeam.selectQuickCounterPlayer(rand);
        Player defender = defendingTeam.selectPlayerByProbabilityDefenderPass(rand);

        events.add("\t¡" + attackingTeam.getName() + " inicia una transición rápida!");
        events.add("\t" + transitionPlayer.getName() + " conduce el balón a gran velocidad.");

        double transitionSuccess = (transitionPlayer.getSkill() - defender.getSkill() + 55);

        if (rand.nextDouble(100) < transitionSuccess) {
            events.add("\t" + transitionPlayer.getName() + " encuentra espacio y genera una ocasión peligrosa.");
            transitionPlayer.setPoints(transitionPlayer.getPoints() + POINTS_QUICK_TRANSITION_INIT);
            // Después de iniciar la transición rápida puede haber una falta
            if (rand.nextInt(100) < TRANSITION_FOUL) {
                Player fouler = defendingTeam.getPlayers().get(rand.nextInt(defendingTeam.getPlayers().size()));
                simulateFoulWithContext(attackingTeam, defendingTeam, false, transitionPlayer, fouler, "transición rápida");
                return;
            }
            simulateOccasion(attackingTeam, transitionPlayer, defendingTeam, transitionPlayer);
        } else {
            events.add("\t" + defender.getName() + " realiza una gran cobertura y detiene la transición.");
            defender.setPoints(defender.getPoints() + POINTS_QUICK_TRANSITION_DEFENSE);
            if (rand.nextInt(100) > 75) {
                events.add("\t" + defendingTeam.getName() + " realiza una contra tras robar el balón");
                simulateCounterAttack(defendingTeam, attackingTeam);
            } else {
                events.add("\t" + defendingTeam.getName() + " mantiene la posesión y busca construir juego.");
            }
        }
    }

    private void simulateInjuryPlayer(Team injuredTeam , Player injuredPlayer) {
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
            cornerTaker.setPoints(cornerTaker.getPoints() + POINTS_CORNER_SHORT_TAKER);
            // Tras el saque en corto, revisar si hay falta en el área
            if (rand.nextInt(100) < CORNER_FOUL) {
                Player fouler = defenderTeam.getPlayers().get(rand.nextInt(defenderTeam.getPlayers().size()));
                simulateFoulWithContext(attackingTeam, defenderTeam, true, receiver, fouler, "área");
                return;
            }
            simulateOccasion(attackingTeam, cornerTaker, defenderTeam, receiver);
        } else {
            events.add("\t " + attackingTeam.getName() + " tiene un córner. Lo ejecuta " + cornerTaker.getName() + ".");
            cornerTaker.setPoints(cornerTaker.getPoints() + POINTS_CORNER_TAKER);
            receiver.setPoints(receiver.getPoints() + POINTS_CORNER_TAKER);
            // Tras el centro, revisar si hay falta en el área (posible penalty)
            if (rand.nextInt(100) < CORNER_FOUL) {
                Player fouler = defenderTeam.getPlayers().get(rand.nextInt(defenderTeam.getPlayers().size()));
                simulateFoulWithContext(attackingTeam, defenderTeam, true, receiver, fouler, "área");
                return;
            }
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
                events.add("\t" + pasador.getName() + " hace un autopase y pasa a " + defender.getName() + " entrando en zona peligrosa.");
                pasador.setPoints(pasador.getPoints() + POINTS_AUTOPASE_SUCCESS);
                defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_FAIL);
                // Después del autopase, puede producirse una falta en la jugada
                if (rand.nextInt(100) < PASS_FOUL) {
                    simulateFoulWithContext(attackingTeam, defenderTeam, false, pasador, defender, "zona de ataque");
                    return;
                }
                simulateOccasion(attackingTeam, pasador, defenderTeam, pasador);
            } else {
                events.add("\t" + pasador.getName() + " intenta un autopase, pero lo intercepta " + defender.getName() + ".");
                pasador.setPoints(pasador.getPoints() + POINTS_AUTOPASE_FAIL);
                defender.setPoints(defender.getPoints() + POINTS_DEFENDER_INTERCEPTION);
            }
        } else {
            if (rand.nextInt(100) < 40) {
                if (rand.nextDouble(100) < atc_win_prob) {
                    events.add("\t" + pasador.getName() + " realiza un pase exitoso a " + receiver.getName() + ".");
                    pasador.setPoints(pasador.getPoints() + POINTS_PASS_SUCCESS);
                    receiver.setPoints(receiver.getPoints() + POINTS_RECEIVER_PASS);
                    defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_FAIL);
                    events.add("\t" + receiver.getName() + " recibe el pase y se encuentra en una buena posición.");
                    // Después del pase exitoso, puede haber falta del defensor sobre el receptor
                    if (rand.nextInt(100) < PASS_FOUL) {
                        simulateFoulWithContext(attackingTeam, defenderTeam, false, receiver, defender, "zona ofensiva");
                        return;
                    }
                    simulateOccasion(attackingTeam, receiver, defenderTeam, pasador);
                } else {
                    events.add("\t" + pasador.getName() + " intenta un pase, pero lo intercepta " + defender.getName() + ".");
                    pasador.setPoints(pasador.getPoints() + POINTS_PASS_FAIL);
                    defender.setPoints(defender.getPoints() + POINTS_DEFENDER_INTERCEPTION);
                }
            } else {
                events.add("\t" + pasador.getName() + " realiza un pase hacia atras para seguir buscando la oportunidad " + receiver.getName() + ".");
                pasador.setPoints(pasador.getPoints() + POINTS_PASS_BACK);
                defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_FAIL);
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
                attacker.setPoints(attacker.getPoints() + POINTS_BALL_MIDDLE_ATTACKER);
                defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_FAIL);
                // Tras crear la ocasión, existe una probabilidad de falta por entrada
                if (rand.nextInt(100) < OCCASION_FOUL) {
                    Player fouler = defendingTeam.getPlayers().get(rand.nextInt(defendingTeam.getPlayers().size()));
                    // Si el atacante es delantero y está cerca de portería, preferimos penalty
                    boolean preferPen = attacker.getPosition() == Position.FORWARD;
                    simulateFoulWithContext(attackingTeam, defendingTeam, preferPen, attacker, fouler, "zona de peligro");
                    return;
                }
                simulateOccasion(attackingTeam, attacker, defendingTeam, assist);
            }
        } else {
            events.add("\t" + defender.getName() + " logra detener a " + attacker.getName() + " e interceptar un gran intento de ataque");
            defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_DEFENDER);
            attacker.setPoints(attacker.getPoints() + POINTS_BALL_MIDDLE_FAIL);

            if (rand.nextInt(100) > 60) {
                events.add("\t" + defendingTeam.getName() + " realiza una contra tras robar el balón");
                // Después de la recuperación, puede haber falta del defensor al rematar
                if (rand.nextInt(100) < PASS_FOUL) {
                    Player fouler = attackingTeam.getPlayers().get(rand.nextInt(attackingTeam.getPlayers().size()));
                    simulateFoulWithContext(defendingTeam, attackingTeam, false, defender, fouler, "centro del campo");
                    return;
                }
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
            attacker.setPoints(attacker.getPoints() + POINTS_COUNTER_ATTACKER);
            defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_FAIL);
            // Tras el inicio del contraataque, comprobar si hay falta
            if (rand.nextInt(100) < COUNTER_FOUL) {
                Player fouler = defendTeam.getPlayers().get(rand.nextInt(defendTeam.getPlayers().size()));
                simulateFoulWithContext(counterAttackingTeam, defendTeam, false, attacker, fouler, "contraataque");
                return;
            }
            simulateOccasion(counterAttackingTeam, attacker, defendTeam, assist);
        } else {
            events.add("\tLa defensa logra reagruparse liderada por " + defender.getName() + " y frustra el contraataque de" + attacker.getName() + ".");
            attacker.setPoints(attacker.getPoints() + POINTS_BALL_MIDDLE_FAIL);
            defender.setPoints(defender.getPoints() + POINTS_COUNTER_DEFENDER);
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
            attacker.setPoints(attacker.getPoints() + POINTS_OCCASIONAL_BEAT);
            defender.setPoints(defender.getPoints() + POINTS_BALL_MIDDLE_FAIL);
            // Tras superar al defensor hay posibilidad de falta (entrada dentro o fuera del área)
            if (rand.nextInt(100) < OCCASION_FOUL) {
                // si es delantero y tiene ventaja contra el portero, es más probable que la falta sea en el área
                boolean preferPen = attacker.getPosition() == Position.FORWARD && attackVsGoalkeeper > 0;
                Player fouler = defendTeam.getPlayers().get(rand.nextInt(defendTeam.getPlayers().size()));
                simulateFoulWithContext(attackingTeam, defendTeam, preferPen, attacker, fouler, preferPen ? "área" : "zona de ataque");
                return;
            }
            if (occasion_chance < chanceToBeatGoalkeeper) {
                simulateGoal(attackingTeam, attacker, assist);
            } else {
                events.add("\t" + goalkeeper.getName() + " realiza una gran parada y evita el gol.");
                goalkeeper.setPoints(goalkeeper.getPoints() + POINTS_GOALKEEPER_SAVE);
            }
        } else if (occasion_chance < 30) {
            simulatePass(attackingTeam, attacker, defendTeam);
        } else {
            events.add("\t" + defender.getName() + " detiene a " + attacker.getName() + " y frustra el ataque.");
            defender.setPoints(defender.getPoints() + POINTS_OCCASIONAL_DEFENSE);
        }
    }

    private void simulateGoal(Team attackingTeam, Player attacker, Player assist) {
        // Comprobamos si hay fuera de juego (10% de probabilidad)
        if (rand.nextDouble(100) < 10) {
            events.add("\t¡El árbitro señala fuera de juego! Se anula el gol de " + attacker.getName() + ".");
            attacker.setPoints(attacker.getPoints() + POINTS_OFFSIDE_ATTEMPT);
            return;
        }
        
        if (attacker != assist) {
            if (attackingTeam == teamA) scoreA++;
            else scoreB++;
            events.add("\t¡Gol de " + attacker.getName() + " para " + attackingTeam.getName() + "! Asistencia de " + assist.getName() + ".");
            attacker.setPoints(attacker.getPoints() + POINTS_GOAL);
            assist.setPoints(assist.getPoints() + POINTS_ASSIST);
        } else {
            if (attackingTeam == teamA) scoreA++;
            else scoreB++;
            events.add("\t¡Gol de " + attacker.getName() + " para " + attackingTeam.getName() + ".");
            attacker.setPoints(attacker.getPoints() + POINTS_GOAL);
        }
    }

    private void simulateFreeKick(Team beenFouledTeam, Team defendFreekickTeam) {
        events.add("\tFalta para " + beenFouledTeam.getName());

        Player freeKickTaker = beenFouledTeam.selectFreeKickTaker(rand);
        double chanceForGreatOpportunity = 40;

        if (rand.nextInt(100) < chanceForGreatOpportunity) {
            simulateOccasion(beenFouledTeam, freeKickTaker, defendFreekickTeam, freeKickTaker);
        } else {
            simulatePass(beenFouledTeam, freeKickTaker, defendFreekickTeam);
        }
    }

    private void simulateFreeKickDanger(Team beenFouledTeam, Team defendFreekickTeam) {
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
            freeKickTaker.setPoints(freeKickTaker.getPoints() + POINTS_GOAL);
        } else if (rand.nextInt(100) < chanceForGreatOpportunity) {
            simulateOccasion(beenFouledTeam, freeKickTaker, defendFreekickTeam, freeKickTaker);
        } else {
            events.add("\tTiro libre ejecutado, pero " + goalkeeper.getName() + " logra detenerlo.");
            goalkeeper.setPoints(goalkeeper.getPoints() + POINTS_GOALKEEPER_SAVE);
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
            penaltyTaker.setPoints(penaltyTaker.getPoints() + POINTS_GOAL);
        } else {
            events.add("\tPenalty ejecutado por " + penaltyTaker.getName() + ", pero el portero logra detenerlo.");
            penaltyTaker.setPoints(penaltyTaker.getPoints() + POINTS_PENALTY_FAIL);
            goalkeeper.setPoints(goalkeeper.getPoints() + POINTS_GOALKEEPER_SAVE_PENALTY);
        }
    }

    private void simulateFoul(Team attackingTeam, Team defendTeam) {
        Player beenfoul = attackingTeam.getPlayers().get(rand.nextInt(attackingTeam.getPlayers().size()));
        Player fouler = defendTeam.getPlayers().get(rand.nextInt(defendTeam.getPlayers().size()));
        int random = rand.nextInt(100) + 1;

        // 5% de probabilidad de tangana
        if (random < 5) {
            simulateTangana(attackingTeam, defendTeam, beenfoul, fouler);
        } 
        // 55% de probabilidad de tarjeta amarilla
        else if (random < 55) {
            if (!fouler.isHasYellowCard()) {
                events.add("\t" + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");
                events.add("\tTarjeta amarilla para " + fouler.getName() + " de " + defendTeam.getName() + ".");
                fouler.setHasYellowCard(true);
                fouler.setPoints(fouler.getPoints() + POINTS_YELLOW_CARD);
            } else {
                events.add("\t" + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");
                events.add("\tTarjeta roja para " + fouler.getName() + " de " + defendTeam.getName() + " por doble amarilla.");
                fouler.setPoints(fouler.getPoints() + POINTS_RED_CARD_DOUBLE);
                expelPlayer(defendTeam, fouler);
                adjustWinProbabilityAfterExpulsion(defendTeam, fouler);
            }
        } else if (random < 95) {
            events.add("\tFalta realizada por " + fouler.getName() + " de " + defendTeam.getName() + " a " + beenfoul.getName() + ".");
        } else {
            events.add("\t" + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");
            events.add("\tTarjeta roja directa para " + fouler.getName() + " de " + defendTeam.getName() + ".");
            fouler.setPoints(fouler.getPoints() + POINTS_RED_CARD_DIRECT);
            expelPlayer(defendTeam, fouler);
            adjustWinProbabilityAfterExpulsion(defendTeam, fouler);
        }

        if (random < 10) {
            events.add("\tEl árbitro detiene el juego para atender al jugador que recibio la falta.");
            simulateInjuryPlayer(attackingTeam , beenfoul);
        }

        if (rand.nextInt(100) > 15) {
            if (rand.nextInt(100) > 50) {
                simulateFreeKickDanger(attackingTeam, defendTeam);
            }else{
                simulateFreeKick(attackingTeam, defendTeam);
            }
        } else {
            simulatePenalty(attackingTeam, defendTeam);
        }
    }

    // Versión con contexto: permite indicar si la falta es dentro del área (preferir penalty),
    // jugadores involucrados y una cadena de localización para mensajes informativos.
    private void simulateFoulWithContext(Team attackingTeam, Team defendTeam, boolean preferPenalty, Player beenfoulParam, Player foulerParam, String location) {
        Player beenfoul = beenfoulParam != null ? beenfoulParam : attackingTeam.getPlayers().get(rand.nextInt(attackingTeam.getPlayers().size()));
        Player fouler = foulerParam != null ? foulerParam : defendTeam.getPlayers().get(rand.nextInt(defendTeam.getPlayers().size()));
        int random = rand.nextInt(100) + 1;

        events.add("\tFalta en " + (location != null ? location : "el campo") + ": " + beenfoul.getName() + " recibe una falta de " + fouler.getName() + ".");

        // 5% de probabilidad de tangana
        if (random < 5) {
            simulateTangana(attackingTeam, defendTeam, beenfoul, fouler);
            return;
        }

        // 55% de probabilidad de tarjeta amarilla
        if (random < 55) {
            if (!fouler.isHasYellowCard()) {
                events.add("\tTarjeta amarilla para " + fouler.getName() + " de " + defendTeam.getName() + ".");
                fouler.setHasYellowCard(true);
                fouler.setPoints(fouler.getPoints() + POINTS_YELLOW_CARD);
            } else {
                events.add("\tTarjeta roja para " + fouler.getName() + " de " + defendTeam.getName() + " por doble amarilla.");
                fouler.setPoints(fouler.getPoints() + POINTS_RED_CARD_DOUBLE);
                expelPlayer(defendTeam, fouler);
                adjustWinProbabilityAfterExpulsion(defendTeam, fouler);
            }
        } else if (random < 95) {
            events.add("\tFalta cometida por " + fouler.getName() + " de " + defendTeam.getName() + " a " + beenfoul.getName() + ".");
        } else {
            events.add("\tTarjeta roja directa para " + fouler.getName() + " de " + defendTeam.getName() + ".");
            fouler.setPoints(fouler.getPoints() + POINTS_RED_CARD_DIRECT);
            expelPlayer(defendTeam, fouler);
            adjustWinProbabilityAfterExpulsion(defendTeam, fouler);
        }

        if (random < 10) {
            events.add("\tEl árbitro detiene el juego para atender al jugador que recibio la falta.");
            simulateInjuryPlayer(attackingTeam , beenfoul);
        }

        // Resuelve en penalty si la falta ocurrió en el área o si la flag lo indica
        if (preferPenalty) {
            simulatePenalty(attackingTeam, defendTeam);
            return;
        }

        if (rand.nextInt(100) > 15) {
            if (rand.nextInt(100) > 50) {
                simulateFreeKickDanger(attackingTeam, defendTeam);
            } else {
                simulateFreeKick(attackingTeam, defendTeam);
            }
        } else {
            simulatePenalty(attackingTeam, defendTeam);
        }
    }

    private void adjustWinProbabilityAfterExpulsion(Team foulingTeam, Player expelledPlayer) {
        int totalSkill = foulingTeam.getPlayers().stream().mapToInt(Player::getSkill).sum() + foulingTeam.getGoalie().getSkill();
        // Ajuste de probabilidad basado en la habilidad del jugador expulsado
        double skillImpact = expelledPlayer.getSkill() / 100.0; // Convierte la habilidad a un porcentaje
        int adjustedTotalSkill = totalSkill - expelledPlayer.getSkill();
        foulingTeam.setSkillAverage((double) adjustedTotalSkill / 11);

        // Ajusta la probabilidad de victoria
        if (foulingTeam == teamA) {
            teamA.setWinProbability(teamA.getWinProbability() - (skillImpact * 15)); // Mayor impacto para jugadores de mayor calidad
            teamB.setWinProbability(teamB.getWinProbability() + (skillImpact * 15));
        } else {
            teamB.setWinProbability(teamB.getWinProbability() - (skillImpact * 15));
            teamA.setWinProbability(teamA.getWinProbability() + (skillImpact * 15));
        }
        events.add("\t¡" + expelledPlayer.getName() + " ha sido expulsado!.");

    }

    private void simulateTangana(Team teamA, Team teamB, Player player1, Player player2) {
        events.add("\t¡SE HA FORMADO UNA TANGANA!");
        events.add("\t" + player1.getName() + " se encaraa con " + player2.getName() + " tras una dura entrada.");
        events.add("\tVARIOS JUGADORES DE AMBOS EQUIPOS SE METEN EN LA PELEA.");
        
        // Seleccionamos algunos jugadores aleatorios que se involucran en la tangana
        int involucrados = rand.nextInt(2) + 1; // 1-3 jugadores involucrados
        List<Player> participantes = new ArrayList<>();
        participantes.add(player1);
        participantes.add(player2);
        
        // Añadir jugadores aleatorios de ambos equipos
        for (int i = 0; i < involucrados; i++) {
            if (rand.nextBoolean()) {
                Player p = teamA.getPlayers().get(rand.nextInt(teamA.getPlayers().size()));
                if (!participantes.contains(p)) participantes.add(p);
            } else {
                Player p = teamB.getPlayers().get(rand.nextInt(teamB.getPlayers().size()));
                if (!participantes.contains(p)) participantes.add(p);
            }
        }

        // Simular consecuencias para cada participante
        for (Player p : participantes) {
            int sancion = rand.nextInt(100);
            Team equipo = teamA.getPlayers().contains(p) ? teamA : teamB;

            if (p.isHasYellowCard() || sancion > 90) { // Tarjeta roja si ya tenía amarilla o es muy grave
                events.add("\tTarjeta roja para " + p.getName() + " de " + equipo.getName() + 
                    (p.isHasYellowCard() ? " por doble amarilla." : " por participar activamente en la tangana."));
                p.setPoints(p.getPoints() + POINTS_RED_CARD_DIRECT);
                expelPlayer(equipo, p);
                adjustWinProbabilityAfterExpulsion(equipo, p);
            } else {
                events.add("\tTarjeta amarilla para " + p.getName() + " de " + equipo.getName() + ".");
                p.setHasYellowCard(true);
            }
        }
        
        events.add("\tEl árbitro consigue calmar los ánimos y el partido continúa.");
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

        allPlayers.addAll(expelledPlayers);       

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




