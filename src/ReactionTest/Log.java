package ReactionTest;

import java.sql.Timestamp;
import java.util.Date;

public class Log {
    private static final boolean LOG = true;

    public static void log(String text) {
        if (LOG) {
            System.out.println(getTime() + ": " + text);
        }
    }

    private static String getTime() {
        return new Timestamp(new Date().getTime()).toString();
    }
}
