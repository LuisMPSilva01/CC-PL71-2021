package packets;

import java.nio.charset.StandardCharsets;

public class FolderName extends Pacote {
    public FolderName(byte[] bytes) {
        super(bytes);
    }

    public FolderName(String folder) {
        super(1 + folder.length() + 1);
        this.bytes[0] = 8;
        byte[] fArray = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, this.bytes, 1, fArray.length);
        this.bytes[this.bytes.length - 1] = 0;
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