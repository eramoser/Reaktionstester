package ReactionTest;

public class Messages {
    public static final int GAME_START = 0;
    public static final int FINISHED_MOVE = 1;

    public static String getMessage(int messageType, String info){
        return messageType + ";" + info;
    }

    public static String getFinishedMoveMessage(){
        return getMessage(GAME_START, )
    }
}
