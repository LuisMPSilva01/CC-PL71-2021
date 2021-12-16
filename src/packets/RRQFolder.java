package packets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RRQFolder extends Pacote {
    public RRQFolder(byte[] bytes) {
        super(bytes);
        offSet= Arrays.hashCode(bytes);
    }

    public RRQFolder(String folder) {
        super(1 + folder.length() + 1);
        this.bytes[0] = 1;
        byte[] fArray = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, this.bytes, 1, fArray.length);
        this.bytes[this.bytes.length - 1] = 0;
        offSet= Arrays.hashCode(bytes);
    }

    public String getFolderName(){
        int i;
        for(i = 1; i < this.bytes.length && this.bytes[i] != 0; i++)
            ;
        byte[] folderName = new byte[i - 1];
        System.arraycopy(this.bytes, 1, folderName, 0, i - 1);
        return new String(folderName, StandardCharsets.UTF_8);
    }
}
