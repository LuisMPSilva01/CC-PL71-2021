package packets;

import java.nio.ByteBuffer;

public class DATA extends Pacote{
    public DATA(int nBloco,byte[] data) {
        super(1+4+data.length);
        bytes[0] = 4;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 1, 4); //Copiar o número do bloco

        System.arraycopy(data, 0, bytes, 5, data.length); //Copiar a data (possivelmente isto pode ser melhorado)
    }
}