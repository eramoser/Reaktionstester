import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ReactionTestClient {
    private Socket connection;
    private boolean running = false;

    private DataInputStream receieve;
    private DataOutputStream send;

    private ArrayList<ActionListener> listener = new ArrayList<>();

    public ReactionTestClient(Socket player) {
        this.connection = player;
    }

    public void start() {
        this.running = true;

        try {
            this.receieve = new DataInputStream(connection.getInputStream());
            this.send = new DataOutputStream(connection.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                while (running) {
                    try {
                        if (receieve != null) {
                            String message = receieve.readUTF();

                            // Verarbeite Nachricht
                            notifyListener(message);
                        }
                    } catch (IOException e) {
                    }
                }
            }
        };
        t.start();
    }

    public void send(String message) {
        if (this.send != null) {
            try {
                send.writeUTF(message);
                send.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopp() {
        this.running = false;
    }

    public void notifyListener(String message) {
        for (ActionListener listener : listener) {
            listener.actionPerformed(new ActionEvent(this, 0, message));
        }
    }

    public void addActionListener(ActionListener al) {
        listener.add(al);
    }

}
