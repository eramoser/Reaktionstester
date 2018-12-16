package ReactionTest.Messages;

import java.io.Serializable;

public class ChangeGame implements Serializable {
    public String gameId;

    public ChangeGame(String gameId) {
        this.gameId = gameId;
    }
}
