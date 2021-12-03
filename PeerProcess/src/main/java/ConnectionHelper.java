import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHelper extends Thread {
    private boolean _remoteChoked = true;
    private BlockingQueue<Message> _queue = new LinkedBlockingQueue<>();
    private Connection _conn;

    // Construct the ConnectionHelper
    public ConnectionHelper(BlockingQueue<Message> q, Connection c) {
        _queue = q;
        _conn = c;
    }

    @Override
    public void run() {
        while (!Process.shutdown) {
            try {
                // Handle the messages in queue
                Message msg = _queue.take();

                // Validate not null
                if (msg == null) continue;

                // We only want to accept msg if we know their id
                if (_conn.GetInfo().getId() != -1) {
                    System.out.println("We have a valid connection to " + _conn.GetInfo().getId());
                    if (msg.getType() == Helpers.CHOKE && !_remoteChoked){
                        _remoteChoked = true;
                        // Send the actual msg
                        _conn.send(msg);
                        System.out.println("Choking and Sending message over connection");
                    }
                    else if (msg.getType() == Helpers.UNCHOKE && _remoteChoked) {
                        _remoteChoked = false;
                        // Send the actual msg
                        _conn.send(msg);
                        System.out.println("Unchoking and Sending message over connection");
                    }
                    else if ((msg.getType() == Helpers.CHOKE && _remoteChoked)
                            || (msg.getType() == Helpers.UNCHOKE && !_remoteChoked)) {
                        System.out.println("NOT sending message over connection");
                        continue;
                    }
                    else {
                        System.out.println("Sending message over connection");
                        _conn.send(msg);
                    }
                }
                else
                    System.out.println("Cannot send messages yet - we have not handshaked");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
