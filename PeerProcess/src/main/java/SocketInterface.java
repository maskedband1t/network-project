import java.io.IOException;

public interface SocketInterface {
	// Close socket
	public void close() throws IOException;

	// Write bytes from byte array over to socket output stream
	public void write(byte[] b) throws IOException;

	// Reads next byte from socket input stream
	public int read() throws IOException;

	// Reads specified amount of bytes from socket input stream
	public int read(byte[] b, int len) throws IOException;

	// Reads as many consecutive bytes as it can from socket input stream
	public int read(byte[] b) throws IOException;

}