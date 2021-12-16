package packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RRQFile extends Pacote{
    public RRQFile(byte[] bytes) {
        super(bytes);
        offSet= Arrays.hashCode(bytes);
    }
    public RRQFile(String file) {
        super(1+4+file.length());
        bytes[0] = 2;

        byte[] blocos = ByteBuffer.allocate(4).putInt(file.length()).array();
        System.arraycopy(blocos, 0, bytes, 1, 4); //Copiar o número do bloco

        byte[] fArray = file.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 5, fArray.length);
        offSet= Arrays.hashCode(bytes);
    }
    public int getStringSize() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    public String getFileName(){
        byte[] fileName = new byte[getStringSize()];
        System.arraycopy(this.bytes, 5, fileName, 0, getStringSize());
        String dasdsa= new String(fileName, StandardCharsets.UTF_8);
        return new String(fileName, StandardCharsets.UTF_8);
    }
}
