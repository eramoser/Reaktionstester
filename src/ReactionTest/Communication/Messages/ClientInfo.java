package ReactionTest.Communication.Messages;

import java.io.Serializable;

public class ClientInfo implements Serializable {
    public String playerName;

    public ClientInfo(String playerName) {
        this.playerName = playerName;
    }

    public ClientInfo() {
    }
}
