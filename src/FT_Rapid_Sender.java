import packets.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;


class FT_Rapid_Sender implements Runnable {
    private final DatagramSocket socket;
    private final int port;
    private final InetAddress address;
    private final String fileName;
    private final int datablock = 1187;
    private int defaultWindowSize=25;
    private LogsMaker logs;
    private boolean showPL;
    private PacketLogs packetLogs;

    public FT_Rapid_Sender(RRQFile rrqFile, InetAddress address, int port, LogsMaker logs, boolean showPL, PacketLogs packetLogs) throws SocketException {
        this.address=address;
        this.port=port;
        this.socket = new DatagramSocket();
        this.fileName=rrqFile.getFileName();
        this.logs=logs;
        this.showPL=showPL;
        if(showPL) this.packetLogs=packetLogs;
    }

    public void sendPacket(UDP_Packet p, InetAddress address, int port) throws IOException { //Envia pacote de DATA
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        if(showPL) this.packetLogs.sent(p.toLogInput());
    }

    public int sendWRQ(File file) throws IOException { //Envia paacote WRQFile e espera por confirmação (ACK(-1))
        int nrblocks = blocksNeeded(file.length());
        socket.setSoTimeout(100);
        int repetitions=3;
        int ack;
        do {
            repetitions--;
            sendPacket(new WRQFile(nrblocks), address, port);
        } while ((ack=waitACK())!=-1&&repetitions!=0);
        if(ack==-1) return nrblocks;
        else return -1;
    }

    public int blocksNeeded(Long l){ //Calcula o numero de blocos necessários para enviar a data toda
        return (int) (Math.floorDiv(l, datablock) + 1);
    }

    public int waitACK() throws IOException { //Espera por um ack e retorna o seu identificador
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

    public void sendDataBlock(DataPlusBlock dpb,InetAddress address, int port) throws IOException { //Envia pacote de DATA
        DATA d = new DATA(dpb.getBlock(), dpb.getData()); //Guardar conteudo num DATA packet
        sendPacket(d, address, port); //Enviar pacote
    }

    public void sendFile(String fileName, int nBlocos, InetAddress address, int port) throws IOException{ //Algoritmo para enviar data
        socket.setSoTimeout(15);
        SlidingWindow windoh = new SlidingWindow(defaultWindowSize,nBlocos,fileName,datablock); //Inicializa slidingWindow

        for (int i=0;i<windoh.getWindowSize();i++) { //Envia todos os pacotes de DATA que cabem na window
            sendDataBlock(windoh.getNext(),address,port);
        }

        boolean moveOut = false; //Variavel vai definir quando o cicla acaba caso haja falha de confirmações
        int timeOuts = Math.max(windoh.getWindowSize(),5); //Ajuda a definir quando o cicla acaba caso haja falha de confirmações

        Queue<DataPlusBlock> sendQueue = new LinkedList<>();
        while (!windoh.isEmpty()) {  //Repetições vai ser usado na ultima iteração para quebrar o ciclo caso não receba o ultimo ack(pode ter sido perdido)
            while (!sendQueue.isEmpty()) { //Envia todos os pacotes na queue
                sendDataBlock(sendQueue.remove(), address, port);
            }

            try {
                moveOut=windoh.moveOut(); //Verifica condições para saida do ciclo
                byte[] dataRecieved = new byte[1200];
                DatagramPacket packet = new DatagramPacket(dataRecieved, 1200);
                socket.receive(packet);
                ACK ack = new ACK(dataRecieved);
                if (ack.isOK()) {
                    if(showPL) this.packetLogs.received(ack.toLogInput());
                    int ackNumber = ack.getNBloco();
                    sendQueue = windoh.update(ackNumber);
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
            if(nrBlocks!=-1){
                sendFile(fileName,nrBlocks,address,port);
                this.socket.close();
                logs.enviado(fileName);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}