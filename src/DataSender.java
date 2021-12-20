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
    private int defaultWindowSize=25;
    private Logs logs;
    private boolean showPL;
    private PacketLogs packetLogs;

    public DataSender(RRQFile rrqFile,InetAddress address,int port,Logs logs,boolean showPL,PacketLogs packetLogs) throws SocketException {
        this.address=address;
        this.port=port;
        this.socket = new DatagramSocket();
        this.fileName=rrqFile.getFileName();
        this.logs=logs;
        this.showPL=showPL;
        if(showPL) this.packetLogs=packetLogs;
    }

    public void sendPacket(UDP_Packet p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        if(showPL) this.packetLogs.sent(p.toLogInput());
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
                if(showPL) this.packetLogs.received(pacote.toLogInput());
                ACK ack = new ACK(buf);
                return ack.getNBloco();
            } else {
                if(showPL) this.packetLogs.received("Bad ack");
                return -2;
            }
        }
        catch (SocketTimeoutException e) {
            if(showPL) this.packetLogs.timeOut("ACK");
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
                    if(showPL) this.packetLogs.received(ack.toLogInput());
                    int ackNumber = ack.getNBloco();
                    sendQueue = windoh.update(ackNumber);
                    if (sendQueue.isEmpty()) moveOut = true;
                } else if(showPL) this.packetLogs.received("Bad ack");
            } catch (SocketTimeoutException ste) {
                if(showPL) this.packetLogs.timeOut("ACK");
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
            logs.enviado(fileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}