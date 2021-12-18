package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ACK implements UDP_Packet{
    byte[] bytes;

    public ACK(int nBloco) {
        bytes = new byte[1200];
        bytes[4] = 5;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 5, 4); //Copiar o número do bloco

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Copiar o número do bloco
    }
    public ACK(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public int getNBloco() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
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
        return  5 == bytes[4] &&
                getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200));
    }
}
