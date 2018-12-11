package ReactionTest.Messages;

import ReactionTest.ReactionButtonsPanel;

import java.io.Serializable;

public class StartMove implements Serializable {
    public float time;
    public int[] rows;
    public int[] cols;

    public StartMove(float time, int[] rows, int[] cols) {
        this.time = time;
        this.rows = rows;
        this.cols = cols;
    }

    public StartMove() {
        time = ReactionButtonsPanel.getRandSeconds();
        int[][] buttons = ReactionButtonsPanel.getRandomUniqueButtonNumbers();
        rows = buttons[0];
        cols = buttons[1];
    }

    @Override
    public String toString() {
        return "Time: " + time;
    }
}
