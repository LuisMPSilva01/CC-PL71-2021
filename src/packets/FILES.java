package packets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FILES extends Pacote{
    public FILES(int bloco,int nblocos,String fileName,long fileSize) {
        super(2+4+4+fileName.length()+1+8+1);
        bytes[0]=0;
        bytes[1]=5;

        int pos=2;

        byte[] byteArray = ByteBuffer.allocate(4).putInt(bloco).array();
        System.arraycopy(byteArray, 0, bytes, pos, 4); //block#
        pos+=4;

        byteArray = ByteBuffer.allocate(4).putInt(nblocos).array();
        System.arraycopy(byteArray, 0, bytes, pos, 4); //total blocks
        pos+=4;

        byteArray = fileName.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(byteArray, 0, bytes, pos, byteArray.length); //filename
        pos+=byteArray.length;

        bytes[pos++]=0;

        byteArray = ByteBuffer.allocate(8).putLong(fileSize).array();
        System.arraycopy(byteArray, 0, bytes, pos, 8); //filesize
        pos+=8;

        bytes[pos]=0;
    }
}
