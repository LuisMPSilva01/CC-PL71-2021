import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class UDPWindow {
    private Queue<Integer> window;
    private int last;
    private final int maxSize;
    private int windowSize;

    public UDPWindow(int defaultWindowSize,int maxSize){
        this.maxSize=maxSize;
        this.windowSize = Math.min(maxSize,defaultWindowSize);

        window = new LinkedList<>();
        for (int i=0;i<windowSize;i++){
            window.offer(i);
        }
        last=windowSize;
    }

    public int getWindowSize(){
        return windowSize;
    }

    public int getNext(){
        int next= window.remove();
        window.offer(next);
        return next;
    }


    public Queue<Integer> update(int recieved){
        Queue<Integer> sendQueue = new PriorityQueue<>();
        if(!window.contains(recieved)) return sendQueue;
        while (true) {
            int retirado= window.remove();
            if (retirado==recieved) break;
            else {
                window.offer(retirado);
                sendQueue.offer(retirado);
            }
        }
        if (last<maxSize){
            window.offer(last);
            sendQueue.offer(last);
            last++;
        }
        return sendQueue;
    }

    public boolean isEmpty(){
        return window.isEmpty();
    }
}