package ReactionTest.Communication;

import ReactionTest.General.Log;
import ReactionTest.Communication.Messages.ChangeGame;
import ReactionTest.Communication.Messages.Disconnect;
import ReactionTest.General.ReactionTestConstants;
import ReactionTest.Server.ServerGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ReactionTestClient {
    private Socket connection;
    private boolean running = false;
    public String playerName = "Anonymous";
    public float time = 0;
    public int points = 0;
    public int state;
    public ServerGame game;

    public ReactionTestClient(Socket player) {
        this.connection = player;
    }
    public static final int READY_TO_START = 0;

    public static final int CURRENTLY_PLAYING = 1;

    public static final int MAX_CONNECT_TIMEOUT_DELAY = 200; // in ms
    private ObjectInputStream receieve;

    private ObjectOutputStream send;

    private Object in;
    private boolean arrayInUse = false;
    private ArrayList<ActionListener> listener = new ArrayList<>();

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

                            Serializable object = (Serializable) receieve.readObject();




                            Log.log("Received Message: " + playerName + " : " + object.getClass().toString());
                            if (object.getClass().equals(ChangeGame.class)){
                                Log.log("Changegame");
                            }

                            // Verarbeite Nachricht
                            waitForArray();
                            keyArray();
                            notifyListener(object);
                        }
                    } catch (StreamCorruptedException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (SocketException e) {
                        System.out.println("Socket of Client: " + playerName + " not working -> will be disconnected.");

                        waitForArray();
                        keyArray();
                        unkeyArray();
                        notifyListener(new Disconnect());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    unkeyArray();
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
        Log.log("Object sent: " + object.getClass().toString());
        if (this.send != null) {
            try {
                send.writeObject(object);
                send.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void keyArray(){
        arrayInUse = true;
        Log.log("Key");
    }

    private void unkeyArray(){
        arrayInUse = false;
        Log.log("Unkey");
    }

    private void waitForArray(){
        boolean active = true;
        while (arrayInUse&&active){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
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
    public void notifyListener(Serializable object) {
        in = object;
        for (ActionListener listener : listener) {
            listener.actionPerformed(new ActionEvent(new ClientObjectPair(this, object), 0, object.toString()));
        }
    }

    /**
     * Adds new listener to Listenerslist
     * @param al
     */
    public void addActionListener(ActionListener al) {
        Log.log("Wait for Array: variable value: " + arrayInUse);
        if (arrayInUse){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitForArray();
                    keyArray();
                    listener.add(al);
                    unkeyArray();
                }
            }).start();
        }else {
            listener.add(al);
        }
    }

    public void removeActionListener(ActionListener al) {
        if (arrayInUse){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitForArray();
                    keyArray();
                    listener.remove(al);
                    unkeyArray();
                }
            }).start();
        }else {
            listener.remove(al);
        }
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
                } catch (SocketException | UnknownHostException e) {
                    // Wait, so that is seems like the connection is not working
                    try {
                        Thread.sleep((1000 + delayMs) * 10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
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
