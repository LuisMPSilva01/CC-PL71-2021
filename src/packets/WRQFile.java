package packets;

import java.nio.ByteBuffer;

public class WRQFile extends Pacote{
    public WRQFile(int nblocks) {
        super(6);
        bytes[0]=1;
        bytes[1]=1;
        byte[] blocos = ByteBuffer.allocate(4).putInt(nblocks).array();
        System.arraycopy(blocos, 0, bytes, 2, blocos.length);
    }
}
