package packets;

import java.nio.charset.StandardCharsets;

public class RRQFile extends Pacote{
    public RRQFile(byte[] bytes) {
        super(bytes);
    }
    public RRQFile(String file) {
        super(1+file.length());
        bytes[0] = 2;

        byte[] fArray = file.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 1, fArray.length);
    }
    public String getFileName(){
        int i;
        for(i = 1; i < this.bytes.length && this.bytes[i] != 0; i++)
            ;
        byte[] fileName = new byte[i - 1];
        System.arraycopy(this.bytes, 1, fileName, 0, i - 1);
        return new String(fileName, StandardCharsets.UTF_8);
    }
}
