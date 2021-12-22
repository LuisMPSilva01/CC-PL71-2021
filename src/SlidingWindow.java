import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class SlidingWindow {
    private final Queue<DataPlusBlock> window;
    private int last;
    private final int maxSize;
    private final int windowSize;
    private final FileInputStream fis;
    private final int BlockSize;
    private final long filesize;

    public SlidingWindow(int defaultWindowSize, int maxSize, String filename, int BlockSize) throws IOException {
        this.maxSize=maxSize;
        this.windowSize = Math.min(maxSize,defaultWindowSize);
        File f = new File(filename);
        fis = new FileInputStream(f);
        this.BlockSize = BlockSize;
        this.filesize= f.length();

        window = new LinkedList<>();
        for (last=0;last<windowSize;last++){
            window.offer(new DataPlusBlock(getNextBlock(),last));
        }
    }

    public int getWindowSize(){
        return windowSize;
    }

    public DataPlusBlock getNext(){
        DataPlusBlock next = window.remove();
        window.offer(next);
        return next;
    }


    public Queue<DataPlusBlock> update(int recieved) throws IOException {
        Queue<DataPlusBlock> sendQueue = new LinkedList<>();
        if(!containsBlock(recieved)){
            sendQueue.add(new DataPlusBlock(new byte[1],-1));
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