package packets;

import java.nio.ByteBuffer;

public class DATA extends Pacote{
    public DATA(int nBloco,byte[] data) {
        super(1+4+data.length+1);
        bytes[0] = 4;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 1, 4); //Copiar o número do bloco
        System.arraycopy(data, 0, bytes, 5, data.length); //Copiar a data (possivelmente isto pode ser melhorado)
        bytes[1+4+data.length] = 0;
    }

    public DATA(byte[] bytes) {
        super(bytes);
    }
    
    public int getNBloco() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
}
