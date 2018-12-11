package ReactionTest.Messages;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayerStats implements Serializable {
    ArrayList<StatsOfOnePlayer> players = new ArrayList<>();

    public void addPlayer(String playerName, float time){
        players.add(new StatsOfOnePlayer(playerName, time));
    }

    @Override
    public String toString() {
        String stats = "";
        for (StatsOfOnePlayer player:players) {
            if (stats != ""){
                stats = stats + ", ";
            }
            stats = stats + player.toString();
        }
        return stats;
    }


}



