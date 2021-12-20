package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class FILES implements UDP_Packet{
    byte[] bytes;
    public FILES(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public FILES(Map<String, LongTuple> m, int nrBlock){
        bytes= new byte[1200];
        bytes[4] = 6;
        int pos = 5;

        byte[] nrBloc = ByteBuffer.allocate(4).putInt(nrBlock).array();
        System.arraycopy(nrBloc, 0, bytes, pos, 4); //block#
        pos += 4;

        for(Map.Entry<String, LongTuple> entry: m.entrySet()){
            byte[] filename = entry.getKey().getBytes();
            byte[] size_filename = intToBytes(filename.length);
            LongTuple lt = entry.getValue();
            byte[] filesize = longToBytes(lt.getA());
            byte[] lastModifiedDate = longToBytes(lt.getB());

            System.arraycopy(size_filename, 0, bytes, pos, 4);
            pos += 4;
            System.arraycopy(filename, 0, bytes, pos, filename.length);
            pos += filename.length;
            System.arraycopy(filesize, 0, bytes, pos, 8);
            pos += 8;
            System.arraycopy(lastModifiedDate, 0, bytes, pos, 8);
            pos += 8;
        }
        bytes[pos] = -1;
        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1200))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Copiar o número do bloco
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

    public byte[] intToBytes(int n) {
        return ByteBuffer.allocate(4).putInt(n).array();
    }

    public byte[] longToBytes(Long l) {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(l);
        return buf.array();
    }

    public int getNbloco() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }

    @Override
    public boolean isOK() {
        return 6==bytes[4];
    }

    @Override
    public String toLogInput() {
        return ("FILES("+getNbloco()+")");
    }


}
