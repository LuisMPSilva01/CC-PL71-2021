import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindow {
    private final Queue<DataPlusBlock> window; //Queue de blcoos de data
    private int last; //Ultimo bloco de data da window
    private final int maxSize; //Tamanho maximos de blocos
    private final int windowSize; //Tamanho maximo da window
    private final FileInputStream fis; //Usado para ler o ficheiro
    private final int BlockSize; //Tamanho dos blocos de data
    private final long filesize; //Tamanho do ficheiro

    public SlidingWindow(int defaultWindowSize, int maxSize, String filename, int BlockSize) throws IOException {
        this.maxSize=maxSize;
        this.windowSize = Math.min(maxSize,defaultWindowSize); //Caso o maxSize seja maior que o tamanho default então o tamanho da window vai ser o maxSize
        File f = new File(filename);
        fis = new FileInputStream(f);
        this.BlockSize = BlockSize;
        this.filesize= f.length();

        window = new LinkedList<>();
        for (last=0;last<windowSize;last++){
            window.offer(new DataPlusBlock(getNextBlock(),last)); //Adicionar todos os blocos de data para a queue
        }
    }

    public int getWindowSize(){
        return windowSize;
    }

    public DataPlusBlock getNext(){ //Retira o proximo elemento da queue e ensere-o no fim da queue de novo
        DataPlusBlock next = window.remove();
        window.offer(next);
        return next;
    }


    public Queue<DataPlusBlock> update(int recieved) throws IOException { //Analisa os acks recebido (MUITO IMPORTANTE)
        Queue<DataPlusBlock> sendQueue = new LinkedList<>(); //Cria queue de pedidos que o Sender vai enviar para o Cliente
        if(!containsBlock(recieved)){ //Se o ack não está na queue então é repetido
            return sendQueue;
        }
        while (true) {
            DataPlusBlock retirado= window.remove();
            if (retirado.getBlock()==recieved) break;
            else {
                window.offer(retirado);
                sendQueue.offer(retirado);
            }
        }
        if (last<maxSize){
            DataPlusBlock dpb = new DataPlusBlock(getNextBlock(),last);
            window.offer(dpb);
            sendQueue.offer(dpb);
            last++;
        }
        return sendQueue;
    }

    private byte[] getNextBlock() throws IOException {
        byte[] blockContent;
        if(last==maxSize-1){
            blockContent = new byte[(int) filesize % BlockSize]; //Ultimo bloco
        }
        else{
        blockContent = new byte[BlockSize]; //Outros
        }
        fis.read(blockContent);
        return blockContent;
    }

    public boolean moveOut(){
        return windowSize>window.size()||windowSize<5;
    }

    public boolean containsBlock(int nBloco){
        for(DataPlusBlock dpb : window){
            if (dpb.getBlock()==nBloco) return true;
        }
        return false;
    }

    public boolean isEmpty(){
        return window.isEmpty();
    }
}