import packets.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;


class DataSender implements Runnable {
    private final DatagramSocket socket;
    private final int port;
    private final InetAddress address;
    private final String fileName;
    private final int datablock = 1187;
    int defaultWindowSize=25;

    public DataSender(RRQFile rrqFile,InetAddress address,int port) throws SocketException {
        this.address=address;
        this.port=port;
        this.socket = new DatagramSocket();
        this.fileName=rrqFile.getFileName();
    }

    public void sendPacket(UDP_Packet p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public int sendWRQ(File file) throws IOException {
        int nrblocks = blocksNeeded(file.length());
        do {
            sendPacket(new WRQFile(nrblocks), address, port);
        } while (waitACK()!=-1);
        return nrblocks;
    }

    public int blocksNeeded(Long l){
        return (int) (Math.floorDiv(l, datablock) + 1);
    }

    public int waitACK() throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            ACK pacote = new ACK(buf);
            if(pacote.isOK()){
                ACK ack = new ACK(buf);
                return ack.getNBloco();
            } else {
                return -2;
            }
        }
        catch (SocketTimeoutException e) {
            return -2;
        }
    }

    public void sendDataBlock(DataPlusBlock dpb,InetAddress address, int port) throws IOException {
        DATA d = new DATA(dpb.getBlock(), dpb.getData()); //Guardar conteudo num DATA packet
        sendPacket(d, address, port); //Enviar pacote
    }

    public void sendFile(String fileName, int nBlocos, InetAddress address, int port) throws IOException{
        socket.setSoTimeout(10);
        UDPWindow windoh = new UDPWindow(defaultWindowSize,nBlocos,fileName,datablock);

        for (int i=0;i<windoh.getWindowSize();i++) { //Sends first wave
            sendDataBlock(windoh.getNext(),address,port);
        }

        boolean moveOut = false;
        int timeOuts = windoh.getWindowSize()/2;

        Queue<DataPlusBlock> sendQueue = new LinkedList<>();
        while (!windoh.isEmpty()) {  //Repetições vai ser usado na ultima iteração para quebrar o ciclo caso não receba o ultimo ack(pode ter sido perdido)
            while (!sendQueue.isEmpty()) {
                DataPlusBlock nextValue = sendQueue.remove();
                if (nextValue.getBlock()!=-1){
                    sendDataBlock(nextValue, address, port);
                }
            }

            try {
                byte[] dataRecieved = new byte[1200];
                DatagramPacket packet = new DatagramPacket(dataRecieved, 1200);
                socket.receive(packet);
                ACK ack = new ACK(dataRecieved);
                if (ack.isOK()) {
                    int ackNumber = ack.getNBloco();
                    sendQueue = windoh.update(ackNumber);
                    if (sendQueue.isEmpty()) moveOut = true;
                }
            } catch (SocketTimeoutException ste) {
                sendQueue.add(windoh.getNext());
                if (moveOut){
                    timeOuts--;
                    if (timeOuts==0) break;
                }
            }
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