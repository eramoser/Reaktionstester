package ReactionTest;

import ReactionTest.Messages.Disconnect;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class ReactionTestClient {
    private Socket connection;
    private boolean running = false;
    public String playerName = "Anonymous";
    public float time;
    public int state;
    public static final int READY_TO_START = 0;
    public static final int CURRENTLY_PLAYING = 1;

    public static final int MAX_CONNECT_TIMEOUT_DELAY = 200; // in ms

    private ObjectInputStream receieve;
    private ObjectOutputStream send;

    private Object in;

    private ArrayList<ActionListener> listener = new ArrayList<>();

    public ReactionTestClient(Socket player) {
        this.connection = player;
    }

    public ReactionTestClient(String hostname) throws IOException {
        this.connection = new Socket(hostname, ReactionTestConstants.SERVER_PORT);
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
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    /**
     * Returns sent Object from Server
     * @return
     */
    public Object getIn(){
        return in;
    }

    /**
     * Sends Object from Client to Server
     * @param object
     */
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

    /**
     * Stops the client
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Notifys all Listeners from client
     * @param object
     */
    public void notifyListener(Object object) {
        for (ActionListener listener : listener) {
            listener.actionPerformed(new ActionEvent(object, 0, object.toString()));
        }
    }

    /**
     * Adds new listener to Listenerslist
     * @param al
     */
    public void addActionListener(ActionListener al) {
        listener.add(al);
    }

    /**
     * Function returns if the connection to a ReactionTestServer is possible (max. time to connect is delayMs)
     * @param hostName
     * @param delayMs
     * @return
     */
    public static boolean canConnect(String hostName, long delayMs){
        Runnable handShakeTask = new Runnable() {
            @Override
            public void run() {
                ReactionTestClient client = null;
                try {
                    client = new ReactionTestClient(hostName);
                    client.start();
                    client.send(new Disconnect());
                    client.stop();
                } catch (IOException e) {
                }

            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        try {
            Future<Boolean> future = executorService.submit(handShakeTask, true);
            executorService.shutdown();
            return future.get(delayMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
        } catch (InterruptedException | ExecutionException e) {
            // handle exception
        }
        return false;
    }

    /**
     * Function returns if the connection to a ReactionTestServer is possible (within a specific time)
     * @param hostName
     * @return
     */
    public static boolean canConnect(String hostName){
        return canConnect(hostName, MAX_CONNECT_TIMEOUT_DELAY);
    }



}
