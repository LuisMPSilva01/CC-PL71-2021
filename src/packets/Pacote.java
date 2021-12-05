package packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Pacote {
    byte[] bytes;

    public Pacote(int size) {
        bytes = new byte[size];
    }

    public String toString(){
        return Arrays.toString(bytes);
    }

    public byte[] getContent(){
        return bytes.clone();
    }

    public byte[] intToBytes(int n) throws IOException {
        return ByteBuffer.allocate(4).putInt(n).array();
    }

    //public int bytesToInt(byte[] bytes){return ByteBuffer.wrap(bytes).getInt();}

    public byte[] longToBytes(Long l) throws IOException {
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

    public String bytesToString(byte[] bytes, int offset, int length) throws IOException {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }
}