package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DATA implements UDP_Packet{
    byte[] bytes;
    public DATA(int nBloco,byte[] data) {
        bytes = new byte[1+4+4+4+ data.length];
        bytes[4] = 4;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 5, 4); //Copiar o número do bloco
        byte[] size = ByteBuffer.allocate(4).putInt(data.length).array();
        System.arraycopy(size, 0, bytes, 9, 4);
        System.arraycopy(data, 0, bytes, 13, data.length); //Copiar a data (possivelmente isto pode ser melhorado)

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1 + 4 + 4 + 4+data.length))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Copiar o número do bloco
    }

    public DATA(byte[] bytes) {
        this.bytes=bytes.clone();
    }
    public int getHashCode(){
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 0, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
    public int getNBloco() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
    public int getblockSize() {
        byte[] blockSize = new byte[4]; //Guardar conteudo
        System.arraycopy(this.bytes, 9, blockSize, 0, 4);
        return ByteBuffer.wrap(blockSize).getInt();
    }

    public byte[] getConteudo(){
        int size=this.getblockSize();
        byte[] tmp = new byte[size];
        System.arraycopy(this.bytes, 13, tmp, 0, size);
        return tmp;
    }

    @Override
    public byte[] getContent(){
        return bytes.clone();
    }

    @Override
    public boolean isOK() {
        return  this.bytes[4]==4
                && getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1+4+4+4+getblockSize()));
    }

    @Override
    public String toLogInput() {
        return ("DATA("+getNBloco()+")");
    }
}
