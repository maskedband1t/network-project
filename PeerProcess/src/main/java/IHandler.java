import java.io.IOException;

public interface IHandler {
    public void handleMsg(Message msg, Connection peerConn) throws IOException;
}
