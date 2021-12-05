package packets;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FILES extends Pacote{
    List<String> ficheiros;
    long size;

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

    public FILES(byte[] array) throws IOException{
        super(array.length);
        bytes = array.clone();
        int pos = 1;
        this.ficheiros = new ArrayList<>();
        this.size=0;

        while (true){
            byte[] filenameSizeArray = new byte[4];
            System.arraycopy(array, pos, filenameSizeArray, 0, 4);
            ByteBuffer wrapped = ByteBuffer.wrap(filenameSizeArray);
            int fileNameSizeInt = wrapped.getInt();
            pos += 4;

            byte[] filenameArray = new byte[fileNameSizeInt];
            System.arraycopy(array, pos, filenameArray, 0, fileNameSizeInt);
            this.ficheiros.add(new String(filenameArray, StandardCharsets.UTF_8));
            pos += fileNameSizeInt;

            byte[] fileSizeArray = new byte[8];
            System.arraycopy(array, pos, fileSizeArray, 0, 8);
            wrapped = ByteBuffer.wrap(fileSizeArray);
            this.size+=wrapped.getLong();
            pos += 8;
        }
        buf[pos] = -1;

        System.arraycopy(buf, 0, this.bytes, 0, buf.length);
    }
}
