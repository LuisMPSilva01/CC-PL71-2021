package packets;

import java.nio.ByteBuffer;

public class ACK extends Pacote{
    public ACK(int nBloco) {
        super(5);
        bytes[0] = 5;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 1, 4); //Copiar o número do bloco
    }
    public ACK(byte[] bytes) {
        super(bytes);
    }
    public int getNBloco() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
}
