package ReactionTest.Messages;

import java.io.Serializable;

public class StatsOfOnePlayer implements Serializable {
    public StatsOfOnePlayer(String playerName, float points) {
        this.playerName = playerName;
        this.points = points;
    }

    private String playerName;
    private float points;

    @Override
    public String toString() {
        return playerName + ": " + points;
    }
}