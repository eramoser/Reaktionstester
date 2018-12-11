package ReactionTest;

import ReactionTest.Messages.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

public class ReactionTestFrame extends JFrame implements Info {

    private ReactionButtonsPanel buttonsPanel;
    private JLabel infoLabel = new JLabel(ReactionTestConstants.START_INFO_TEXT);
    private JTextField username = new JTextField("");
    public ReactionTestClient client;

    public ReactionTestFrame() {
        setTitle(ReactionTestConstants.FRAME_TITLE);
        setSize(ReactionTestConstants.WINDOW_WIDTH, ReactionTestConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Open Frame in the middle of Display
        setLayout(new BorderLayout());
        ImageIcon img = new ImageIcon("Logo_440x440.png");
        setIconImage(img.getImage());

        // TODO: Lines for testing
        try {
            client = new ReactionTestClient(new Socket(ReactionTestConstants.SERVER_HOST, ReactionTestConstants.SERVER_PORT));


            client.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if (actionEvent.getSource().getClass().equals(ReactionTestClient.class)) {
                        ReactionTestClient client = (ReactionTestClient) actionEvent.getSource();
                        Object objectReceived = client.getIn();

                        client.send(new ServerLog("Object received: " + objectReceived.getClass().toString()));

                        // Handle if other Players are not Finished
                        if (objectReceived.getClass().equals(PlayersNotYetFinished.class)){
                            System.out.println("PlayersNotYetFinished received: "  + username.getText());
                            info(objectReceived.toString());
                        }

                        // Handle if other Players are not Finished
                        if (objectReceived.getClass().equals(PlayerStats.class)){
                            info(objectReceived.toString());
                        }

                        // Handle Client Info (New Player Name received)
                        if (objectReceived.getClass().equals(ClientInfo.class)){
                            ClientInfo clientInfo = (ClientInfo)objectReceived;
                            if (clientInfo.playerName != null){
                                username.setText(clientInfo.playerName);
                            }
                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonsPanel = new ReactionButtonsPanel(this);
        //buttonsPanel.initialStart();

        username.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.playerName = username.getText();
                client.send(clientInfo);
            }
        });
        username.setSize(200, 10);

        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.add(new JLabel("Username: "), BorderLayout.WEST);
        usernamePanel.add(username, BorderLayout.CENTER);
        add(usernamePanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                client.send(new Disconnect());
                e.getWindow().dispose();
            }
        });

        client.start();

        setVisible(true);
    }

    @Override
    public ReactionTestClient getClient() {
        return client;
    }

    @Override
    public void info(String info) {
        infoLabel.setText(info);
    }

    @Override
    public void error(String error) {
        info(error);
    }
}
