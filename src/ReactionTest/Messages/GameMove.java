
package ReactionTest.Messages;

import ReactionTest.ReactionButton;

public class GameMove {
    private ReactionButton[] buttons;
    private long msBeforeStart;

    public GameMove(ReactionButton[] buttons, long msBeforeStart) {
        this.buttons = buttons;
        this.msBeforeStart = msBeforeStart;
    }
}
