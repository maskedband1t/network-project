import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerSocketFactory {
	// Builds a PeerSocket with host and port info
	public SocketInterface buildSocket(String host, int port)
	throws IOException, UnknownHostException {
		return new PeerSocket(host, port);
	}

	// Builds a PeerSocket with an existing socket
	public SocketInterface buildSocket(Socket socket)
	throws IOException {
		return new PeerSocket(socket);
	}
}
