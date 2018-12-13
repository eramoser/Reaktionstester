package ReactionTest;

import ReactionTest.Messages.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ReactionTestServer {
    private int port;
    private boolean running = false;
    private ArrayList<ReactionTestClient> clients = new ArrayList<>();

    private int anonymIndex = 0;

    private String[] defaultPlayerNames = {"\"Nur-Kurz-aufs-Klo\" Jack", "Kopfrechnengenie Wenzl","Tesla Jacki","Dirty JackSchabrack","\"Des-Wochnend-dring-I-nix\" Waunz"};
    private ArrayList<String> defaultNamesList = new ArrayList<>();
    private ArrayList<String> availableNames = new ArrayList<>();

    public ReactionTestServer(int port) {
        this.port = port;
        for (String name:defaultPlayerNames) {
            availableNames.add(name);
            defaultNamesList.add(name);
        }
    }

    private ActionListener broadcastListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource().getClass().equals(ReactionTestClient.class)) {
                ReactionTestClient client = (ReactionTestClient) actionEvent.getSource();
                Object objectReceived = client.getIn();

                // Handle if Object GameMove is received
                if (objectReceived.getClass().equals(GameMove.class)){
                    client.state = ReactionTestClient.READY_TO_START;
                    client.time = ((GameMove)objectReceived).time;

                    if (allReadyToPlay()) {
                        StartMove startMove = new StartMove();
                        PlayerStats playerStats = new PlayerStats();

                        for (ReactionTestClient clienti : clients) {
                            playerStats.addPlayer(clienti.playerName, clienti.time);
                        }

                        for (ReactionTestClient clienti : clients) {
                            clienti.send(startMove);
                            clienti.state = ReactionTestClient.CURRENTLY_PLAYING;

                            clienti.send(playerStats);
                        }
                    }else {
                        PlayersNotYetFinished playersNotYetFinished = getPlayersNotReady();


                        for (ReactionTestClient clienti:clients) {
                            if (clienti.state == ReactionTestClient.READY_TO_START){
                                clienti.send(playersNotYetFinished);
                            }
                        }
                    }
                }

                // Handle if Object ClientInfo is received
                if (objectReceived.getClass().equals(ClientInfo.class)){
                    ClientInfo clientInfo = (ClientInfo) objectReceived;
                    if (clientInfo.playerName != null) {
                        freeNameIfDefault(client.playerName);
                        client.playerName = clientInfo.playerName;
                    }
                }

                // Handle if Client Disconnects
                if (objectReceived.getClass().equals(Disconnect.class)){
                    clients.remove(client);
                    client.stop();
                    System.out.println("Removed Player: " + client.playerName);
                    freeNameIfDefault(client.playerName);
                }

                // Handle Server Log
                if (objectReceived.getClass().equals(ServerLog.class)){
                    ServerLog serverLog = (ServerLog)objectReceived;
                    System.out.println("Message from " + client.playerName + ": " + serverLog.message);
                }

                // sende nachricht an alle clients
                // die dem server derzeit bekannt sind
                for (ReactionTestClient clienti : clients) {

                }
            }else {
                System.out.println("Received Message from other class than ReactionTestClient");
            }
        }
    };

    /**
     * Object Respresent all Player which are at the moment not Ready
     * @return
     */
    private PlayersNotYetFinished getPlayersNotReady(){
        PlayersNotYetFinished playersNotYetFinished = new PlayersNotYetFinished();
        for (ReactionTestClient clienti:clients) {
            if (clienti.state != ReactionTestClient.READY_TO_START){
                playersNotYetFinished.playersNotFinished.add(clienti.playerName);
            }
        }
        return playersNotYetFinished;
    }

    /**
     * If player Joins doesn't fill in a name in Textfield he gets one of Default Name List
     * @param name
     */
    private void freeNameIfDefault(String name){
        if (defaultNamesList.contains(name)&&(!availableNames.contains(name))){
            availableNames.add(name);
        }
    }

    /**
     * Returns Available Name from Default Name List
     * @return
     */
    private String getRandomDefaultName(){
        String name = "";
        if (availableNames.size() > 0) {
            name = availableNames.get(ReactionButtonsPanel.getRandInt(0, availableNames.size()));
            availableNames.remove(name);
        }else {
            if (anonymIndex > 10000000){
                anonymIndex = 0;
            }
            name = "Anonym " + anonymIndex++;
        }
        return name;
    }

    /**
     * Sets readyToPlay false if Clients are playing
     * @return
     */
    private boolean allReadyToPlay(){
        boolean readyToPlay = true;
        for (ReactionTestClient client:clients) {
            if (client.state != ReactionTestClient.READY_TO_START){
                readyToPlay = false;
            }
        }
        return readyToPlay;
    }

    public void start(){
        this.running = true;

        Thread serverThread = new Thread(){

            @Override
            public void run() {
                try(ServerSocket server = new ServerSocket(port)){
                    while (running){
                        // Client verbindet sich auf den Server
                        Socket client = server.accept();

                        // Weitere Aktionen mit dem Client
                        // ...

                        ReactionTestClient p = new ReactionTestClient(client);
                        p.addActionListener(broadcastListener);
                        clients.add(p);
                        p.start();
                        p.playerName = getRandomDefaultName();
                        p.send(new ClientInfo(p.playerName));

                        System.out.println("Client added: " + p.playerName);

                        if (clients.size() == 1){
                            p.send(new StartMove());
                            p.state = ReactionTestClient.CURRENTLY_PLAYING;
                        }else{
                            p.state = ReactionTestClient.READY_TO_START;
                            p.send(getPlayersNotReady());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        serverThread.run();
    }

    public void stopp(){
        this.running = false;
    }

}
