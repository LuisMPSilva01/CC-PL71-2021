package packets;

public interface UDP_Packet {
    public byte[] getContent();
    public boolean isOK(); //Verifica integridade e identificador do pacote
    public String toLogInput(); //Transforma o pacote numa entrada dos logs (packetLogs)
}
