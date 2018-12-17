package ReactionTest.Communication.Messages;

import ReactionTest.General.ReactionTestConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class PlayerStats implements Serializable {
    ArrayList<StatsOfOnePlayer> players = new ArrayList<>();

    public void addPlayer(String playerName, int points){

        players.add(new StatsOfOnePlayer(playerName, points));

    }

    @Override
    public String toString() {
        players.sort(new Comparator<StatsOfOnePlayer>() {
            @Override
            public int compare(StatsOfOnePlayer statsOfOnePlayer, StatsOfOnePlayer t1) {
                return t1.points - statsOfOnePlayer.points;
            }
        });
        String stats = ReactionTestConstants.PLAYER_STATS_INIT;
        for (StatsOfOnePlayer player:players) {
            stats = stats + "\n";
            stats = stats + player.toString();
        }
        return stats;
    }


}



