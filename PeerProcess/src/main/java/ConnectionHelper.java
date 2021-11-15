import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHelper extends Thread {
    private boolean remoteChoked = true;
    private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private SocketInterface socket;

    public ConnectionHelper(BlockingQueue<Message> q, SocketInterface s) {
        q = queue;
        socket = s;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // handle the messages in queue
                Message msg = queue.take();

                // validate not null
                if (msg == null) continue;

                if (msg.getType() == Helpers.CHOKE)
                    remoteChoked = true;
                else if (msg.getType() == Helpers.UNCHOKE)
                    remoteChoked = false;

                byte[] lengthAsArr = Helpers.intToByte(msg.getLength(), 4);
                socket.write(lengthAsArr);
                socket.write(new byte[]{msg.getType()});
                socket.write(msg.getPayload());
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
