package ReactionTest;

import java.util.ArrayList;

public class ServerGame {
    private String gameId;

    public ServerGame(String gameId) {
        this.gameId = gameId;
    }

    public static boolean isGameIdContained(ArrayList<ServerGame> serverGames, String id) {
        for (ServerGame sg : serverGames) {
            if (sg.getGameId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static String getFirstFreeId(ArrayList<ServerGame> serverGames) {
        int index = 1;
        while (isGameIdContained(serverGames, Integer.toString(index))) {
            index++;
        }
        return Integer.toString(index);
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
