import java.io.IOException;
import java.net.UnknownHostException;

public class PeerSocketFactory {
	public SocketInterface buildSocket(String host, int port)
	throws IOException, UnknownHostException {
		return new PeerSocket(host, port);
	}

	public SocketInterface buildSocket(Socket socket)
	throws IOException {
		return new PeerSocket(socket);
	}
}
