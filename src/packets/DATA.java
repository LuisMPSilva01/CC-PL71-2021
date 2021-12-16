package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DATA extends Pacote{
    public DATA(int nBloco,byte[] data) {
        super(1 + 4 + 4 + 4+data.length);
        bytes[4] = 4;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 5, 4); //Copiar o número do bloco
        byte[] size = ByteBuffer.allocate(4).putInt(data.length).array();
        System.out.println("bloco: " + nBloco + " | len: " + data.length);
        System.arraycopy(size, 0, bytes, 9, 4);
        System.arraycopy(data, 0, bytes, 13, data.length); //Copiar a data (possivelmente isto pode ser melhorado)

        byte[] hashcode = ByteBuffer.allocate(4).putInt(Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1 + 4 + 4 + 4+data.length))).array();
        System.arraycopy(hashcode, 0, bytes, 0, 4); //Copiar o número do bloco
    }

    public DATA(byte[] bytes) {
        super(bytes);
        offSet= Arrays.hashCode(bytes);
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

    public boolean verificaIntegridade(){
        System.out.println(getHashCode());
        System.out.println();
        return  this.bytes[4]==4 && getHashCode() == Arrays.hashCode(Arrays.copyOfRange(bytes, 4,1 + 4 + 4 + 4+getblockSize()));
    }
}
