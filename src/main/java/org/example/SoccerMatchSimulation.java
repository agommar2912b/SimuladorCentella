package org.example;

import java.util.ArrayList;
import java.util.List;

public class SoccerMatchSimulation {
    public static void main(String[] args) {
        new SoccerMatchSimulation().show();
    }
    public void show() {
        List<Player> criaturasPlayers = new ArrayList<>(List.of(
                new Player("Aiden", 92, Position.FORWARD, true), new Player("Laurel", 87, Position.MIDFIELDER, true),
                new Player("Ar ecks", 87, Position.MIDFIELDER, true), new Player("Shawn", 89, Position.DEFENDER, true),
                new Player("Byron", 92, Position.MIDFIELDER, true), new Player("Samford R", 86, Position.MIDFIELDER, true),
                new Player("Erik", 88, Position.MIDFIELDER, true), new Player("Kalil", 85, Position.DEFENDER, true),
                new Player("Hurley", 89, Position.DEFENDER, true), new Player("Nathan 0", 87, Position.DEFENDER, true)
        ));

        Player criaturasGoalie = new Player("Phobos", 87, Position.GOALKEEPER, true);

        List<Player> criaturasBench = new ArrayList<>(List.of(
                new Player("Peabody", 85, Position.GOALKEEPER, false),
                new Player("Kappa", 84, Position.MIDFIELDER, false),
                new Player("Garcia", 84, Position.DEFENDER, false), new Player("Perseus", 82, Position.FORWARD, false), new Player("Leung", 83, Position.MIDFIELDER, false)
        ));

        Team criaturasDeLaNoche = new Team("Criaturas de la Noche", criaturasPlayers, criaturasGoalie, criaturasBench);

        setPenaltyKickers(criaturasDeLaNoche, List.of("Aiden", "Shawn", "Byron"));
        setFreeKickKickers(criaturasDeLaNoche, List.of("Byron"));
        setCornerKickers(criaturasDeLaNoche, List.of("Byron"));

        List<Player> dragonPlayers = new ArrayList<>(List.of(
                new Player("Bai Long", 89, Position.FORWARD, true), new Player("Mitsuru", 88, Position.MIDFIELDER, true),
                new Player("Tezcat", 89, Position.FORWARD, true), new Player("Nosaka", 89, Position.MIDFIELDER, true),
                new Player("Yurika", 88, Position.FORWARD, true), new Player("Darren", 86, Position.MIDFIELDER, true),
                new Player("Sael", 87, Position.FORWARD, true), new Player("Hermana", 88, Position.DEFENDER, true),
                new Player("Destra", 87, Position.FORWARD, true), new Player("Froy", 88, Position.DEFENDER, true)
        ));
        Player dragonGoalie = new Player("Nishikage", 88, Position.GOALKEEPER, true);
        List<Player> dragonBench = new ArrayList<>(List.of(
                new Player("Keenan", 84, Position.GOALKEEPER, false), new Player("Mike", 84, Position.FORWARD, false),
                new Player("Tomatin", 85, Position.DEFENDER, false), new Player("Nae", 86, Position.FORWARD, false), new Player("Kozomaru", 85, Position.FORWARD, false)
        ));

        Team dragonLink = new Team("Dragon Link", dragonPlayers, dragonGoalie, dragonBench);

        setPenaltyKickers(dragonLink, List.of());
        setFreeKickKickers(dragonLink, List.of());
        setCornerKickers(dragonLink, List.of());


        List<Player> inazumaPlayers = new ArrayList<>(List.of(
                new Player("Hector", 88, Position.FORWARD, true), new Player("Robingo", 88, Position.FORWARD, true),
                new Player("Alpha", 87, Position.FORWARD, true), new Player("Harper", 88, Position.FORWARD, true),
                new Player("Xene", 87, Position.MIDFIELDER, true), new Player("Nakata", 90, Position.MIDFIELDER, true),
                new Player("Quagmire", 86, Position.MIDFIELDER, true), new Player("Trina", 87, Position.DEFENDER, true),
                new Player("Quebec", 85, Position.DEFENDER, true), new Player("Camelia", 86, Position.DEFENDER, true)
        ));
        Player inazumaGoalie = new Player("Nero", 86, Position.GOALKEEPER, true);
        List<Player> inazumaBench = new ArrayList<>(List.of(
                new Player("David", 87, Position.GOALKEEPER, false),
                new Player("Bomber", 83, Position.DEFENDER, false),
                new Player("Leonardo", 83, Position.MIDFIELDER, false), new Player("Janus", 85, Position.MIDFIELDER, false), new Player("Tsukikage", 85, Position.FORWARD, false)
        ));
        Team inazuma = new Team("Inazuma Japon", inazumaPlayers, inazumaGoalie, inazumaBench);

        setPenaltyKickers(inazuma, List.of("Nakata", "Robingo", "Xene"));
        setFreeKickKickers(inazuma, List.of("Nakata", "Robingo", "Haru"));
        setCornerKickers(inazuma, List.of("Hector", "Nakata", "Robingo"));


        List<Player> universalPlayers = new ArrayList<>(List.of(
                new Player("Victor", 89, Position.FORWARD, true), new Player("Hiroto", 87, Position.FORWARD, true),
                new Player("Ruger", 88, Position.FORWARD, true), new Player("Falco", 87, Position.FORWARD, true),
                new Player("Drakul", 89, Position.MIDFIELDER, true), new Player("Sol", 87, Position.MIDFIELDER, true),
                new Player("Ghiris", 87, Position.MIDFIELDER, true), new Player("Houdini", 86, Position.DEFENDER, true),
                new Player("Jack", 88, Position.DEFENDER, true), new Player("Icer", 86, Position.DEFENDER, true)
        ));
        Player universalGoalie = new Player("Jp", 87, Position.GOALKEEPER, true);
        List<Player> universalBench = new ArrayList<>(List.of(
                new Player("Saturn", 82, Position.GOALKEEPER, false),
                new Player("Malcolm", 84, Position.DEFENDER, false),
                new Player("Kia", 84, Position.MIDFIELDER, false),
                new Player("Arthur", 84, Position.FORWARD, false),
                new Player("Dakkar", 84, Position.MIDFIELDER, false)
        ));
        Team universal = new Team("Universal", universalPlayers, universalGoalie, universalBench);

        setPenaltyKickers(universal, List.of("Hiroto", "Victor", "Sol"));
        setFreeKickKickers(universal, List.of("Sol", "Drakul", "Ghiris"));
        setCornerKickers(universal, List.of("Sol", "Drakul", "Ghiris"));

        List<Player> unicornPlayers = new ArrayList<>(List.of(
                new Player("Beta", 88, Position.FORWARD, true), new Player("Edgar", 87, Position.FORWARD, true),
                new Player("Turner", 86, Position.FORWARD, true), new Player("Rondula", 86, Position.MIDFIELDER, true),
                new Player("Paolo", 89, Position.MIDFIELDER, true), new Player("Krueger", 88, Position.MIDFIELDER, true),
                new Player("Scotty", 86, Position.DEFENDER, true), new Player("Tori", 87, Position.DEFENDER, true),
                new Player("Nathan", 89, Position.DEFENDER, true), new Player("Goldie", 89, Position.DEFENDER, true)
        ));
        Player unicornGoalie = new Player("Quentin", 89, Position.GOALKEEPER, true);
        List<Player> unicornBench = new ArrayList<>(List.of(
                new Player("Gigi", 86, Position.GOALKEEPER, false),
                new Player("Tasuke", 83, Position.DEFENDER, false),
                new Player("Garreu", 83, Position.MIDFIELDER, false), new Player("Zephyr", 85, Position.DEFENDER, false), new Player("Zack", 85, Position.FORWARD, false)
        ));
        Team unicorn = new Team("Unicorn", unicornPlayers, unicornGoalie, unicornBench);

        setPenaltyKickers(unicorn, List.of("Beta", "Paolo", "Edgar"));
        setFreeKickKickers(unicorn, List.of("Paolo", "Nathan"));
        setCornerKickers(unicorn, List.of("Krueger", "Nathan"));

        List<Player> royalReduxPlayers = new ArrayList<>(List.of(
                new Player("Vulpeen", 89, Position.FORWARD, true), new Player("Haizaki", 87, Position.FORWARD, true),
                new Player("Vladimir", 87, Position.FORWARD, true), new Player("Austin", 88, Position.FORWARD, true),
                new Player("Petronio", 86, Position.MIDFIELDER, true), new Player("Caleb", 91, Position.MIDFIELDER, true),
                new Player("Jude", 89, Position.MIDFIELDER, true), new Player("Acker", 85, Position.DEFENDER, true),
                new Player("Gabi", 89, Position.DEFENDER, true), new Player("Bobby", 85, Position.DEFENDER, true),
                new Player("King", 86, Position.GOALKEEPER, true)
        ));
        Player royalReduxGoalie = new Player("King", 86, Position.GOALKEEPER, true);
        List<Player> royalReduxBench = new ArrayList<>(List.of(
                new Player("Salvador", 86, Position.GOALKEEPER, false),
                new Player("Billy", 83, Position.DEFENDER, false),
                new Player("Kirina", 85, Position.MIDFIELDER, false), new Player("Acuto", 85, Position.MIDFIELDER, false), new Player("Lucian", 82, Position.FORWARD, false)
        ));
        Team royalRedux = new Team("Royal Academy Redux", royalReduxPlayers, royalReduxGoalie, royalReduxBench);

        setPenaltyKickers(royalRedux, List.of("Caleb", "Vulpeen", "Lucian"));
        setFreeKickKickers(royalRedux, List.of("Caleb", "Acuto"));
        setCornerKickers(royalRedux, List.of("Jude", "Acker"));

        List<Player> zanPlayers = new ArrayList<>(List.of(
                new Player("Torch", 88, Position.FORWARD, true), new Player("Arion", 89, Position.FORWARD, true),
                new Player("Davy", 85, Position.FORWARD, true), new Player("Fei", 87, Position.MIDFIELDER, true),
                new Player("Buddy", 84, Position.MIDFIELDER, true), new Player("Xing Zhou", 83, Position.MIDFIELDER, true),
                new Player("Quintet", 86, Position.DEFENDER, true), new Player("Iggie", 83, Position.DEFENDER, true),
                new Player("Ozrock", 88, Position.DEFENDER, true), new Player("Mitya", 80, Position.DEFENDER, true)
        ));
        Player zanGoalie = new Player("Terry", 88, Position.GOALKEEPER, true);
        List<Player> zanBench = new ArrayList<>(List.of(
                new Player("Astaroth", 85, Position.GOALKEEPER, false),
                new Player("Yang", 83, Position.MIDFIELDER, false), new Player("Syon", 83, Position.FORWARD,false), new Player("Plink", 84, Position.MIDFIELDER, false)
        ));
        Team zan = new Team("Zan", zanPlayers, zanGoalie, zanBench);

        setPenaltyKickers(zan, List.of("Arion", "Fei"));
        setFreeKickKickers(zan, List.of("Ozrock", "Quintet"));
        setCornerKickers(zan, List.of("Torch", "Davy"));

        List<Player> orfeoPlayers = new ArrayList<>(List.of(
                new Player("Simeon", 91, Position.FORWARD,true), new Player("Lancer", 87,  Position.FORWARD,true),
                new Player("Gamma", 87, Position.FORWARD,true), new Player("Gandares", 88,  Position.FORWARD,true),
                new Player("Cronus", 87, Position.MIDFIELDER,true), new Player("Caleb Redux", 87,  Position.MIDFIELDER,true),
                new Player("Choi", 85, Position.MIDFIELDER,true), new Player("Yale", 86,  Position.DEFENDER,true),
                new Player("Sor", 87,  Position.DEFENDER,true), new Player("Aster", 85, Position.FORWARD,true)
        ));
        Player orfeoGoalie = new Player("MECAMARK", 88,  Position.GOALKEEPER,true);
        List<Player> orfeoBench = new ArrayList<>(List.of(
                new Player("Luceafar", 86,Position.GOALKEEPER,false),
                new Player("Zohen", 82,  Position.DEFENDER,false),
                new Player("Adé", 85,  Position.MIDFIELDER,false),
                new Player("Maximiano", 84,  Position.MIDFIELDER,false),
                new Player("Lump", 82,  Position.DEFENDER,false)
        ));

        Team orfeo = new Team("Orfeo", orfeoPlayers, orfeoGoalie, orfeoBench);

        setPenaltyKickers(orfeo, List.of("Simeon"));
        setFreeKickKickers(orfeo, List.of("Cronus"));
        setCornerKickers(orfeo, List.of("Cronus"));

        List<Player> marytimesPlayers = new ArrayList<>(List.of(
                new Player("Dylan", 86, Position.FORWARD,true), new Player("Zanark", 88,  Position.FORWARD,true),
                new Player("Flora", 89, Position.FORWARD,true), new Player("Clario", 89,  Position.MIDFIELDER,true),
                new Player("Bellatrix", 87, Position.MIDFIELDER,true), new Player("Samford", 87,  Position.MIDFIELDER,true),
                new Player("Sonny", 87, Position.MIDFIELDER,true), new Player("Thor", 86,  Position.DEFENDER,true),
                new Player("Thiago", 89,  Position.DEFENDER,true), new Player("Malcom", 85, Position.DEFENDER,true)
        ));
        Player marytimesGoalie = new Player("Il grande", 88,  Position.GOALKEEPER,true);
        List<Player> marytimesBench = new ArrayList<>(List.of(
                new Player("Dvalin", 85,Position.GOALKEEPER,false),
                new Player("Mountain", 84,  Position.GOALKEEPER,false),
                new Player("Steve", 83,  Position.MIDFIELDER,false), new Player("Kevin", 85,  Position.FORWARD,false), new Player("Tatsumi", 84,  Position.MIDFIELDER,false)
        ));
        Team marytimes = new Team("Mary Times", marytimesPlayers, marytimesGoalie, marytimesBench);

        setPenaltyKickers(marytimes, List.of("Flora","Zanark"));
        setFreeKickKickers(marytimes, List.of("Clario","Bellatrix"));
        setCornerKickers(marytimes, List.of("Bellatrix","Samford"));

        List<Player> gigantesPlayers = new ArrayList<>(List.of(
                new Player("Gazelle", 88, Position.FORWARD,true), new Player("Max", 86,  Position.MIDFIELDER,true),
                new Player("Skipper", 83, Position.FORWARD,true), new Player("Max oscuro", 86,  Position.MIDFIELDER,true),
                new Player("Tom Dark", 88, Position.FORWARD,true), new Player("Subaru", 83,  Position.DEFENDER,true),
                new Player("Arculus", 87, Position.FORWARD,true), new Player("Master", 84,  Position.DEFENDER,true),
                new Player("Tod", 85, Position.DEFENDER,true), new Player("Shadow oscuro", 83,  Position.FORWARD,true)
        ));
        Player gigantesGoalie = new Player("King Redux", 88,  Position.GOALKEEPER,true);
        List<Player> gigantesBench = new ArrayList<>(List.of(
                new Player("Alvicci", 80,Position.GOALKEEPER,false),
                new Player("Galliano", 82,  Position.DEFENDER,false),
                new Player("Steve", 84,  Position.MIDFIELDER,false), new Player("Gabrini", 82,  Position.MIDFIELDER,false),  new Player("Michael", 82,  Position.FORWARD,false)
//                new Player("Yurkeh", 83,  Position.MIDFIELDER,false)
        ));

        Team gigantes = new Team("Gigantes", gigantesPlayers, gigantesGoalie, gigantesBench);

        setPenaltyKickers(gigantes, List.of("Gazelle"));
        setFreeKickKickers(gigantes, List.of("Max oscuro"));
        setCornerKickers(gigantes, List.of("Yurkeh"));

        List<Player> dragonesDeFuegoPlayers = new ArrayList<>(List.of(
                new Player("Dvalin", 86, Position.FORWARD, true),
                new Player("Bala Gasgula", 86, Position.FORWARD, true),
                new Player("Soundtown", 86, Position.MIDFIELDER, true),
                new Player("Hao Li", 87, Position.MIDFIELDER, true),
                new Player("Roma", 87, Position.MIDFIELDER, true),
                new Player("Malik", 85, Position.MIDFIELDER, true),
                new Player("Banda", 86, Position.MIDFIELDER, true),
                new Player("Zippy Lerner", 86, Position.DEFENDER, true),
                new Player("Archer", 87, Position.DEFENDER, true),
                new Player("Keenan Sharpe", 86, Position.DEFENDER, true)
        ));

        Player dragonesDeFuegoGoalie = new Player("Mark", 90, Position.GOALKEEPER, true);

        List<Player> dragonesDeFuegoBench = new ArrayList<>(List.of(
                new Player("Preston", 80, Position.GOALKEEPER, false),
                new Player("Kiburn", 83, Position.DEFENDER, false),
                new Player("Hairy", 84, Position.MIDFIELDER, false),
                new Player("Lus Kasim", 84, Position.DEFENDER, false),
                new Player("Canon Evans", 85, Position.FORWARD, false)));

        Team dragones = new Team("Dragones de fuego", dragonesDeFuegoPlayers, dragonesDeFuegoGoalie, dragonesDeFuegoBench);

        setPenaltyKickers(dragones, List.of("Gazelle"));
        setFreeKickKickers(dragones, List.of("Max oscuro"));
        setCornerKickers(dragones, List.of("Yurkeh"));


        List<Player> zeroPlayers = new ArrayList<>(List.of(
                new Player("Axel", 89, Position.FORWARD, true), new Player("Njord", 87, Position.FORWARD, true),
                new Player("Xavier", 89, Position.FORWARD, true), new Player("Aitor", 86, Position.DEFENDER, true),
                new Player("Mehr", 87, Position.MIDFIELDER, true), new Player("Jordan", 86, Position.MIDFIELDER, true),
                new Player("Riccardo", 89, Position.MIDFIELDER, true), new Player("Frank", 85, Position.DEFENDER, true),
                new Player("Clear", 84, Position.DEFENDER, true), new Player("Ogar", 84, Position.DEFENDER, true)
        ));

        Player zeroGoalie = new Player("Skie Blue", 84, Position.GOALKEEPER, true);

        List<Player> zeroBench = new ArrayList<>(List.of(
                new Player("Samguk", 83, Position.GOALKEEPER, false),
                new Player("Cerise", 84, Position.MIDFIELDER, false),
                new Player("Gozu", 83, Position.DEFENDER, false), new Player("Yuri Rodina", 83, Position.FORWARD, false), new Player("Dolphin", 83, Position.MIDFIELDER, false)));


        Team zero = new Team("Zero", zeroPlayers, zeroGoalie, zeroBench);
        setPenaltyKickers(zero, List.of("Axel","Xavier"));
        setFreeKickKickers(zero, List.of("Riccardo","Jordan"));
        setCornerKickers(zero, List.of("Riccardo","Aitor"));

        Game game = new Game(zero, universal);
        game.simulate();
        game.showEvents();
    }

    private void setPenaltyKickers(Team team, List<String> names) {
        team.penaltyKickers = team.players.stream()
                .filter(player -> names.contains(player.name))
                .toList();
    }

    private void setFreeKickKickers(Team team, List<String> names) {
        team.freekickKickers = team.players.stream()
                .filter(player -> names.contains(player.name))
                .toList();
    }

    private void setCornerKickers(Team team, List<String> names) {
        team.cornerKickers = team.players.stream()
                .filter(player -> names.contains(player.name))
                .toList();
    }
}




