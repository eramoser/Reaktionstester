package ReactionTest.Messages;

import ReactionTest.ReactionTestConstants;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayerStats implements Serializable {
    ArrayList<StatsOfOnePlayer> players = new ArrayList<>();

    public void addPlayer(String playerName, int points){
        if (points != 0) {
            players.add(new StatsOfOnePlayer(playerName, points));
        }
    }

    @Override
    public String toString() {
        String stats = ReactionTestConstants.PLAYER_STATS_INIT;
        for (StatsOfOnePlayer player:players) {
            stats = stats + "\n";
            stats = stats + player.toString();
        }
        return stats;
    }


}



