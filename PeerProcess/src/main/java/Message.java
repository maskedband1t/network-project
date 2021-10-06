public class Message {
    private int type;
    private byte[] data;

    public Message(int type, byte[] data) {
        this.type = type;
        this.data = (byte[])data.clone();
    }

    public int getMsgType() {
        return type;
    }

    public byte[] getMsgData() {
        return (byte[])data.clone();
    }
}
