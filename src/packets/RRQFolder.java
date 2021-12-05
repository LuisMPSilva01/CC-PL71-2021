package packets;

import java.nio.charset.StandardCharsets;

public class RRQFolder extends Pacote {
    public RRQFolder(byte[] bytes) {
        super(bytes);
    }
    public RRQFolder(String folder) {

        super(1+folder.length()+1);
        bytes[0] = 1;
        byte[] fArray = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 1, fArray.length);
        bytes[bytes.length-1] = 0;
    }
    public String getFolderName(){
        byte[] fArray= new byte[bytes.length-2];
        System.arraycopy(bytes, 1, fArray, 0, bytes.length-2);
        return new String(fArray, StandardCharsets.UTF_8);
    }
}
