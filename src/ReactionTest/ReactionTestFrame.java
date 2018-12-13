package ReactionTest;

import ReactionTest.Messages.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class ReactionTestFrame extends JFrame implements Info {

    private ReactionButtonsPanel buttonsPanel;
    private JLabel infoLabel = new JLabel(ReactionTestConstants.START_INFO_TEXT);
    private JTextField hostname = new JTextField(ReactionTestConstants.SERVER_HOST);
    private JTextField username = new JTextField();
    public ReactionTestClient client;

    public ReactionTestFrame() {
        setTitle(ReactionTestConstants.FRAME_TITLE);
        setSize(ReactionTestConstants.WINDOW_WIDTH, ReactionTestConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Open Frame in the middle of Display
        setLayout(new BorderLayout());
        ImageIcon img = new ImageIcon("Logo_440x440.png");
        setIconImage(img.getImage());


        username.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (client != null) {
                    ClientInfo clientInfo = new ClientInfo();
                    clientInfo.playerName = username.getText();
                    client.send(clientInfo);
                }
            }
        });

        hostname.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                connectToClient(hostname.getText());
            }
        });

        username.setSize(200, 10);

        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.add(new JLabel("Username: "), BorderLayout.WEST);
        usernamePanel.add(username, BorderLayout.CENTER);

        JPanel hostPanel = new JPanel(new BorderLayout());
        hostPanel.add(new JLabel("Hostname: "), BorderLayout.WEST);
        hostPanel.add(hostname, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(hostPanel, BorderLayout.NORTH);
        topPanel.add(usernamePanel, BorderLayout.SOUTH);
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

        add(buttonsPanel, BorderLayout.CENTER);

        connectToClient(ReactionTestConstants.SERVER_HOST);


        setVisible(true);
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
                        if (actionEvent.getSource().getClass().equals(ReactionTestClient.class)) {
                            ReactionTestClient client = (ReactionTestClient) actionEvent.getSource();
                            Object objectReceived = client.getIn();

                            client.send(new ServerLog("Object received: " + objectReceived.getClass().toString()));

                            // Handle if other Players are not Finished
                            if (objectReceived.getClass().equals(PlayersNotYetFinished.class)) {
                                info(objectReceived.toString());
                            }

                            // Handle if other Players are not Finished
                            if (objectReceived.getClass().equals(PlayerStats.class)) {
                                info(objectReceived.toString());
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
                        }
                    }
                });

                buttonsPanel.setClient(client);

                client.start();
            } catch (ConnectException e) {
                error("Connection Refused");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            buttonsPanel.setClient(null);
            info("Can't connect to server");
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
