package packets;

import java.nio.charset.StandardCharsets;

public class RRQFolder extends Pacote {

    public RRQFolder(String folder) {
        super(1+folder.length()+1);
        bytes[0] = 1;
        byte[] fArray = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 1, fArray.length);
        bytes[bytes.length-1] = 0;
    }
}
