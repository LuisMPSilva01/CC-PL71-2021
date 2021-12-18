package packets;

import java.nio.charset.StandardCharsets;

public class RRQFolder implements UDP_Packet {
    byte[] bytes;
    public RRQFolder(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public RRQFolder(String folder) {
        bytes = new byte[1+folder.length()+1];
        this.bytes[0] = 1;
        byte[] fArray = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, this.bytes, 1, fArray.length);
        this.bytes[this.bytes.length - 1] = 0;
    }

    @Override
    public byte[] getContent(){
        return bytes.clone();
    }

    @Override
    public boolean isOK() {
        return 1 == bytes[0];
    }
}
