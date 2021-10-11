import java.io.IOException;

public interface SocketInterface {
	// close socket
	public void close() throws IOException;

	// write bytes from byte array over to socket output stream

	public void write(byte[] b) throws IOException;

	// reads next byte from socket input stream
	public int read() throws IOException;

	//reads specified amount of bytes from socket input stream
	public int read(byte[] b, int len) throws IOException;

	// reads as many consecutive bytes as it can from socket input stream
	public int read(byte[] b) throws IOException;

}