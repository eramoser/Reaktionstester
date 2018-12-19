package ReactionTest.Communication.Messages;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    String messages;

    public Chat(ArrayList<ChatMessage> messages) {
        String ret = "";
        for (ChatMessage message:messages) {
            ret = ret + message.toString() + "\n";
        }
        this.messages = ret;
    }

    @Override
    public String toString() {
        return messages;
    }
}
