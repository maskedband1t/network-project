import java.io.IOException;
import java.net.UnknownHostException;

public class Connection{
	private PeerInfo _info;
	private SocketInterface _socketInterface;

	// opens new connection to specified peer 
	public Connection(PeerInfo info)
	throws IOException, UnknownHostException {
		_info = info;
		PeerSocketFactory factory = new PeerSocketFactory();
		_socketInterface = factory.buildSocket(_info.getHost(), _info.getPort());
	}

	// creates connection with pre-existing socket
	public Connection(PeerInfo info, SocketInterface socket){
		_info = info;
		_socketInterface = socket;
	}
	
}