package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class WRQFile extends Pacote{
    public WRQFile(byte[] bytes) {
        super(bytes);
        offSet= Arrays.hashCode(bytes);
    }
    public WRQFile(int nblocks) {
        super(1+4);
        bytes[0] = 3;
        byte[] blocos = ByteBuffer.allocate(4).putInt(nblocks).array();
        System.arraycopy(blocos, 0, bytes, 1, blocos.length);
        offSet= Arrays.hashCode(bytes);
    }
    public int getNBlocos() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
}
