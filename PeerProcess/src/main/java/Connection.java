import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Connection {
	private PeerInfo _info;
	private SocketInterface _socket;

	// Constructs a connection based off of PeerInfo
	public Connection(PeerInfo info)
	throws IOException, UnknownHostException {
		_info = info;
		PeerSocketFactory factory = new PeerSocketFactory();
		_socket = factory.buildSocket(_info.getHost(), _info.getPort());
	}

	// Constructs a connection based off of PeerInfo and an existing socket
	public Connection(PeerInfo info, SocketInterface socket){
		_info = info;
		_socket = socket;
	}

	// Updates the PeerInfo for the Connection
	// This is used when we have an anonymous connection that identifies themself via. handshaking
	public void updatePeerInfo(PeerInfo info) {
		_info = info;
	}

	// Sends a handshake message
	public void sendHandshake(HandshakeMessage msg) throws IOException{
		_socket.write("P2PFILESHARINGPROJ".getBytes());
		_socket.write(new byte[10]);
		System.out.println("Sending handshake with id" + msg.getPeerId());
		_socket.write(Helpers.intToBytes(msg.getPeerId(), 4));
	}

	// Receives a handshake message
	public HandshakeMessage receiveHandshake() throws IOException {
		byte[] str = new byte[18];
		byte[] zeros = new byte[10];
		byte[] id = new byte[4];

		// Validate the 18 bit string
		try {
			_socket.read(str);
			if (!new String(str).equals("P2PFILESHARINGPROJ")) {
				System.out.println("Did not receive handshake - expected handshake");
				return null;
			}
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}

		// Validate the 10 bits of zeroes
		try {
			_socket.read(zeros);
			for (byte b : zeros) {
				if (b != 0) {
					System.out.println("Handshake header not followed by 10 zero bits");
					return null;
				}
			}
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}

		// Validate the remote peer id
		try {
			_socket.read(id);
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}

		System.out.println("Received handshake with id " + Helpers.bytesToInt(id));

		// Returns a new handshake to send to the remote peer
		return new HandshakeMessage(Helpers.bytesToInt(id));
	}

	// Sends a message to remote peer
	public void send(Message m)
	throws IOException {
		byte[] lengthAsArr = Helpers.intToBytes(m.getLength(), 4);
		_socket.write(lengthAsArr);
		_socket.write(new byte[]{m.getType()});
		_socket.write(m.getPayload());
	}

	// Receives a message from remote peer
	public Message receive()
	throws IOException {
		byte[] msg_length = new byte[4];
		Byte type = -1;

		// Read message length
		try{
			_socket.read(msg_length, 4);
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}
		int ml = ByteBuffer.wrap(msg_length).getInt();

		// Read message type
		try {
			type = (byte)_socket.read();
			if(type == -1)
				throw new IOException("End of stream reached.");
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}

		// Allocating byte array for msg
		byte[] msg = new byte[ml];

		// Read message payload
		try {
			_socket.read(msg,ml);
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}

		// Returns the message
		return new Message(type, msg);
	}

	// Closes the socket of this Connection
	public void close(){
		if( _socket != null){
			try {
				_socket.close();
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			_socket = null; //reset
		}
	}

	// Get the PeerInfo
	public PeerInfo GetInfo() {
		return _info;
	}
}