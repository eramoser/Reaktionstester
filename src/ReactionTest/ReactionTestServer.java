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



    private void freeNameIfDefault(String name){
        if (defaultNamesList.contains(name)&&(!availableNames.contains(name))){
            availableNames.add(name);
        }
    }

    private String getRandomDefaultName(){
        String name;
        name = availableNames.get(ReactionButtonsPanel.getRandInt(0, availableNames.size()));
        availableNames.remove(name);
        return name;
    }

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

                        if (clients.size() == 1){
                            p.send(new StartMove());
                            p.state = ReactionTestClient.CURRENTLY_PLAYING;
                        }else{
                            p.state = ReactionTestClient.READY_TO_START;
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
