package ReactionTest.Client;

import ReactionTest.Communication.ClientObjectPair;
import ReactionTest.Communication.ReactionTestClient;
import ReactionTest.General.Log;
import ReactionTest.Communication.Messages.*;
import ReactionTest.General.ReactionTestConstants;

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

public class Client extends JFrame {

    private ReactionButtonsPanel buttonsPanel;
    private JLabel infoLabel = new JLabel(ReactionTestConstants.START_INFO_TEXT);
    private JTextField hostname = new JTextField(ReactionTestConstants.SERVER_HOST);
    private JTextField username = new JTextField();
    private JTextField gameId = new JTextField();
    private JTextArea playerStats = new JTextArea(ReactionTestConstants.PLAYER_STATS_INIT);
    private JTextArea chatArea = new JTextArea();
    private JTextField chatInput = new JTextField(ReactionTestConstants.DEFAULT_CHAT_MESSAGE);
    private JScrollPane chatScrollPane;
    private Timer reconnectTimer = new Timer();
    private static final int RECONNECT_DELAY = 500; // 500ms
    public ReactionTestClient client;

    public Client() {
        System.out.println("Falls bei Game nicht automatisch eine Default Zahl eingegeben wurde, muss nochmal die Adresse hostname bestätigt werden");
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

        username.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
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

        hostname.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
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
        gameId.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                gameChanged();
            }
        });

        chatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                client.send(new ChatMessage(chatInput.getText()));
                chatInput.setText("");
            }
        });

        JPanel topLeftPanel = new JPanel(new GridLayout(3, 1));
        JPanel topRightPanel = new JPanel(new GridLayout(3, 2));
        topLeftPanel.add(new JLabel("Username: "));
        topRightPanel.add(username);
        topLeftPanel.add(new JLabel("Hostname: "));
        topRightPanel.add(hostname);
        topLeftPanel.add(new JLabel("Game: "));
        topRightPanel.add(gameId);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(topLeftPanel, BorderLayout.WEST);
        topPanel.add(topRightPanel, BorderLayout.CENTER);
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

        JPanel chat = new JPanel(new BorderLayout());
        chatArea.setEditable(false);
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chat.add(chatScrollPane, BorderLayout.CENTER);
        chat.add(chatInput, BorderLayout.SOUTH);

        chatInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if (chatInput.getText().equals(ReactionTestConstants.DEFAULT_CHAT_MESSAGE)){
                    chatInput.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (chatInput.getText().equals("")){
                    chatInput.setText(ReactionTestConstants.DEFAULT_CHAT_MESSAGE);
                }
            }
        });

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playerStats, chat);

        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonsPanel, rightSplitPane);
        jSplitPane.setDividerLocation(ReactionTestConstants.DIVIDER_LOCATION);
        add(jSplitPane, BorderLayout.CENTER);

        rightSplitPane.setDividerLocation(ReactionTestConstants.WINDOW_HEIGHT/3);

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
                    client.send(new ClientInfo(username.getText()));
                    buttonsPanel.disableAllButtons();
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
                reconnectTimer.cancel();

            try {
                if (client != null) {
                    client.send(new Disconnect());
                    client.stop();
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
                                Client.this.playerStats.setText(playerStats.toString());
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

                            // Handle Chat received
                            if (objectReceived.getClass().equals(Chat.class)) {
                                Chat chat = (Chat) objectReceived;

                                chatArea.setText(chat.toString());

                                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                                vertical.setValue( vertical.getMaximum() );

                                repaint();
                            }

                            // Handle StartMove
                            if (objectReceived.getClass().equals(StartMove.class)) {
                                info(" ");
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
            reconnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    connectToClient(hostname);
                }
            }, RECONNECT_DELAY);
        }
    }

    /**
     * Sets Text in the Bottom
     *
     * @param info
     */
    public void info(String info) {
        infoLabel.setText(info);
    }


    public void error(String error) {
        info(error);
    }

    public static void main(String[] args) {
        new Client();
    }
}
