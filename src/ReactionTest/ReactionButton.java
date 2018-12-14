package ReactionTest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Represents a button which can be enabled or disabled
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

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


    /**
     * Set Background to from ReactionButton in DISABLED_COLOR (Default: Black)
     */
    public void disableReaction(){
        this.setBackground(ReactionTestConstants.DISABLED_COLOR);
        this.setEnabled(false);
    }

    /**
     * Set Background to from ReactionButton in ENABLED_COLOR (Default: Green)
     */
    public void enableReaction(){
        this.setBackground(ReactionTestConstants.ENABLED_COLOR);
        this.setEnabled(true);
    }
}

