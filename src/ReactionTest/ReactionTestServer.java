package ReactionTest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
            // sende nachricht an alle clients
            // die dem server derzeit bekannt sind
            for (ReactionTestClient client:clients) {
                if(!client.equals(actionEvent.getSource())){
                    client.send(actionEvent.getActionCommand());

                }
            }
            System.out.println("Message recieved: " + actionEvent.getActionCommand());
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
