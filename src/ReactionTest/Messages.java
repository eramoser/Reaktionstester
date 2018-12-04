package ReactionTest;

public class Messages {
    public static final String SEPARATOR = ";";
    public static final int GAME_START = 0;
    public static final int FINISHED_MOVE = 1;
    public static final int CLIENT_CLOSED = 2;

    public static String getMessage(int messageType, String info){
        return messageType + ";" + info;
    }

    public static String getFinishedMoveMessage(float time){
        return getMessage(FINISHED_MOVE, Float.toString(time));
    }

    public static String ClientClose(){
        return getMessage(CLIENT_CLOSED);
    }

    public static int getTypeOfMessage(String message){
        return Integer.parseInt(message.substring(0,message.indexOf(SEPARATOR)));
    }


}
