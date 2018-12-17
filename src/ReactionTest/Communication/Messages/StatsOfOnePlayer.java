package ReactionTest.Communication.Messages;

import java.io.Serializable;

public class StatsOfOnePlayer implements Serializable {
    public StatsOfOnePlayer(String playerName, int points) {
        this.playerName = playerName;
        this.points = points;
    }

    private String playerName;
    public int points;

    @Override
    public String toString() {
        return playerName + ": " + points;
    }
}