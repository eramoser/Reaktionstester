package ReactionTest;

import ReactionTest.Messages.ClientInfo;
import ReactionTest.Messages.GameMove;

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

    public ReactionTestServer(int port) {
        this.port = port;
    }

    private ActionListener broadcastListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource().getClass().equals(ReactionTestClient.class)) {
                ReactionTestClient client = (ReactionTestClient) actionEvent.getSource();
                Object objectReceived = client.getIn();

                System.out.println(client.playerName + " sent a Message: ");

                // Handle if Object GameMove is received
                if (objectReceived.getClass().equals(GameMove.class)){
                    System.out.println("Class Game Move received");
                }

                // Handle if Object ClientInfo is received
                if (objectReceived.getClass().equals(ClientInfo.class)){
                    ClientInfo clientInfo = (ClientInfo) objectReceived;
                    if (client.playerName != null) {
                        client.playerName = clientInfo.playerName;
                    }
                }

                // sende nachricht an alle clients
                // die dem server derzeit bekannt sind
                for (ReactionTestClient clienti : clients) {
                    if (!clienti.equals(actionEvent.getSource())) {
                        client.send((Serializable) objectReceived);

                    }
                }
                System.out.println("Object received: " + objectReceived.getClass().toString());
            }else {
                System.out.println("Received Message from other class than ReactionTestClient");
            }
        }
    };

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
                        System.out.println("Client added");
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
