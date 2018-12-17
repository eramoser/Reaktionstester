package ReactionTest;

import ReactionTest.Messages.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ReactionTestFrame extends JFrame implements Info {

    private ReactionButtonsPanel buttonsPanel;
    private JLabel infoLabel = new JLabel(ReactionTestConstants.START_INFO_TEXT);
    private JTextField hostname = new JTextField(ReactionTestConstants.SERVER_HOST);
    private JTextField username = new JTextField();
    private JTextField gameId = new JTextField();
    private JTextArea playerStats = new JTextArea(ReactionTestConstants.PLAYER_STATS_INIT);
    public ReactionTestClient client;

    public ReactionTestFrame() {
        setTitle(ReactionTestConstants.FRAME_TITLE);
        setSize(ReactionTestConstants.WINDOW_WIDTH, ReactionTestConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Open Frame in the middle of Display
        setLayout(new BorderLayout());
        ImageIcon img = new ImageIcon("Logo_440x440.png");
        setIconImage(img.getImage());


        username.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                usernameChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                usernameChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                usernameChanged();
            }
        });

        hostname.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hostNameChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hostNameChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                hostNameChanged();
            }
        });

        gameId.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                gameChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                gameChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                gameChanged();
            }
        });

        JPanel topPanel = new JPanel(new GridLayout(3, 2));
        topPanel.add(new JLabel("Username: "));
        topPanel.add(username);
        topPanel.add(new JLabel("Hostname: "));
        topPanel.add(hostname);
        topPanel.add(new JLabel("Game: "));
        topPanel.add(gameId);
        add(topPanel, BorderLayout.NORTH);
        add(infoLabel, BorderLayout.SOUTH);



        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.send(new Disconnect());
                    e.getWindow().dispose();
                }
            }
        });

        buttonsPanel = new ReactionButtonsPanel();

        playerStats.setEditable(false);

        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonsPanel, playerStats);
        jSplitPane.setDividerLocation(ReactionTestConstants.DIVIDER_LOCATION);
        add(jSplitPane, BorderLayout.CENTER);

        connectToClient(ReactionTestConstants.SERVER_HOST);

        setVisible(true);
    }

    private void gameChanged() {
        String gameNamePrev = this.gameId.getText();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (gameNamePrev.equals(gameId.getText())) {
                    client.send(new ChangeGame(gameId.getText()));
                }
            }
        }, 1000);
    }

    private void hostNameChanged() {
        String hostnamePrev = this.hostname.getText();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hostnamePrev.equals(hostname.getText())) {

                    connectToClient(hostname.getText());

                }
            }
        }, 1000);

    }

    private void usernameChanged() {
        if (client != null) {
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.playerName = this.username.getText();
            client.send(clientInfo);
        }
    }

    private void connectToClient(String hostname) {
        if (ReactionTestClient.canConnect(hostname)) {
            try {
                if (client != null) {
                    client.send(new Disconnect());
                }
                client = new ReactionTestClient(new Socket(hostname, ReactionTestConstants.SERVER_PORT));


                client.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        ClientObjectPair pair = (ClientObjectPair)actionEvent.getSource();
                        if (pair.client.getClass().equals(ReactionTestClient.class)) {

                            ReactionTestClient client = pair.client;
                            Serializable objectReceived = pair.object;

                            client.send(new ServerLog("Received Message from Server: " + objectReceived.getClass().toString()));
                            Log.log("Received Message from Server: " + objectReceived.getClass().toString());

                            // Handle if other Players are not Finished
                            if (objectReceived.getClass().equals(PlayersNotYetFinished.class)) {
                                info(objectReceived.toString());
                            }

                            // Handle if other Players are not Finished
                            if (objectReceived.getClass().equals(PlayerStats.class)) {
                                PlayerStats playerStats = (PlayerStats)objectReceived;
                                ReactionTestFrame.this.playerStats.setText(playerStats.toString());
                            }

                            // Handle Client Info (New Player Name received)
                            if (objectReceived.getClass().equals(ClientInfo.class)) {
                                ClientInfo clientInfo = (ClientInfo) objectReceived;

                                if (username.getText().length() <= 0) {
                                    if (clientInfo.playerName != null) {
                                        username.setText(clientInfo.playerName);
                                    }
                                } else {
                                    client.send(new ClientInfo(username.getText()));
                                }
                            }

                            // Handle Change Game (New Game Name received)
                            if (objectReceived.getClass().equals(ChangeGame.class)) {
                                ChangeGame changeGame = (ChangeGame) objectReceived;

                                if (gameId.getText().length() <= 0) {
                                    if (changeGame.gameId != null) {
                                        gameId.setText(changeGame.gameId);
                                    }
                                } else {
                                    client.send(new ClientInfo(gameId.getText()));
                                }
                            }
                        }
                    }
                });

                buttonsPanel.setClient(client);

                client.start();

                info("Successfully connected to: " + hostname);
            } catch (ConnectException e) {
                error("Connection Refused");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (client != null) {
                client.send(new Disconnect());
            }
            buttonsPanel.setClient(null);
            error("Can't connect to server");
        }
    }

    @Override
    public ReactionTestClient getClient() {
        return client;
    }

    /**
     * Sets Text in the Bottom
     *
     * @param info
     */
    @Override
    public void info(String info) {
        infoLabel.setText(info);
    }


    @Override
    public void error(String error) {
        info(error);
    }
}
