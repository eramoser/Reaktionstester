package ReactionTest.Messages;

import java.io.Serializable;

public class StatsOfOnePlayer implements Serializable {
    public StatsOfOnePlayer(String playerName, float time) {
        this.playerName = playerName;
        this.time = time;
    }

    private String playerName;
    private float time;

    @Override
    public String toString() {
        return playerName + ": " + time;
    }
}