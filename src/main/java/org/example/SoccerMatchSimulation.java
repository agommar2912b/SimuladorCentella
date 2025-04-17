package org.example;

import org.example.entity.PlayerEntity;
import org.example.entity.TeamEntity;

import java.util.ArrayList;
import java.util.List;

public class SoccerMatchSimulation {
    public static void main(String[] args) {
        new SoccerMatchSimulation().show();
    }

    public void show() {
        List<PlayerEntity> criaturasPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Aiden", 91, Position.FORWARD, true), new PlayerEntity("Laurel", 87, Position.MIDFIELDER, true),
                new PlayerEntity("Ar ecks", 87, Position.MIDFIELDER, true), new PlayerEntity("Shawn", 89, Position.DEFENDER, true),
                new PlayerEntity("Byron", 91, Position.MIDFIELDER, true), new PlayerEntity("Samford", 86, Position.MIDFIELDER, true),
                new PlayerEntity("Erik", 88, Position.MIDFIELDER, true), new PlayerEntity("Kalil", 85, Position.DEFENDER, true),
                new PlayerEntity("Hurley", 89, Position.DEFENDER, true), new PlayerEntity("Peabody", 85, Position.DEFENDER, true)
        ));

        PlayerEntity criaturasGoalie = new PlayerEntity("Phobos", 87, Position.GOALKEEPER, true);

        List<PlayerEntity> criaturasBench = new ArrayList<>(List.of(
                new PlayerEntity("Peabody", 85, Position.GOALKEEPER, false),
                new PlayerEntity("Kappa", 84, Position.GOALKEEPER, false),
                new PlayerEntity("Garcia", 84, Position.DEFENDER, false), new PlayerEntity("Perseus", 82, Position.FORWARD, false), new PlayerEntity("Leung", 83, Position.MIDFIELDER, false)
        ));

        TeamEntity criaturasDeLaNoche = new TeamEntity("Criaturas de la Noche");
        criaturasDeLaNoche.setPlayers(criaturasPlayers);
        criaturasDeLaNoche.setGoalie(criaturasGoalie);
        criaturasDeLaNoche.setSubstitutes(criaturasBench);

        setPenaltyKickers(criaturasDeLaNoche, List.of("Aiden", "Shawn", "Byron"));
        setFreeKickKickers(criaturasDeLaNoche, List.of("Byron"));
        setCornerKickers(criaturasDeLaNoche, List.of("Byron"));

        List<PlayerEntity> dragonPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Bai Long", 89, Position.FORWARD, true), new PlayerEntity("Mitsuru", 88, Position.MIDFIELDER, true),
                new PlayerEntity("Tezcat", 89, Position.FORWARD, true), new PlayerEntity("Nosaka", 89, Position.MIDFIELDER, true),
                new PlayerEntity("Yurika", 88, Position.FORWARD, true), new PlayerEntity("Darren", 86, Position.MIDFIELDER, true),
                new PlayerEntity("Sael", 87, Position.FORWARD, true), new PlayerEntity("Hermana", 88, Position.DEFENDER, true),
                new PlayerEntity("Destra", 87, Position.FORWARD, true), new PlayerEntity("Froy", 88, Position.DEFENDER, true)
        ));
        PlayerEntity dragonGoalie = new PlayerEntity("Nishikage", 88, Position.GOALKEEPER, true);
        List<PlayerEntity> dragonBench = new ArrayList<>(List.of(
                new PlayerEntity("Keenan", 84, Position.GOALKEEPER, false), new PlayerEntity("Mike", 84, Position.FORWARD, false),
                new PlayerEntity("Tomatin", 85, Position.DEFENDER, false), new PlayerEntity("Nae", 86, Position.FORWARD, false), new PlayerEntity("Kozomaru", 85, Position.FORWARD, false)
        ));

        TeamEntity dragonLink = new TeamEntity("Dragon Link");
        dragonLink.setPlayers(dragonPlayers);
        dragonLink.setGoalie(dragonGoalie);
        dragonLink.setSubstitutes(dragonBench);

        setPenaltyKickers(dragonLink, List.of());
        setFreeKickKickers(dragonLink, List.of());
        setCornerKickers(dragonLink, List.of());


        List<PlayerEntity> inazumaPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Hector", 88, Position.FORWARD, true), new PlayerEntity("Robingo", 88, Position.FORWARD, true),
                new PlayerEntity("Alpha", 87, Position.FORWARD, true), new PlayerEntity("Harper", 88, Position.FORWARD, true),
                new PlayerEntity("Xene", 87, Position.MIDFIELDER, true), new PlayerEntity("Nakata", 90, Position.MIDFIELDER, true),
                new PlayerEntity("Quagmire", 86, Position.MIDFIELDER, true), new PlayerEntity("Trina", 87, Position.DEFENDER, true),
                new PlayerEntity("Quebec", 85, Position.DEFENDER, true), new PlayerEntity("Camelia", 86, Position.DEFENDER, true)
        ));
        PlayerEntity inazumaGoalie = new PlayerEntity("Nero", 86, Position.GOALKEEPER, true);
        List<PlayerEntity> inazumaBench = new ArrayList<>(List.of(
                new PlayerEntity("David", 87, Position.GOALKEEPER, false),
                new PlayerEntity("Bomber", 83, Position.DEFENDER, false),
                new PlayerEntity("Leonardo", 83, Position.MIDFIELDER, false), new PlayerEntity("Janus", 85, Position.MIDFIELDER, false), new PlayerEntity("Tsukikage", 85, Position.FORWARD, false)
        ));
        TeamEntity inazuma = new TeamEntity("Inazuma Japon");
        inazuma.setPlayers(inazumaPlayers);
        inazuma.setGoalie(inazumaGoalie);
        inazuma.setSubstitutes(inazumaBench);

        setPenaltyKickers(inazuma, List.of("Nakata", "Robingo", "Xene"));
        setFreeKickKickers(inazuma, List.of("Nakata", "Robingo", "Haru"));
        setCornerKickers(inazuma, List.of("Hector", "Nakata", "Robingo"));


        List<PlayerEntity> universalPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Victor", 89, Position.FORWARD, true), new PlayerEntity("Hiroto", 87, Position.FORWARD, true),
                new PlayerEntity("Ruger", 88, Position.FORWARD, true), new PlayerEntity("Falco", 87, Position.FORWARD, true),
                new PlayerEntity("Drakul", 89, Position.MIDFIELDER, true), new PlayerEntity("Sol", 87, Position.MIDFIELDER, true),
                new PlayerEntity("Ghiris", 87, Position.MIDFIELDER, true), new PlayerEntity("Houdini", 86, Position.DEFENDER, true),
                new PlayerEntity("Jack", 88, Position.DEFENDER, true), new PlayerEntity("Icer", 86, Position.DEFENDER, true)
        ));
        PlayerEntity universalGoalie = new PlayerEntity("Jp", 87, Position.GOALKEEPER, true);
        List<PlayerEntity> universalBench = new ArrayList<>(List.of(
                new PlayerEntity("Saturn", 82, Position.GOALKEEPER, false),
                new PlayerEntity("Malcolm", 84, Position.DEFENDER, false),
                new PlayerEntity("Kia", 84, Position.MIDFIELDER, false),
                new PlayerEntity("Arthur", 84, Position.FORWARD, false),
                new PlayerEntity("Dakkar", 84, Position.MIDFIELDER, false)
        ));
        TeamEntity universal = new TeamEntity("Universal");
        universal.setPlayers(universalPlayers);
        universal.setGoalie(universalGoalie);
        universal.setSubstitutes(universalBench);

        setPenaltyKickers(universal, List.of("Hiroto", "Victor", "Sol"));
        setFreeKickKickers(universal, List.of("Sol", "Drakul", "Ghiris"));
        setCornerKickers(universal, List.of("Sol", "Drakul", "Ghiris"));

        List<PlayerEntity> unicornPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Beta", 88, Position.FORWARD, true), new PlayerEntity("Edgar", 87, Position.FORWARD, true),
                new PlayerEntity("Turner", 86, Position.FORWARD, true), new PlayerEntity("Rondula", 86, Position.MIDFIELDER, true),
                new PlayerEntity("Paolo", 89, Position.MIDFIELDER, true), new PlayerEntity("Krueger", 88, Position.MIDFIELDER, true),
                new PlayerEntity("Scotty", 86, Position.DEFENDER, true), new PlayerEntity("Tori", 87, Position.DEFENDER, true),
                new PlayerEntity("Nathan", 89, Position.DEFENDER, true), new PlayerEntity("Goldie", 89, Position.DEFENDER, true)
        ));
        PlayerEntity unicornGoalie = new PlayerEntity("Quentin", 89, Position.GOALKEEPER, true);
        List<PlayerEntity> unicornBench = new ArrayList<>(List.of(
                new PlayerEntity("Gigi", 86, Position.GOALKEEPER, false),
                new PlayerEntity("Tasuke", 83, Position.DEFENDER, false),
                new PlayerEntity("Garreu", 83, Position.MIDFIELDER, false), new PlayerEntity("Zephyr", 85, Position.DEFENDER, false), new PlayerEntity("Zack", 85, Position.FORWARD, false)
        ));
        TeamEntity unicorn = new TeamEntity("Unicorn");
        unicorn.setPlayers(unicornPlayers);
        unicorn.setGoalie(unicornGoalie);
        unicorn.setSubstitutes(unicornBench);

        setPenaltyKickers(unicorn, List.of("Beta", "Paolo", "Edgar"));
        setFreeKickKickers(unicorn, List.of("Paolo", "Nathan"));
        setCornerKickers(unicorn, List.of("Krueger", "Nathan"));

        List<PlayerEntity> royalReduxPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Vulpeen", 89, Position.FORWARD, true), new PlayerEntity("Haizaki", 87, Position.FORWARD, true),
                new PlayerEntity("Vladimir", 87, Position.FORWARD, true), new PlayerEntity("Austin", 88, Position.FORWARD, true),
                new PlayerEntity("Petronio", 86, Position.MIDFIELDER, true), new PlayerEntity("Caleb", 91, Position.MIDFIELDER, true),
                new PlayerEntity("Jude", 89, Position.MIDFIELDER, true), new PlayerEntity("Acker", 85, Position.DEFENDER, true),
                new PlayerEntity("Gabi", 89, Position.DEFENDER, true), new PlayerEntity("Bobby", 85, Position.DEFENDER, true),
                new PlayerEntity("King", 86, Position.GOALKEEPER, true)
        ));
        PlayerEntity royalReduxGoalie = new PlayerEntity("King", 86, Position.GOALKEEPER, true);
        List<PlayerEntity> royalReduxBench = new ArrayList<>(List.of(
                new PlayerEntity("Salvador", 86, Position.GOALKEEPER, false),
                new PlayerEntity("Billy", 83, Position.DEFENDER, false),
                new PlayerEntity("Kirina", 85, Position.MIDFIELDER, false), new PlayerEntity("Acuto", 85, Position.MIDFIELDER, false), new PlayerEntity("Lucian", 82, Position.FORWARD, false)
        ));
        TeamEntity royalRedux = new TeamEntity("Royal Academy Redux");
        royalRedux.setPlayers(royalReduxPlayers);
        royalRedux.setGoalie(royalReduxGoalie);
        royalRedux.setSubstitutes(royalReduxBench);

        setPenaltyKickers(royalRedux, List.of("Caleb", "Vulpeen", "Lucian"));
        setFreeKickKickers(royalRedux, List.of("Caleb", "Acuto"));
        setCornerKickers(royalRedux, List.of("Jude", "Acker"));

        List<PlayerEntity> zanPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Torch", 88, Position.FORWARD, true), new PlayerEntity("Arion", 89, Position.FORWARD, true),
                new PlayerEntity("Davy", 85, Position.FORWARD, true), new PlayerEntity("Fei", 87, Position.MIDFIELDER, true),
                new PlayerEntity("Buddy", 84, Position.MIDFIELDER, true), new PlayerEntity("Xing Zhou", 83, Position.MIDFIELDER, true),
                new PlayerEntity("Quintet", 86, Position.DEFENDER, true), new PlayerEntity("Iggie", 83, Position.DEFENDER, true),
                new PlayerEntity("Ozrock", 88, Position.DEFENDER, true), new PlayerEntity("Mitya", 80, Position.DEFENDER, true)
        ));
        PlayerEntity zanGoalie = new PlayerEntity("Terru", 88, Position.GOALKEEPER, true);
        List<PlayerEntity> zanBench = new ArrayList<>(List.of(
                new PlayerEntity("Astaroth", 85, Position.GOALKEEPER, false),
                new PlayerEntity("Yang", 83, Position.MIDFIELDER, false), new PlayerEntity("Syon", 83, Position.FORWARD, false), new PlayerEntity("Plink", 84, Position.MIDFIELDER, false)
        ));
        TeamEntity zan = new TeamEntity("Zan");
        zan.setPlayers(zanPlayers);
        zan.setGoalie(zanGoalie);
        zan.setSubstitutes(zanBench);

        setPenaltyKickers(zan, List.of("Arion", "Fei"));
        setFreeKickKickers(zan, List.of("Ozrock", "Quintet"));
        setCornerKickers(zan, List.of("Torch", "Davy"));

        List<PlayerEntity> orfeoPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Simeon", 91, Position.FORWARD, true), new PlayerEntity("Lancer", 87, Position.FORWARD, true),
                new PlayerEntity("Gamma", 87, Position.FORWARD, true), new PlayerEntity("Gandares", 88, Position.FORWARD, true),
                new PlayerEntity("Cronus", 87, Position.MIDFIELDER, true), new PlayerEntity("Caleb Redux", 87, Position.MIDFIELDER, true),
                new PlayerEntity("Choi", 85, Position.MIDFIELDER, true), new PlayerEntity("Yale", 86, Position.DEFENDER, true),
                new PlayerEntity("Sor", 87, Position.DEFENDER, true), new PlayerEntity("Aster", 85, Position.FORWARD, true)
        ));
        PlayerEntity orfeoGoalie = new PlayerEntity("MECAMARK", 88, Position.GOALKEEPER, true);
        List<PlayerEntity> orfeoBench = new ArrayList<>(List.of(
                new PlayerEntity("Lucefar", 86, Position.GOALKEEPER, false),
                new PlayerEntity("Zohen", 82, Position.DEFENDER, false),
                new PlayerEntity("Adé", 85, Position.MIDFIELDER, false),
                new PlayerEntity("Maximiano", 84, Position.MIDFIELDER, false),
                new PlayerEntity("Lump", 82, Position.DEFENDER, false)
        ));

        TeamEntity orfeo = new TeamEntity("Orfeo");
        orfeo.setPlayers(orfeoPlayers);
        orfeo.setGoalie(orfeoGoalie);
        orfeo.setSubstitutes(orfeoBench);

        setPenaltyKickers(orfeo, List.of("Simeon"));
        setFreeKickKickers(orfeo, List.of("Cronus"));
        setCornerKickers(orfeo, List.of("Cronus"));

        List<PlayerEntity> marytimesPlayers = new ArrayList<>(List.of(
                new PlayerEntity("Dylan", 86, Position.FORWARD, true), new PlayerEntity("Zanark", 88, Position.FORWARD, true),
                new PlayerEntity("Flora", 89, Position.FORWARD, true), new PlayerEntity("Clario", 89, Position.MIDFIELDER, true),
                new PlayerEntity("Bellatrix", 87, Position.MIDFIELDER, true), new PlayerEntity("Samford", 87, Position.MIDFIELDER, true),
                new PlayerEntity("Sonny", 87, Position.MIDFIELDER, true), new PlayerEntity("Thor", 86, Position.DEFENDER, true),
                new PlayerEntity("Thiago", 89, Position.DEFENDER, true), new PlayerEntity("Malcom", 85, Position.DEFENDER, true)
        ));
        PlayerEntity marytimesGoalie = new PlayerEntity("Il grande", 88, Position.GOALKEEPER, true);
        List<PlayerEntity> marytimesBench = new ArrayList<>(List.of(
                new PlayerEntity("Dvalin", 85, Position.GOALKEEPER, false),
                new PlayerEntity("Mountain", 84, Position.GOALKEEPER, false),
                new PlayerEntity("Steve", 83, Position.MIDFIELDER, false), new PlayerEntity("Kevin", 85, Position.FORWARD, false), new PlayerEntity("Tatsumi", 84, Position.MIDFIELDER, false)
        ));
        TeamEntity marytimes = new TeamEntity("Mary Times");
        marytimes.setPlayers(marytimesPlayers);
        marytimes.setGoalie(marytimesGoalie);
        marytimes.setSubstitutes(marytimesBench);

        setPenaltyKickers(marytimes, List.of("Flora", "Zanark"));
        setFreeKickKickers(marytimes, List.of("Clario", "Bellatrix"));
        setCornerKickers(marytimes, List.of("Bellatrix", "Samford"));

        Game game = new Game(dragonLink, marytimes);
        game.simulate();
        game.showEvents();
    }

    private void setPenaltyKickers(TeamEntity team, List<String> names) {
        team.setPenaltyKickers(team.getPlayers().stream()
                .filter(player -> names.contains(player.getName()))
                .toList());
    }

    private void setFreeKickKickers(TeamEntity team, List<String> names) {
        team.setFreekickKickers(team.getPlayers().stream()
                .filter(player -> names.contains(player.getName()))
                .toList());
    }

    private void setCornerKickers(TeamEntity team, List<String> names) {
        team.setCornerKickers(team.getPlayers().stream()
                .filter(player -> names.contains(player.getName()))
                .toList());
    }
}



