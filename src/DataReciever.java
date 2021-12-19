import packets.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;


class DataReciever implements Runnable {
    private final DatagramSocket socket;
    private int port;
    private InetAddress address;
    private final String fileName;
    private final String newFileName;

    public DataReciever(InetAddress address,int serverPort,String fileName,String newFileName) throws SocketException{
        this.address=address;
        this.port=serverPort;
        this.socket = new DatagramSocket();
        this.fileName=fileName;
        this.newFileName = newFileName;
    }

    public void sendPacket(UDP_Packet p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf,buf.length, address, port);
        socket.send(packet);
    }

    public int sendRRQ() throws IOException {
        WRQFile pacote;
        do {
            sendPacket(new RRQFile(fileName));
        } while ((pacote=getWRQ())==null);
        return pacote.getNBlocos();
    }

    public WRQFile getWRQ() throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            socket.setSoTimeout(300);
            socket.receive(packet);
            WRQFile pacote = new WRQFile(buf);
            if(pacote.isOK()){
                this.address = packet.getAddress();
                this.port = packet.getPort();
                return pacote;
            }
            else return null;
        }
        catch (SocketTimeoutException e) {
            return null;
        }
    }

    public boolean containsBlock(Queue<DataPlusBlock> queue, int nBloco){
        for(DataPlusBlock dpb : queue){
            if (dpb.getBlock()==nBloco) return true;
        }
        return false;
    }

    public void writeFile(int nrblocks) throws IOException{
        boolean sendFirst = true;
        socket.setSoTimeout(50);

        sendPacket(new ACK(-1));
        Queue<DataPlusBlock> waitingToWrite = new PriorityQueue<>();
        int next=0;

        File f = new File(newFileName);
        FileOutputStream output = new FileOutputStream(f, true);
        f.createNewFile(); //Unhandled

        while (true) {
            try {
                while (!(waitingToWrite.isEmpty()) && (waitingToWrite.element().getBlock()== next) ){
                    output.write(waitingToWrite.remove().getData());
                    output.flush();
                    next++;
                }
                if (next==nrblocks) break;

                byte[] buf = new byte[1200]; //Receber pacote
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                this.socket.receive(packet); //Receber pacote
                DATA pacote = new DATA(buf);
                if (pacote.isOK()) { //Verifica se é um pacote de DATA intacto
                    int blocoPacote = pacote.getNBloco();
                    sendFirst=false;
                    sendPacket(new ACK(blocoPacote));
                    if (blocoPacote>=next&&!containsBlock(waitingToWrite,blocoPacote)) {
                        waitingToWrite.add(new DataPlusBlock(pacote.getConteudo(),blocoPacote));
                    }
                }
            } catch (SocketTimeoutException ste){
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
