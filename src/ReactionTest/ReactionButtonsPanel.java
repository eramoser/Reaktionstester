package ReactionTest;

import ReactionTest.Messages.ClientInfo;
import ReactionTest.Messages.GameMove;
import ReactionTest.Messages.StartMove;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
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
    private Timer timer;
    private ReactionTestClient client;

    public ReactionButtonsPanel() {
        this.setLayout(new GridLayout(ReactionTestConstants.ROWS_BUTTONS, ReactionTestConstants.COLS_BUTTONS));
        for (int i = 0; i < ReactionTestConstants.ROWS_BUTTONS; i++) {
            for (int j = 0; j < ReactionTestConstants.COLS_BUTTONS; j++) {
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

    }

    public void setClient(ReactionTestClient client){
        this.client = client;
        if (client != null) {
            this.client.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ClientObjectPair pair = (ClientObjectPair)e.getSource();
                    ReactionTestClient client = pair.client;
                    Serializable objectReceived = pair.object;
                    if (objectReceived.getClass().equals(StartMove.class)) {
                        StartMove startMove = (StartMove) objectReceived;
                        ReactionButtonsPanel.this.start(startMove);
                    }
                }
            });
        }
        disableAllButtons();
    }

    public void initialStart(){
        startWithRandomButtons();
    }

    /**
     * Reacts if one Button was clicked.
     */
    private void buttonChanged(ActionEvent actionEvent) {
        if (numberOfEnabledButtons((ReactionButton) actionEvent.getSource()) == 0) {
            currentEndTime = System.currentTimeMillis();

            GameMove gameMove = new GameMove();
            gameMove.time = getTimeDuration();
            if (client != null) {
                client.send(gameMove);
            }
            //startWithRandomButtons();
        }
    }

    private float getTimeDuration() {
        return (float) ((currentEndTime - currentStartTime) / 1000.0);
    }

    /**
     * @param excludeButton this button should be ignored if enabled (this is useful, if used in the actionListener of the button,
     *                      because the button may not be set disabled upon call of the Listener
     * @return the number of currently Enabled Buttons
     */
    private int numberOfEnabledButtons(ReactionButton excludeButton) {
        int counter = 0;
        for (ReactionButton[] buttons : this.buttons) {
            for (ReactionButton button : buttons) {
                if (button.isEnabled() && !(excludeButton == button)) {
                    ++counter;
                }
            }
        }
        return counter;
    }

    /**
     * Same function as above without ignore the last Button
     * @return
     */
    private int numberOfEnabledButtons() {
        return numberOfEnabledButtons(null);
    }

    /**
     * Generates the Seconds of Waiting Time until next Round starts. (Between MAX_SECONDS and MIN_SECONDS)
      * @return
     */
    public static float getRandSeconds() {
        return ReactionTestConstants.MIN_SECONDS + (float) Math.random() * (ReactionTestConstants.MAX_SECONDS - ReactionTestConstants.MIN_SECONDS);
    }

    /**
     * Generates the Number of Enabled Buttons for upcoming Round.
     * @return
     */
    public ReactionButton[] getRandButtons() {
        // Number of max enabled Buttons can't be higher than number of Buttons
        int max = 0;
        if (getNumberOfButtons() < ReactionTestConstants.MAX_RAND_BUTTONS) {
            max = getNumberOfButtons();
        } else {
            max = ReactionTestConstants.MAX_RAND_BUTTONS;
        }

        int numOfButtons = getRandInt(ReactionTestConstants.MIN_RAND_BUTTONS, max + 1);

        currentButtons = numOfButtons;

        ReactionButton[] reactionButtons = new ReactionButton[numOfButtons];
        for (int i = 0; i < numOfButtons; i++) {
            reactionButtons[i] = getRandButton(reactionButtons);
        }

        return reactionButtons;
    }

    /**
     * Returns a random Button which is not included in the given Array
     *
     * @param reactionButtons Array where the buttons are which should be excluded
     * @return
     */
    private ReactionButton getRandButton(ReactionButton[] reactionButtons) {
        Vector buttonVector = new Vector<>();
        for (ReactionButton button : reactionButtons) {
            buttonVector.add(button);
        }
        ReactionButton button = null;
        do {
            button = buttons[getRandInt(0, ReactionTestConstants.ROWS_BUTTONS)][getRandInt(0, ReactionTestConstants.COLS_BUTTONS)];
        } while (buttonVector.contains(button));
        return button;
    }

    /**
     * Returns an Integer between min and max (max excluded)
     * @param min
     * @param maxExcluded
     * @return
     */
    public static int getRandInt(int min, int maxExcluded) {
        return ThreadLocalRandom.current().nextInt(min, maxExcluded);
    }

    /**
     * @return Number of Buttons on the Panel
     */
    private static int getNumberOfButtons() {
        return ReactionTestConstants.COLS_BUTTONS * ReactionTestConstants.ROWS_BUTTONS;
    }

    /**
     * [Function just for SingePlayer offline Mode] -> Returns Reference to Button which will be enabled
     */
    public void startWithRandomButtons() {
        ReactionButton[] reactionButtons = getRandButtons();

        long timeMs = (long) (getRandSeconds() * 1000.0);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (ReactionButton button : reactionButtons) {
                    button.enableReaction();
                }
                currentStartTime = System.currentTimeMillis();
            }
        }, timeMs);
    }

    /**
     * Each Button has a number and will be enabled by his number. Returns the Number from Button which will be enabled
      * @return
     */
    public static int[][] getRandomUniqueButtonNumbers(){
        ReactionButtonsPanel reactionButtonsPanel = new ReactionButtonsPanel();
        ReactionButton[] buttons = reactionButtonsPanel.getRandButtons();
        int[][] array = new int[2][buttons.length];
        for (int i = 0;i< buttons.length;i++){
            array[0][i] = buttons[i].getRow();
            array[1][i] = buttons[i].getCol();
        }
        return array;
    }


    public void disableAllButtons(){
        for (ReactionButton[] buttonRows:buttons) {
            for (ReactionButton button:buttonRows) {
                button.disableReaction();
            }
        }
    }

    /**
     * Enables Button if Server send Information
     * @param rows
     * @param cols
     * @param timeMs
     */
    public void start(int[] rows, int[] cols, long timeMs) {
        // TODO: Implement to start with same Buttons as other PC
        ReactionButton[] reactionButtons = new ReactionButton[rows.length];

        for (int i = 0; i < rows.length; i++) {
            reactionButtons[i] = buttons[rows[i]][cols[i]];
        }

        if (timer != null){
            timer.cancel();
        }

        disableAllButtons();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (ReactionButton button : reactionButtons) {
                    button.enableReaction();
                }
                currentStartTime = System.currentTimeMillis();
            }
        }, timeMs);
    }

    /**
     * If Server sends Object StartMove this Function Extracts Information (Rows,Cols,Time) from it.
     * @param startMove
     */
    public void start(StartMove startMove){
        start(startMove.rows, startMove.cols, (long)startMove.time*1000);
    }

    /**
     * Enables Button in right row and col (Like Sinking Boats)
     * @param row
     * @param col
     */
    private void enableButton(int row, int col) {
        buttons[row][col].enableReaction();
    }


}
