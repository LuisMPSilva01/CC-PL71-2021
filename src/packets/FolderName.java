package packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FolderName extends Pacote {
    public FolderName(byte[] bytes) {
        super(bytes);
        offSet= Arrays.hashCode(bytes);
    }

    public FolderName(String folder,int nFilesBlocks) {
        super(1 + 4 + 4+folder.length() + 1);
        this.bytes[0] = 8;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nFilesBlocks).array();
        System.arraycopy(blocos, 0, bytes, 1, blocos.length);

        byte[] tamanho = ByteBuffer.allocate(4).putInt(folder.length()).array();
        System.arraycopy(tamanho, 0, bytes, 5, tamanho.length);

        byte[] fArray = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, this.bytes, 9, fArray.length);
        this.bytes[this.bytes.length - 1] = 0;
        offSet= Arrays.hashCode(bytes);
    }
    public int getFilesBlocks() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
    public int getStringSize() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    public String getFolderName(){
        int stringSize=getStringSize();
        byte[] folderName = new byte[stringSize];
        System.arraycopy(this.bytes, 9, folderName, 0, stringSize);

        return new String(folderName, StandardCharsets.UTF_8);
    }
}