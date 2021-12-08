import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerSocket implements SocketInterface{
	private Socket s;
	private InputStream is;
	private OutputStream os;

	// Constructor for PeerSocket, uses params to create Java API Socket
	public PeerSocket(String host, int port)
	throws IOException {
		s = new Socket(host,port);
		is = s.getInputStream();
		os = s.getOutputStream();
	}

	// Constructor with an existing Socket
	public PeerSocket(Socket socket)
	throws IOException {
		s = socket;
		is = s.getInputStream();
		os = s.getOutputStream();
	}

	// Close socket
	public void close()
	throws IOException {
		is.close();
		os.close();
		s.close();
	}

	// Write to socket
	public void write(byte[] b)
	throws IOException{
		os.write(b);
		os.flush();
	}

	// Write byte to socket
	public void write(byte b)
			throws IOException{
		os.write(b);
	}

	// Read next byte from socket
	public int read() {
		try {
			return is.read();
		}
		catch (Exception e) {
			//e.printStackTrace();
			return -1;
		}
	}

	// Read determined amount from socket
	public int read(byte[] b, int len) throws IOException {
		try {
			// set default offset to 0
			return is.read(b, 0, len);
		}
		catch (Exception e) {
			//System.exit(-1);
			return -1;
		}
	}

	// Read fully from socket
	public int read(byte[] b) throws IOException {
		try {
		return is.read(b);
		}
		catch (Exception e) {
			//System.exit(-1);
			return -1;
		}
	}
}