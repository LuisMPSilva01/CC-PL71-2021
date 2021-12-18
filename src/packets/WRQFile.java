package packets;

import java.nio.ByteBuffer;

public class WRQFile implements UDP_Packet{
    byte[] bytes;
    public WRQFile(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public WRQFile(int nblocks) {
        bytes= new byte[1+4];
        bytes[0] = 3;
        byte[] blocos = ByteBuffer.allocate(4).putInt(nblocks).array();
        System.arraycopy(blocos, 0, bytes, 1, blocos.length);
    }
    public int getNBlocos() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    @Override
    public byte[] getContent(){
        return bytes.clone();
    }

    @Override
    public boolean isOK() {
        return 3 == bytes[0];
    }
}
