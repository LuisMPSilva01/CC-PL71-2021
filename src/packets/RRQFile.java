package packets;

import java.nio.charset.StandardCharsets;

public class RRQFile extends Pacote{
    public RRQFile(byte[] bytes) {
        super(bytes);
    }
    public RRQFile(String file) {
        super(1+file.length()+1);
        bytes[0] = 2;

        byte[] fArray = file.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 1, fArray.length);

        bytes[bytes.length-1] = 0;
    }
    public String getFileName(){
        byte[] fArray= new byte[bytes.length-2];
        System.arraycopy(bytes, 1, fArray, 0, bytes.length-2);
        return new String(fArray, StandardCharsets.UTF_8);
    }
}
