package packets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class PacketLogs {
    private FileOutputStream packetLogs;
    private ReentrantLock lock = new ReentrantLock();

    public PacketLogs(String name) throws IOException {
        File f = new File(name);
        f.delete();
        f.createNewFile();
        this.packetLogs = new FileOutputStream(name, true);
    }

    public void received(String msg) throws IOException {
        lock.lock();
        try {
            packetLogs.write(("Received "+msg+"\n").getBytes());
        } finally {
            lock.unlock();
        }
    }

    public void sent(String msg) throws IOException {
        lock.lock();
        try {
            packetLogs.write(("Sent "+msg+"\n").getBytes());
        } finally {
            lock.unlock();
        }
    }

    public void timeOut(String msg) throws IOException {
        lock.lock();
        try {
            packetLogs.write(("Time Out: "+msg+"\n").getBytes());
        } finally {
            lock.unlock();
        }
    }

    public void close() throws IOException {
        lock.lock();
        try {
            packetLogs.close();
        } finally {
            lock.unlock();
        }
    }
}
