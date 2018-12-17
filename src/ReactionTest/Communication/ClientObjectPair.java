package ReactionTest.Communication;

import java.io.Serializable;

public class ClientObjectPair {
    public ReactionTestClient client;
    public Serializable object;

    public ClientObjectPair(ReactionTestClient client, Serializable object) {
        this.client = client;
        this.object = object;
    }
}
