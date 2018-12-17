package ReactionTest;

import ReactionTest.Messages.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

public class ServerGame {
    private String gameId;
    private ArrayList<ReactionTestClient> clients = new ArrayList<>();

    private int anonymIndex = 0;

    private String[] defaultPlayerNames = {"\"Nur-Kurz-aufs-Klo\" Jack", "Kopfrechnengenie Wenzl", "Tesla Jacki", "Dirty JackSchabrack", "\"Des-Wochnend-dring-I-nix\" Waunz"};
    private ArrayList<String> defaultNamesList = new ArrayList<>();
    private ArrayList<String> availableNames = new ArrayList<>();
    private ActionListener clientListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ClientObjectPair pair = (ClientObjectPair)actionEvent.getSource();
            ReactionTestClient client = pair.client;
            Serializable objectReceived = pair.object;
            if (client.getClass().equals(ReactionTestClient.class)) {

                // Handle if Object GameMove is received
                if (objectReceived.getClass().equals(GameMove.class)) {
                    client.state = ReactionTestClient.READY_TO_START;
                    client.time = ((GameMove) objectReceived).time;

                    startMoveOrPlayersNotFinished();
                    Log.log("Game Move (" + gameId + ") Received");
                }

                // Handle if Object ClientInfo is received
                if (objectReceived.getClass().equals(ClientInfo.class)) {
                    ClientInfo clientInfo = (ClientInfo) objectReceived;
                    if (clientInfo.playerName != null) {
                        freeNameIfDefault(client.playerName);
                        client.playerName = clientInfo.playerName;
                    }
                }

                // Handle if Client Disconnects
                if (objectReceived.getClass().equals(Disconnect.class)) {
                    Log.log("Disconnected player " + client.playerName);
                    clients.remove(client);
                    client.stop();
                    freeNameIfDefault(client.playerName);
                    startMoveOrPlayersNotFinished();
                }

                System.out.println("Client: " + client.playerName + " Object received: " + objectReceived.getClass());

            } else {
                System.out.println("Received Message from other class than ReactionTestClient: " + client.getClass().toString());
            }
        }
    };


    public ServerGame(String gameId) {
        this.gameId = gameId;

        for (String name : defaultPlayerNames) {
            availableNames.add(name);
            defaultNamesList.add(name);
        }
    }

    public static boolean isGameIdContained(ArrayList<ServerGame> serverGames, String id) {
        return getGameWithId(serverGames, id) != null;
    }

    public static ServerGame getGameWithId(ArrayList<ServerGame> serverGames, String id) {
        for (ServerGame sg : serverGames) {
            if (sg.getGameId().equals(id)) {
                return sg;
            }
        }
        return null;
    }

    public void addClient(ReactionTestClient client) {
        clients.add(client);
        client.game = this;
        client.points = 0;

        client.addActionListener(clientListener);

        client.playerName = getRandomDefaultName();
        client.send(new ClientInfo(client.playerName));
        if (clients.size() == 1) {
            client.send(new StartMove());
            client.state = ReactionTestClient.CURRENTLY_PLAYING;
        } else {
            client.state = ReactionTestClient.READY_TO_START;
            client.send(getPlayersNotReady());
        }
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

    public void removeClient(ReactionTestClient client) {
        clients.remove(client);
        client.removeActionListener(clientListener);
    }

    private void startMoveOrPlayersNotFinished() {
        if (allReadyToPlay()) {
            startNewMove();
        } else {
            playersNotFinished();
        }
    }

    private void playersNotFinished() {
        PlayersNotYetFinished playersNotYetFinished = getPlayersNotReady();
        for (ReactionTestClient clienti : clients) {
            if (clienti.state == ReactionTestClient.READY_TO_START) {
                clienti.send(playersNotYetFinished);
            }
        }
    }

    private void startNewMove() {
        StartMove startMove = new StartMove();
        PlayerStats playerStats = new PlayerStats();

        ReactionTestClient bestPlayerOfLastRound = null;
        for (ReactionTestClient clienti : clients) {
            if (bestPlayerOfLastRound == null){
                bestPlayerOfLastRound = clienti;
            }else {
                if (clienti.time < bestPlayerOfLastRound.time){
                    bestPlayerOfLastRound = clienti;
                }
            }
        }

        bestPlayerOfLastRound.points++;

        for (ReactionTestClient clienti : clients) {
            playerStats.addPlayer(clienti.playerName, clienti.points);
        }

        for (ReactionTestClient clienti : clients) {
            clienti.send(startMove);
            clienti.state = ReactionTestClient.CURRENTLY_PLAYING;

            clienti.send(playerStats);
        }
    }

    /**
     * Object Respresent all Player which are at the moment not Ready
     *
     * @return
     */
    private PlayersNotYetFinished getPlayersNotReady() {
        PlayersNotYetFinished playersNotYetFinished = new PlayersNotYetFinished();
        for (ReactionTestClient clienti : clients) {
            if (clienti.state != ReactionTestClient.READY_TO_START) {
                playersNotYetFinished.playersNotFinished.add(clienti.playerName);
            }
        }
        return playersNotYetFinished;
    }

    /**
     * If player Joins doesn't fill in a name in Textfield he gets one of Default Name List
     *
     * @param name
     */
    private void freeNameIfDefault(String name) {
        if (defaultNamesList.contains(name) && (!availableNames.contains(name))) {
            availableNames.add(name);
        }
    }

    /**
     * Returns Available Name from Default Name List
     *
     * @return
     */
    private String getRandomDefaultName() {
        String name = "";
        if (availableNames.size() > 0) {
            name = availableNames.get(ReactionButtonsPanel.getRandInt(0, availableNames.size()));
            availableNames.remove(name);
        } else {
            if (anonymIndex > 10000000) {
                anonymIndex = 0;
            }
            name = "Anonym " + anonymIndex++;
        }
        return name;
    }

    /**
     * Sets readyToPlay false if Clients are playing
     *
     * @return
     */
    private boolean allReadyToPlay() {
        boolean readyToPlay = true;
        for (ReactionTestClient client : clients) {
            if (client.state != ReactionTestClient.READY_TO_START) {
                readyToPlay = false;
            }
        }
        return readyToPlay;
    }
}
