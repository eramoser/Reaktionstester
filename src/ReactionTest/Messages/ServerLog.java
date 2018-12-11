package ReactionTest.Messages;

import java.io.Serializable;

public class ServerLog implements Serializable {
    public String message;

    public ServerLog(String message) {
        this.message = message;
    }
}
