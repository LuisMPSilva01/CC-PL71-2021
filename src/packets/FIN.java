package packets;

import java.util.Arrays;

public class FIN extends Pacote{
    public FIN(byte[] bytes) {
        super(bytes);
        offSet= Arrays.hashCode(bytes);
    }
    public FIN() {
        super(1);
        this.bytes[0] = 7;
        offSet= Arrays.hashCode(bytes);
    }
}
