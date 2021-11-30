package packets;

import java.nio.ByteBuffer;

public class ACK extends Pacote{
    public ACK(int nBloco) {
        super(6);
        bytes[0]=0;
        bytes[1]=3;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 2, 4); //Copiar o número do bloco
    }
}
