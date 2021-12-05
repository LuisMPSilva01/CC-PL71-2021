package packets;

public class FIN extends Pacote{
    public FIN(byte[] bytes) {
        super(bytes);
    }
    public FIN() {
        super(1);
        this.bytes[0] = 7;
    }
}
