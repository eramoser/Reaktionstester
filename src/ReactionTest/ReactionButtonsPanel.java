package ReactionTest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This Panel contains a number of Reaction Buttons
 */
public class ReactionButtonsPanel extends JPanel {

    private ReactionButton[][] buttons = new ReactionButton[ReactionTestConstants.ROWS_BUTTONS][ReactionTestConstants.COLS_BUTTONS];
    private long currentStartTime = 0;
    private long currentEndTime = 0;
    private int currentButtons = 0;
    private float timeBeforeStart = 0;
    private Info info;

    public ReactionButtonsPanel(Info info) {
        this.info = info;
        this.setLayout(new GridLayout(ReactionTestConstants.ROWS_BUTTONS, ReactionTestConstants.COLS_BUTTONS));
        for (int i = 0; i< ReactionTestConstants.ROWS_BUTTONS; i++){
            for (int j = 0; j< ReactionTestConstants.COLS_BUTTONS; j++){
                ReactionButton button = new ReactionButton(i, j);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        ReactionButtonsPanel.this.buttonChanged(actionEvent);
                    }
                });
                buttons[i][j] = button;
                add(button);
            }
        }

        startWithRandomButtons();
    }

    /**
     * Reacts if one Button was clicked.
     */
    private void buttonChanged(ActionEvent actionEvent){
        if (numberOfEnabledButtons((ReactionButton) actionEvent.getSource()) == 0){
            currentEndTime = System.currentTimeMillis();

            info.info("Time: " + getTimeDuration() + " Time before Start: " + timeBeforeStart + " Number of Buttons: " + currentButtons);
            info.getClient().send(Messages.getFinishedMoveMessage(getTimeDuration()));
            startWithRandomButtons();
        }
    }

    private float getTimeDuration(){
        return (float)((currentEndTime - currentStartTime)/1000.0);
    }

    /**
     *
     * @param excludeButton this button should be ignored if enabled (this is useful, if used in the actionListener of the button,
     *                      because the button may not be set disabled upon call of the Listener
     * @return the number of currently Enabled Buttons
     */
    private int numberOfEnabledButtons(ReactionButton excludeButton){
        int counter = 0;
        for (ReactionButton[] buttons:this.buttons) {
            for (ReactionButton button:buttons) {
                if (button.isEnabled() && !(excludeButton==button)){
                    ++counter;
                }
            }
        }
        return counter;
    }

    private int numberOfEnabledButtons(){
        return numberOfEnabledButtons(null);
    }

    private float getRandSeconds(){
        return ReactionTestConstants.MIN_SECONDS + (float)Math.random() * (ReactionTestConstants.MAX_SECONDS - ReactionTestConstants.MIN_SECONDS);
    }

    private ReactionButton[] getRandButtons(){
        // Number of max enabled Buttons can't be higher than number of Buttons
        int max = 0;
        if (getNumberOfButtons() < ReactionTestConstants.MAX_RAND_BUTTONS){
            max = getNumberOfButtons();
        }else {
            max = ReactionTestConstants.MAX_RAND_BUTTONS;
        }

        int numOfButtons = getRandInt(ReactionTestConstants.MIN_RAND_BUTTONS, max+1);

        currentButtons = numOfButtons;

        ReactionButton[] reactionButtons = new ReactionButton[numOfButtons];
        for (int i = 0; i<numOfButtons;i++) {
            reactionButtons[i] = getRandButton(reactionButtons);
        }

        return reactionButtons;
    }

    /**
     * Returns a random Button which is not included in the given Array
     * @param reactionButtons Array where the buttons are which should be excluded
     * @return
     */
    private ReactionButton getRandButton(ReactionButton[] reactionButtons){
        Vector buttonVector = new Vector<>();
        for (ReactionButton button:reactionButtons) {
            buttonVector.add(button);
        }
        ReactionButton button = null;
        do {
            button = buttons[getRandInt(0, ReactionTestConstants.ROWS_BUTTONS)][getRandInt(0, ReactionTestConstants.COLS_BUTTONS)];
        }while (buttonVector.contains(button));
        return button;
    }
    
    private int getRandInt(int min, int maxExcluded){
        return ThreadLocalRandom.current().nextInt(min, maxExcluded);
    }

    /**
     * @return Number of Buttons on the Panel
     */
    private int getNumberOfButtons(){
        return ReactionTestConstants.COLS_BUTTONS* ReactionTestConstants.ROWS_BUTTONS;
    }

    public void startWithRandomButtons(){
        ReactionButton[] reactionButtons = getRandButtons();

        long timeMs = (long) (getRandSeconds()*1000.0);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (ReactionButton button:reactionButtons) {
                    button.enableReaction();
                }
                currentStartTime = System.currentTimeMillis();
            }
        }, timeMs);
    }

    public void start(ReactionButton[] buttons, long timeMs){
        // TODO: Implement to start with same Buttons as other PC
    }

    private void enableButton(int row, int col){
        buttons[row][col].enableReaction();
    }
}
