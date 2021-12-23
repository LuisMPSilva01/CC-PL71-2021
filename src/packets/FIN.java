package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class FIN implements UDP_Packet{
    byte[] bytes;
    public FIN(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public FIN() {
        bytes= new byte[1200];
        bytes[4]=7;

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4);//Gerar hashcode
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
        return 7==bytes[4]
                && getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200));
    }

    @Override
    public String toLogInput() {
        return "FIN";
    }

}
