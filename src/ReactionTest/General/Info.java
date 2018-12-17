package ReactionTest.General;

import ReactionTest.Communication.ReactionTestClient;

public interface Info {
    public ReactionTestClient getClient();
    public void info(String info);
    public void error(String error);
}
