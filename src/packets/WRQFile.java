package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class WRQFile implements UDP_Packet{
    byte[] bytes;
    public WRQFile(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public WRQFile(int nblocks) {
        bytes= new byte[1200];
        bytes[4] = 3;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nblocks).array();
        System.arraycopy(blocos, 0, bytes, 5, blocos.length); //Numero de blocos de DATA

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Gerar hashcode
    }

    public int getNBlocos() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    public int getHashCode(){
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 0, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    @Override
    public byte[] getContent(){
        return bytes.clone();
    }

    @Override
    public boolean isOK() {
        return 3 == bytes[4] &&
                getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200));
    }

    @Override
    public String toLogInput() {
        return ("WRQFile("+getNBlocos()+")");
    }
}
