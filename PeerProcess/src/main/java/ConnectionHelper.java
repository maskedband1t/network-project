import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHelper extends Thread {
    private boolean remoteChoked = true;
    private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private Connection conn;

    public ConnectionHelper(BlockingQueue<Message> q, Connection c) {
        q = queue;
        conn = c;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // handle the messages in queue
                Message msg = queue.take();

                // validate not null
                if (msg == null) continue;

                // we only want to accept msg if we know their id
                if (conn.GetInfo().getId() != -1) {
                    if (msg.getType() == Helpers.CHOKE)
                        remoteChoked = true;
                    else if (msg.getType() == Helpers.UNCHOKE)
                        remoteChoked = false;
                }
                else
                    System.out.println("Cannot send messages yet - we have not handshaked");

                // send the actual msg
                conn.send(msg);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
