package ReactionTest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ReactionTestClient {
    private Socket connection;
    private boolean running = false;
    public String playerName = "Anonymous";
    public float time;
    public int state;
    public static final int READY_TO_START = 0;
    public static final int CURRENTLY_PLAYING = 1;

    private ObjectInputStream receieve;
    private ObjectOutputStream send;

    private Object in;

    private ArrayList<ActionListener> listener = new ArrayList<>();

    public ReactionTestClient(Socket player) {
        this.connection = player;
    }



    public void start() {
        this.running = true;

        try {
            this.send = new ObjectOutputStream(new DataOutputStream(connection.getOutputStream()));
            this.receieve = new ObjectInputStream(new DataInputStream(connection.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                while (running) {
                    try {
                        if (receieve != null) {
                            in = receieve.readObject();

                            // Verarbeite Nachricht
                            notifyListener(ReactionTestClient.this);
                        }
                    } catch (IOException e) {
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    public Object getIn(){
        return in;
    }

    public void send(Serializable object) {
        if (this.send != null) {
            try {
                send.writeObject(object);
                send.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopp() {
        this.running = false;
    }

    public void notifyListener(Object object) {
        for (ActionListener listener : listener) {
            listener.actionPerformed(new ActionEvent(object, 0, object.toString()));
        }
    }

    public void addActionListener(ActionListener al) {
        listener.add(al);
    }

}
