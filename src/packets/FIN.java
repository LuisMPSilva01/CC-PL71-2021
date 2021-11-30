package packets;

public class FIN extends Pacote{
    public FIN() {
        super(2);
        bytes[0]=0;
        bytes[1]=6;
    }
}
