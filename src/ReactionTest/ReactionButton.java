package ReactionTest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Represents a button which can be enabled or disabled
 * Reajetonasdf
 */
public class ReactionButton extends JButton {
    private int row;
    private int col;
    public ReactionButton(int row, int col) {
        super();
        this.row = row;
        this.col = col;

        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                disableReaction();
            }
        });
        disableReaction();
    }
    public void disableReaction(){
        this.setBackground(ReactionTestConstants.DISABLED_COLOR);
        this.setEnabled(false);
    }

    public void enableReaction(){
        this.setBackground(ReactionTestConstants.ENABLED_COLOR);
        this.setEnabled(true);
    }
}

