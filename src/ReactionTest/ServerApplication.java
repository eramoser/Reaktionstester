package ReactionTest;



public class ServerApplication {
    public static void main(String[] args) {
        ReactionTestServer server = new ReactionTestServer(ReactionTestConstants.SERVER_PORT);
        server.start();
    }
}
