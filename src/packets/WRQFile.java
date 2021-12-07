package packets;

import java.nio.ByteBuffer;

public class WRQFile extends Pacote{
    public WRQFile(byte[] bytes) {
        super(bytes);
    }
    public WRQFile(int nblocks) {
        super(1+4);
        bytes[0] = 3;
        byte[] blocos = ByteBuffer.allocate(4).putInt(nblocks).array();
        System.arraycopy(blocos, 0, bytes, 1, blocos.length);
    }
    public int getNBlocos() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
}
