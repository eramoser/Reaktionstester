package ReactionTest;

import ReactionTest.Messages.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ReactionTestServer {
    private int port;
    private boolean running = false;
    private ArrayList<ServerGame> games = new ArrayList<>();
    private ServerGame defaultGame = new ServerGame("1");
    private ArrayList<ReactionTestClient> clients = new ArrayList<>();
    private ActionListener clientListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource().getClass().equals(ReactionTestClient.class)) {
                ReactionTestClient client = (ReactionTestClient) actionEvent.getSource();
                Object objectReceived = client.getIn();



                // Handle Server Log
                if (objectReceived.getClass().equals(ServerLog.class)){
                    ServerLog serverLog = (ServerLog)objectReceived;
                    System.out.println("Log: Message from " + client.playerName + ": " + serverLog.message);
                }

            }else {
                System.out.println("Received Message from other class than ReactionTestClient");
            }
        }
    };

    public ReactionTestServer(int port) {
        this.port = port;

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
                        p.addActionListener(clientListener);

                        defaultGame.addClient(p);
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
