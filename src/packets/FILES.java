package packets;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FILES extends Pacote{
    public FILES(int bloco,int nblocos,String fileName,long fileSize) {
        super(1+4+4+fileName.length()+1+8+1);
        bytes[0] = 6;

        int pos = 1;

        byte[] byteArray = ByteBuffer.allocate(4).putInt(bloco).array();
        System.arraycopy(byteArray, 0, bytes, pos, 4); //block#
        pos += 4;

        byteArray = ByteBuffer.allocate(4).putInt(nblocos).array();
        System.arraycopy(byteArray, 0, bytes, pos, 4); //total blocks
        pos += 4;

        byteArray = fileName.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(byteArray, 0, bytes, pos, byteArray.length); //filename
        pos+=byteArray.length;

        bytes[pos++] = 0;

        byteArray = ByteBuffer.allocate(8).putLong(fileSize).array();
        System.arraycopy(byteArray, 0, bytes, pos, 8); //filesize
        pos+=8;

        bytes[pos] = 0;
    }

    public FILES(Map<String, Long> m) throws IOException{
        super(1200);  // mudar depois
        byte[] buf = new byte[1200];
        buf[0] = 6;
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

            System.arraycopy(size_filename, 0, buf, pos, 4);
            pos += 4;
            System.arraycopy(filename, 0, buf, pos, filename.length);
            pos += filename.length;
            System.arraycopy(filesize, 0, buf, pos, 8);
            pos += 8;
        }
        buf[pos] = -1;    
        
        System.arraycopy(buf, 0, this.bytes, 0, buf.length);
    }
}
