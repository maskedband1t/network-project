import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PeerSocket implements SocketInterface{
	private Socket s;
	private InputStream is;
	private OutputStream os;

	// Constructor for PeerSocket, uses params to create Java API Socket
	public PeerSocket(String host, int port) {
		try {
			s = new Socket(host,port);
			is = s.getInputStream();
			os = s.getOutputStream();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	// Constructor with an existing Socket
	public PeerSocket(Socket socket) {
		s = socket;
		try {
			is = s.getInputStream();
			os = s.getOutputStream();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	// Close socket
	public void close() {
		try {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
			if (s != null)
				s.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	// Write to socket
	public void write(byte[] b) {
		try {
			if (b != null) {
				os.write(b);
				os.flush();
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	// Write byte to socket
	public void write(byte b) {
		try {
			os.write(b);
		} catch (IOException e) {
			//e.printStackTrace();
		}
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
	public int read(byte[] b, int len) {
		try {
			// set default offset to 0
			return is.read(b, 0, len);
		}
		catch (Exception e) {
			//e.printStackTrace();
			return -1;
		}
	}

	// Read fully from socket
	public int read(byte[] b) {
		try {
		return is.read(b);
		}
		catch (Exception e) {
			//e.printStackTrace();
			return -1;
		}
	}
}