package packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RRQFile implements UDP_Packet{
    byte[] bytes;
    public RRQFile(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public RRQFile(String file) {
        this.bytes= new byte[1200];
        bytes[4] = 2;

        byte[] tamanho = ByteBuffer.allocate(4).putInt(file.length()).array();
        System.arraycopy(tamanho, 0, bytes, 5, 4); //tamanho do nome do ficheiro

        byte[] fArray = file.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 9, fArray.length); //Nome do ficheiro

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Gerar hashcode
    }
    public int getStringSize() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    public String getFileName(){
        byte[] fileName = new byte[getStringSize()];
        System.arraycopy(this.bytes, 9, fileName, 0, getStringSize());
        return new String(fileName, StandardCharsets.UTF_8);
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
        return 2 == bytes[4]&&
                getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200));
    }

    @Override
    public String toLogInput() {
        return "RRQFile("+getFileName()+")";
    }
}
