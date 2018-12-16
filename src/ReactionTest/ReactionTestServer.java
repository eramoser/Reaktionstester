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

                Log.log("Server received (from " + client.playerName + "): Object: " + objectReceived.getClass());

                // Handle ChangeGame
                if (objectReceived.getClass().equals(ChangeGame.class)) {
                    ChangeGame changeGame = (ChangeGame) objectReceived;

                    if (!changeGame.gameId.equals(client.game.getGameId())) {

                        client.game.removeClient(client);

                        if (ServerGame.isGameIdContained(games, changeGame.gameId)) {
                            ServerGame.getGameWithId(games, changeGame.gameId).addClient(client);
                        } else {
                            ServerGame game = new ServerGame(changeGame.gameId);
                            games.add(game);
                            game.addClient(client);
                        }
                    }
                }

                // Handle Server Log
                if (objectReceived.getClass().equals(ServerLog.class)){
                    ServerLog serverLog = (ServerLog)objectReceived;
                    Log.log("Log: Message from " + client.playerName + ": " + serverLog.message);
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

                        p.start();
                        defaultGame.addClient(p);
                        p.send(new ChangeGame(defaultGame.getGameId()));

                        Log.log("Add client: " + p.playerName);
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
