package packets;

import java.nio.ByteBuffer;

public class DATA extends Pacote{
    public DATA(int nBloco,byte[] data) {
        super(1 + 4 + 4 + data.length);
        bytes[0] = 4;

        byte[] blocos = ByteBuffer.allocate(4).putInt(nBloco).array();
        System.arraycopy(blocos, 0, bytes, 1, 4); //Copiar o número do bloco
        byte[] size = ByteBuffer.allocate(4).putInt(data.length).array();
        System.out.println("bloco: " + nBloco + " | len: " + data.length);
        System.arraycopy(size, 0, bytes, 5, 4);
        System.arraycopy(data, 0, bytes, 9, data.length); //Copiar a data (possivelmente isto pode ser melhorado)
    }

    public DATA(byte[] bytes) {
        super(bytes);
    }

    public int getNBloco() {
        byte[] tmp = new byte[4];
        System.arraycopy(this.bytes, 1, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).getInt();
    }
    public int getblockSize() {
        byte[] blockSize = new byte[4]; //Guardar conteudo
        System.arraycopy(this.bytes, 5, blockSize, 0, 4);
        return ByteBuffer.wrap(blockSize).getInt();
    }

    public byte[] getConteudo(){
        int size=this.getblockSize();
        byte[] tmp = new byte[size];
        System.arraycopy(this.bytes, 9, tmp, 0, size);
        return tmp;
    }
}
