package packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FolderName implements UDP_Packet {
    byte[] bytes;
    public FolderName(byte[] bytes) {
        this.bytes=bytes.clone();
    }

    public FolderName(String folder,int nFilesBlocks) {
        bytes = new byte[1200];
        this.bytes[4] = 8;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nFilesBlocks).array();
        System.arraycopy(blocos, 0, bytes, 5, blocos.length);

        byte[] tamanho = ByteBuffer.allocate(4).putInt(folder.length()).array();
        System.arraycopy(tamanho, 0, bytes, 9, tamanho.length);

        byte[] nome = folder.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(nome, 0, this.bytes, 13, nome.length);

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Copiar o número do bloco
    }
    public int getFilesBlocks() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    public int getStringSize() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 9, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    public String getFolderName(){
        int stringSize=getStringSize();
        byte[] folderName = new byte[stringSize];
        System.arraycopy(this.bytes, 13, folderName, 0, stringSize);

        return new String(folderName, StandardCharsets.UTF_8);
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
        return 8==bytes[4] &&
                getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200));
    }

    @Override
    public String toLogInput() {
        return "FolderName("+getFolderName()+")";
    }

}