import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection implements Runnable{
	private PeerInfo _info;
	private SocketInterface _socket;
	private int _peerId;
	private FileManager _fileManager;
	private PeerManager _peerManager;
	private boolean _isConnectingPeer;
	private int _expectedRemotePeerId;
	private int _remotePeerId;
	private BlockingQueue<Message> _queue = new LinkedBlockingQueue<>();

	// opens new connection to specified peer 
	public Connection(PeerInfo info)
	throws IOException, UnknownHostException {
		_info = info;
		PeerSocketFactory factory = new PeerSocketFactory();
		_socket = factory.buildSocket(_info.getHost(), _info.getPort());
	}

	// opens new connection to localhost peer for dev purposes
	public Connection(int peerId, int port)
			throws IOException, UnknownHostException {
		_info = new PeerInfo(peerId, "localhost", port);
		PeerSocketFactory factory = new PeerSocketFactory();
		_socket = factory.buildSocket(_info.getHost(), _info.getPort());
	}

	// creates connection with pre-existing socket
	public Connection(PeerInfo info, SocketInterface socket){
		_info = info;
		_socket = socket;
	}

	public Connection(int id, PeerSocket sock, FileManager fileManager, PeerManager peerManager) {
		_peerId = id;
		_socket = sock;
		_remotePeerId = -1;
		_expectedRemotePeerId = -1;
		_fileManager = fileManager;
		_peerManager = peerManager;
		_isConnectingPeer = false;
	}

	public void sendHandshake(byte[] handshake) throws IOException{
		_socket.write(handshake);
	}

	public void send(Message m)
	throws IOException {
		// TODO: implement logging here
		// TODO: think about whether writing a message will also need its type passed
		byte[] lengthAsArr = Helpers.intToByte(m.getLength(), 4);
		_socket.write(lengthAsArr);
		_socket.write(new byte[]{m.getType()});
		_socket.write(m.getPayload()); // passed in byte[]
	}

	public Message receive()
	throws IOException {
		byte[] msg_length = new byte[4];
		Byte type = -1;

		try{
			_socket.read(msg_length, 4);
		} catch (IOException e) {
			// TODO: handle exception
		}
		int ml = ByteBuffer.wrap(msg_length).getInt();

		// allocating byte array for msg 
		byte[] msg = new byte[ml];
		
		try {
			type = (byte)_socket.read();
			if(type == -1){ 
				// end of stream reached
				//TODO: handle
			}

		} catch (IOException e) {
			//TODO: handle exception
		}

		try {
			_socket.read(msg,ml);  // tries to read buffer length ml from output stream to capture the msg
		} catch (IOException e) {
			//TODO: handle exception
		}

		Message m = new Message(type, msg); // not sure yet if type is representing exactly what it should
		return m;
	}

	public void close(){
		if( _socket != null){
			try {
				_socket.close();
			} catch (IOException e) {
				//TODO: handle exception
			}
			_socket = null; //reset
		}
	}

	public PeerInfo GetInfo() {
		return _info;
	}

	@Override
	public void run() {
		new ConnectionHelper(_queue, _socket).start();

		// TODO: MORE STUFF
	}
}