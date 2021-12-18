package packets;

import java.nio.ByteBuffer;
import java.util.Map;

public class FILES implements UDP_Packet{
    byte[] bytes;
    public FILES(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public FILES(Map<String, Long> m){
        bytes= new byte[1200];  // mudar depois
        bytes[0] = 6;
        int pos = 1;

        /*
        byte[] byteArray = ByteBuffer.allocate(4).putInt(bloco).array();
        System.arraycopy(byteArray, 0, bytes, pos, 4); //block#
        pos += 4;

        byteArray = ByteBuffer.allocate(4).putInt(nblocos).array();
        System.arraycopy(byteArray, 0, bytes, pos, 4); //total blocks
        pos += 4;

        */ 

        for(Map.Entry<String, Long> entry: m.entrySet()){
            byte[] filename = entry.getKey().getBytes();
            byte[] size_filename = intToBytes(filename.length);
            byte[] filesize = longToBytes(entry.getValue());

            System.arraycopy(size_filename, 0, bytes, pos, 4);
            pos += 4;
            System.arraycopy(filename, 0, bytes, pos, filename.length);
            pos += filename.length;
            System.arraycopy(filesize, 0, bytes, pos, 8);
            pos += 8;
        }
        bytes[pos] = -1;
    }

    @Override
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

    public int getNbloco(){
        return 0;
    }

    @Override
    public boolean isOK() {
        return 6==bytes[0];
    }
}
