/**
* A message in our protocol consists of:
*    - 4-byte  message  length  field
*    - 1-byte  message  type  field
*    - a message payload with variable size.
**/
public class Message {
    private byte[] length;
    private byte type;
    private byte[] payload;

    public Message(byte[] length, byte type, byte[] data) {
        this.length = (byte[])length;
        this.type = type;
        this.payload = (byte[])data.clone();
    }

    public byte[] getLength() { return length; }

    public byte getType() { return type; }

    public byte[] getPayload() {
        return (byte[])payload.clone();
    }
}
