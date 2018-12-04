package ReactionTest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class GameConnection {
    // Connection object (Server -> Client or Client -> Server)
    private Socket socket;

    // receive data
    private DataInputStream receive;
    // send data
    private DataOutputStream send;

    private String username = "";

    // list containing all listeners
    private ArrayList<ActionListener> listeners = new ArrayList<>();

    private boolean running = false;

    public GameConnection(Socket connection) {
        socket = connection;

        try {
            // initialize the streams
            receive = new DataInputStream(socket.getInputStream());
            send = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            GameConnection.this.stop();
            GameConnection.this.close();
        }
    }

    /**
     * Start the connection
     */
    public void start() {
        // boolean variable
        // --> mark the server as running
        // variable used in the while loop in our thread
        running = true;

        // anonymous implementation of the thread
        // we need to override our run method
        new Thread() {
            @Override
            public void run() {
                // loop for managing our incoming connections
                while (running) {
                    try {
                        // Receive messages from the other side
                        String message = receive.readUTF();

                        if (username.length() <= 0) {
                            username = message;
                        } else {
                            // Send message to all Listeners
                            notifyListeners(message);
                        }
                    } catch (IOException e) {
                        GameConnection.this.stop();
                        GameConnection.this.close();
                    }
                }
                // at this point the running variable is false
                // stop() was called
            }
        }.start();
    }

    /**
     * Checks if the connection is established
     *
     * @return isConnected
     */
    public boolean isConnected() {
        return running;
    }

    /**
     * stop the connection
     */
    public void stop() {
        running = false;
    }

    /**
     * close all in- and outputstreams
     * close the socket connection
     */
    public void close() {
        try {
            send.close();
            receive.close();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send message to the other side
     *
     * @param message the message to send
     */
    public void sendMessage(String message) {
        try {
            send.writeUTF(message);
            send.flush();
        } catch (IOException e) {
            GameConnection.this.stop();
            GameConnection.this.close();
        }
    }

    /**
     * notify all listeners: There is a new message
     *
     * @param message
     */
    private void notifyListeners(String message) {
        for (ActionListener l : listeners) {
            l.actionPerformed(new ActionEvent(this, 0, message));
        }
    }

    /**
     * Add a new Listener to our List
     *
     * @param l
     */
    public void addMessageListener(ActionListener l) {
        listeners.add(l);
    }

    public String getUsername() {
        return username;
    }
}
