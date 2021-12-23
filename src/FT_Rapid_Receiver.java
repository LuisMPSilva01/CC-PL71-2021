import packets.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.PriorityQueue;
import java.util.Queue;


class FT_Rapid_Receiver implements Runnable {
    private final DatagramSocket socket;
    private int port;
    private InetAddress address;
    private final String fileName;
    private final String newFileName;
    private final LogsMaker logs;
    private final boolean showPL;
    private PacketLogs packetLogs;

    public FT_Rapid_Receiver(InetAddress address, int serverPort, String fileName, String newFileName, LogsMaker logs, boolean showPL, PacketLogs packetLogs) throws SocketException{
        this.address=address;
        this.port=serverPort;
        this.socket = new DatagramSocket();
        this.fileName=fileName;
        this.newFileName = newFileName;
        this.logs=logs;
        this.showPL=showPL;
        if(showPL) this.packetLogs=packetLogs;
    }

    public void sendPacket(UDP_Packet p) throws IOException { //Envia pacote
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf,buf.length, address, port);
        socket.send(packet);
        if(showPL) this.packetLogs.sent(p.toLogInput());
    }

    public int sendRRQ() throws IOException { //Envia RRQFile
        WRQFile pacote;
        do {
            sendPacket(new RRQFile(fileName));
        } while ((pacote=getWRQ())==null);
        return pacote.getNBlocos();
    }

    public WRQFile getWRQ() throws IOException { //Espera pelo WRQFile
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            socket.setSoTimeout(300);
            socket.receive(packet);
            WRQFile pacote = new WRQFile(buf);
            if(pacote.isOK()){ //Verifica integridade
                if(showPL) this.packetLogs.received(pacote.toLogInput());
                this.address = packet.getAddress();
                this.port = packet.getPort();
                return pacote;
            }
            else {
                if(showPL) this.packetLogs.received("Bad WRQFile");
                return null;
            }
        }
        catch (SocketTimeoutException e) {
            if(showPL) this.packetLogs.timeOut("WRQFile");
            return null;
        }
    }

    public boolean containsBlock(Queue<DataPlusBlock> queue, int nBloco){ //Verifica se o nBloco está contido na queue
        for(DataPlusBlock dpb : queue){
            if (dpb.getBlock()==nBloco) return true;
        }
        return false;
    }

    public void writeFile(int nrblocks) throws IOException{ //Recebe pacotes de DATA e guarda ficheiro
        boolean sendFirst = true; //Envia confirmação do WRQFile se tiver a true e fica a falso quando receber DATA
        socket.setSoTimeout(50);

        sendPacket(new ACK(-1)); //Confirma o pacote WRQFile
        Queue<DataPlusBlock> waitingToWrite = new PriorityQueue<>(); //Queue de blocos de data
        int next=0;

        File f = new File(newFileName);
        f.delete(); //Apaga ficheiro se já existir
        f.createNewFile();
        FileOutputStream output = new FileOutputStream(f, true);

        while (true) {
            try {
                while (!(waitingToWrite.isEmpty()) && (waitingToWrite.element().getBlock()== next) ){
                    output.write(waitingToWrite.remove().getData());  //Escreve bloco de data se a cabeça for o proximo numero de bloco
                    output.flush();
                    next++;
                }
                if (next==nrblocks) break;

                byte[] buf = new byte[1200]; //Receber pacote
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                this.socket.receive(packet); //Receber pacote
                DATA pacote = new DATA(buf);
                if (pacote.isOK()) { //Verifica se é um pacote de DATA intacto
                    if(showPL) this.packetLogs.received(pacote.toLogInput());
                    int blocoPacote = pacote.getNBloco();
                    sendFirst=false;
                    sendPacket(new ACK(blocoPacote));
                    if (blocoPacote>=next&&!containsBlock(waitingToWrite,blocoPacote)) { //Guarda data se não estiver na queue e tiver um identificador superior ao next
                        waitingToWrite.add(new DataPlusBlock(pacote.getConteudo(),blocoPacote));
                    }
                } else if(showPL) this.packetLogs.received("Bad DATABLOCK");
            } catch (SocketTimeoutException ste){
                if(showPL) this.packetLogs.timeOut("DATA packet");
                if(sendFirst) sendPacket(new ACK(-1));
            }
        }
        output.close();
    }


    @Override
    public void run(){
        try {
            int nrBlocks = sendRRQ();
            writeFile(nrBlocks);
            this.socket.close();
            this.logs.recebido(newFileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}