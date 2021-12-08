package packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Pacote {
    byte[] bytes;

    public Pacote(int size) {
        bytes = new byte[size];
    }
    public Pacote(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public String toString(){
        return Arrays.toString(bytes);
    }

    public byte[] getContent(){
        return bytes.clone();
    }

    public byte[] intToBytes(int n) {
        return ByteBuffer.allocate(4).putInt(n).array();
    }

    public byte[] longToBytes(Long l) {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(l);
        return buf.array();
    }

    public Long bytesToLong(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);
        Long l = in.readLong();
        in.close();
        return l;
    }

    public String bytesToString(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }
    public boolean isFIN(){
        return bytes[0]==7;
    }
    public boolean isWRQFile(){
        return bytes[0]==3;
    }
}
