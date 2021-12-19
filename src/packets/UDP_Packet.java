package packets;

public interface UDP_Packet {
    public byte[] getContent();
    public boolean isOK();
}
