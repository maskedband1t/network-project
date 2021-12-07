import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

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
			e.printStackTrace();
			return null;
		}

		// Validate the remote peer id
		try {
			_socket.read(id);
		}
		catch(Exception e) {
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

		// Send message payload
		if (m.getLength()-1 > 0) {
			try {
				_socket.write(m.getPayload());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Sending " + Helpers.GetMessageType(m.getType()) + " message with no payload.");
		}
	}

	// Receives a message from remote peer
	public Message receive() throws IOException {
		System.out.println("Attempting to receive...");
		byte[] msg_length = new byte[4];
		byte type;

		// Read message length
		try{
			System.out.println("Reading 4 bytes into msg_length");
			for (int i = 0; i < 4; i++) {
				try {
					msg_length[i] = (byte)_socket.read();
				}
				catch (SocketException e) {
					e.printStackTrace();
					return null;
				}
			}
			//_socket.read(msg_length, 4);
			System.out.println("Read msg_length: " + Helpers.bytesToInt(msg_length));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		// ml is length of payload only (one was for type byte)
		int ml = Helpers.bytesToInt(msg_length) - 1;

		// Read message type
		try {
			type = (byte)_socket.read();
			System.out.println("Read type: " + type);
			if(type == -1) {
				return null;
				//throw new IOException("End of stream reached.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Allocating byte array for msg
		byte[] msg = new byte[ml];

		// Read message payload
		if (ml > 0) {
			try {
				_socket.read(msg, ml);
				System.out.println("Read msg: " + Helpers.bytesToInt(msg));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			//System.out.println("No payload, not reading");
		}

		// Returns the message
		Message returnMsg = ml > 0 ? new Message(type, msg) : new Message(type, new byte[]{});
		return returnMsg;
	}

	// Closes the socket of this Connection
	public void close(){
		if( _socket != null){
			try {
				_socket.close();
			} catch (IOException e) {
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