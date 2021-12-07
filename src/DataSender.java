import packets.DATA;
import packets.Pacote;
import packets.RRQFile;
import packets.WRQFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


class DataSender implements Runnable {
    private DatagramSocket socket;
    private final int port;
    private final InetAddress address;
    private String fileName;
    private final int datablock = 1195;
    private final int timeOut = 1000;

    public DataSender(RRQFile rrqFile,InetAddress address,int port) throws SocketException, UnknownHostException {
        this.address=address;
        this.port=port;
        this.socket = new DatagramSocket();
        this.fileName=rrqFile.getFileName();
        this.socket.setSoTimeout(timeOut);
    }

    public void sendPacket(Pacote p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public int sendWRQ(File file) throws IOException {
        int nrblocks = blocksNeeded(file.length());
        do {
            sendPacket(new WRQFile(nrblocks),address,port);
        } while (!waitPacket(5));
        return nrblocks;
    }

    public int blocksNeeded(Long l){
        return (int) (Math.floorDiv(l, datablock) + 1);
    }

    public boolean waitPacket(int identifier) throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            return buf[0] == identifier;
        }
        catch (SocketTimeoutException e) {
            return false;
        }
    }

    public void sendFile(String fileName, int nBlocos, InetAddress address, int port) throws IOException{
        File f = new File(fileName);
        byte[] fileContent = Files.readAllBytes(f.toPath());

        for(int i = 0; i < nBlocos; i++){
            byte[] blockContent;
            if(i == nBlocos - 1){
                blockContent = new byte[fileContent.length - (i * datablock)];
            }
            else{
                blockContent = new byte[datablock];
            }
            System.arraycopy(fileContent, i * datablock, blockContent, 0, blockContent.length);
            DATA d = new DATA(i + 1, blockContent);
            sendPacket(d, address, port);
        }
    }
    public void run(){
        try {
            File f = new File(this.fileName);
            int nrBlocks = sendWRQ(f);
            sendFile(fileName,nrBlocks,address,port);
            this.socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}