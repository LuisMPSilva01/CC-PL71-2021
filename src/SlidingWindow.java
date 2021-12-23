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
        if(!containsBlock(recieved)){ //Se o ack não está na queue então é repetido/duplicado sendo que terá de ser ignorado
            return sendQueue;
        }
        while (true) {
            DataPlusBlock retirado= window.remove(); //Retira a cabeça do queue
            if (retirado.getBlock()==recieved) break;  //Só para quando encontrar o bloco correspondente ao do ack
            else { //Reenviar todos os elementos da queue que não são correspondem ao ack
                window.offer(retirado);
                sendQueue.offer(retirado);
            }
        }
        if (last<maxSize){ //Caso o last seja igual ao maxSize então não se vai ler novos blocos de data
            DataPlusBlock dpb = new DataPlusBlock(getNextBlock(),last); //ler proximo bloco de data
            window.offer(dpb); //Adiciona-lo a windoh
            sendQueue.offer(dpb); //Adiciona-lo a queue
            last++; //Avançao o maior elemento em 1
        }
        return sendQueue;
    }

    private byte[] getNextBlock() throws IOException { //Le o proximo bloco
        byte[] blockContent;
        if(last==maxSize-1){
            blockContent = new byte[(int) filesize % BlockSize]; //Ultimo bloco
        }
        else{
        blockContent = new byte[BlockSize]; //Outros
        }
        fis.read(blockContent); //Le conteudo
        return blockContent;
    }

    public boolean moveOut(){
        return windowSize>window.size()||windowSize<5; //Condição para sair do ciclo de enviar pacotes caso se percam acks
    }

    public boolean containsBlock(int nBloco){ //Verifica se o bloco recebido do ack corresponde a algum dos blocos de data da lista
        for(DataPlusBlock dpb : window){
            if (dpb.getBlock()==nBloco) return true;
        }
        return false;
    }

    public boolean isEmpty(){
        return window.isEmpty();
    }
}