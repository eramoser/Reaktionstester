import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class ReactionTestFrame extends JFrame implements Info {

    private ReactionButtonsPanel buttonsPanel;
    private JLabel infoLabel = new JLabel(ReactionTestConstants.START_INFO_TEXT);
    private ReactionTestClient client;

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

        setVisible(true);
    }

    @Override
    public void info(String info) {
        infoLabel.setText(info);

        client.send(info);
    }

    @Override
    public void error(String error) {
        info(error);
    }
}
