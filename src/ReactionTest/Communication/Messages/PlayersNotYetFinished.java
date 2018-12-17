package ReactionTest.Communication.Messages;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayersNotYetFinished implements Serializable {
    public ArrayList<String> playersNotFinished = new ArrayList<>();

    @Override
    public String toString() {
        String playersNotReady = "";
        for (String playerName:playersNotFinished) {
            if (playersNotReady != ""){
                playersNotReady = playersNotReady + ", ";
            }
            playersNotReady = playersNotReady + playerName;
        }
        return "These Players are not ready: " + playersNotReady;
    }
}
