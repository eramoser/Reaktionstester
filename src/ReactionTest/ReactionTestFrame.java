package ReactionTest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;

public class ReactionTestFrame extends JFrame implements Info {

    private ReactionButtonsPanel buttonsPanel;
    private JLabel infoLabel = new JLabel(ReactionTestConstants.START_INFO_TEXT);
    public ReactionTestClient client;

    public ReactionTestFrame() {
        setTitle(ReactionTestConstants.FRAME_TITLE);
        setSize(ReactionTestConstants.WINDOW_WIDTH, ReactionTestConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Open Frame in the middle of Display
        setLayout(new BorderLayout());
        ImageIcon img = new ImageIcon("Logo_440x440.png");
        setIconImage(img.getImage());

        buttonsPanel = new ReactionButtonsPanel(this);

        // TODO: Lines for testing
        try {
            client = new ReactionTestClient(new Socket("localhost", ReactionTestConstants.SERVER_PORT));
            client.start();

            client.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    System.out.println("Message received: " + actionEvent.getActionCommand());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        add(buttonsPanel, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                client.send(Messages);
                e.getWindow().dispose();
            }
        });

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
