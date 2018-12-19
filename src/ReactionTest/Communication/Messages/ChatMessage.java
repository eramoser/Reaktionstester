package ReactionTest.Communication.Messages;

import ReactionTest.Communication.ReactionTestClient;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatMessage implements Serializable {
    public String playerName = "";
    public String message;
    public Date date;

    public ChatMessage(String message) {
        this.message = message;
        date = new Date();
    }

    @Override
    public String toString() {
        return new SimpleDateFormat("HH:mm").format(date) + " " + playerName + ": " + message;
    }
}
