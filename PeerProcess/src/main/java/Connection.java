import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Connection {
	private PeerInfo _info;
	private SocketInterface _socket;

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

	public void sendHandshake(HandshakeMessage msg) throws IOException{
		_socket.write("P2PFILESHARINGPROJ".getBytes());
		_socket.write(new byte[10]);
		System.out.println("Sending handshake with id" + msg.getPeerId());
		_socket.write(Helpers.intToBytes(msg.getPeerId(), 4));
	}

	public HandshakeMessage receieveHandshake() throws IOException {
		byte[] str = new byte[18];
		byte[] zeros = new byte[10];
		byte[] id = new byte[4];

		try {
			_socket.read(str);

			if (!new String(str).equals("P2PFILESHARINGPROJ")) {
				System.out.println("Did not receive handshake - expected handshake");
				return null;
			}
		}
		catch(Exception ex) {
			System.out.println(ex);
			return null;
		}

		try {
			_socket.read(zeros);

			for (byte b : zeros) {
				if (b != 0) {
					System.out.println("Handshake header not followed by 10 zero bits");
					return null;
				}
			}
		}
		catch(Exception ex) {
			System.out.println(ex);
			return null;
		}

		try {
			_socket.read(id);
		}
		catch(Exception e) {
			System.out.println(e);
			return null;
		}

		System.out.println("Received handshake with id " + Helpers.bytesToInt(id));
		return new HandshakeMessage(Helpers.bytesToInt(id));
	}

	public void send(Message m)
	throws IOException {
		byte[] lengthAsArr = Helpers.intToBytes(m.getLength(), 4);
		_socket.write(lengthAsArr);
		_socket.write(new byte[]{m.getType()});
		_socket.write(m.getPayload());
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
}