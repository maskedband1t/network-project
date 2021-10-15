import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerSocket implements SocketInterface{
	private Socket s;
	private InputStream is;
	private OutputStream os;

	//constructor for PeerSocket, uses params to create Java API Socket
	public PeerSocket(String host, int port)
	throws IOException, UnknownHostException {
		s = new Socket(host,port);
	}

	// above constructor gets thrown into this one for instantiation
	public PeerSocket(Socket socket)
	throws IOException {
		s = socket;
		is = s.getInputStream();
		os = s.getOutputStream();
	}

	//close socket
	public void close()
	throws IOException {
		is.close();
		os.close();
		s.close();
	}

	public void write(byte[] b)
	throws IOException{
		os.write(b);
		os.flush();
	}

	public int read()
	throws IOException {
		return is.read();
	}

	public int read(byte[] b, int len) throws IOException {
		// set default offset to 0
		return is.read(b, 0, len);
	}

	public int read(byte[] b) throws IOException {
		return is.read(b);
	}
}