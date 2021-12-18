package packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RRQFolder implements UDP_Packet {
    byte[] bytes;
    public RRQFolder(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public RRQFolder() {
        bytes = new byte[1200];
        this.bytes[4] = 1;

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Copiar o número do bloco
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
        return 1 == bytes[4] &&
                getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200));
    }
}
