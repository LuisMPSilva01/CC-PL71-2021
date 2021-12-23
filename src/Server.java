import packets.*;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;

public class Server extends Thread {
    private final DatagramSocket socket;
    boolean running = true;
    private final File folder;
    private final List<Thread> threads = new ArrayList<>();
    private final int sizeBlock = 1200;
    private final LogsMaker logs;
    private final boolean showPL;
    private PacketLogs packetLogs;


    public Server(DatagramSocket socket, File folder, LogsMaker logs,boolean showPL) throws IOException {
        this.socket = socket;
        this.folder= folder;
        this.logs=logs;
        this.showPL=showPL;
        if(showPL) this.packetLogs= new PacketLogs("Servidor_packets.txt");
    }

    public void sendPacket(UDP_Packet p, InetAddress address, int port) throws IOException { //Envia pacote
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        if(showPL) packetLogs.sent(p.toLogInput());
    }

    public void getFilesInFolder(Map<String, LongTuple> m, File folder, String path) { //Obtem os ficheiros do folder fornecido
        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(m, fileEntry,(path+fileEntry.getName()+"/"));
            } else {
                LongTuple lt = new LongTuple(fileEntry.length(), fileEntry.lastModified());
                m.put(path + fileEntry.getName(), lt);
            }
        }
    }

    public int nrBlocksFILES(Map<String, LongTuple> map){ //Calcula o numero de blocos necessários para o FILES
        int block = 0;
        int total = 9;

        for(Map.Entry<String, LongTuple> entry: map.entrySet()){
            if(sizeBlock > total + 4 + entry.getKey().length() + 8 + 8){
                total += 4 + entry.getKey().length() + 8 + 8;
            }
            else{
                block++;
                total = 9 + 4 + entry.getKey().length() + 8 + 8;
            }
        }

        return block + 1;
    }

    public void sendFiles(Map<String, LongTuple> map,InetAddress address, int port) throws IOException { //Envia os pacotes de FILES e recebe confirmações
        int total = 9, block = 0;
        Map<String, LongTuple> FILES = new HashMap<>();

        for(Map.Entry<String, LongTuple> entry: map.entrySet()){
            if(sizeBlock > total + 4 + entry.getKey().length() + 8 + 8){ //Caso seja o ultimo pacote
                total += 4 + entry.getKey().length() + 8 + 8;
            }
            else{
                //send packet
                FILES files = new FILES(FILES, block);
                do {
                    sendPacket(files, address, port);
                } while (waitACK()!=block); //Envia o mesmo pacote enquanto não tiver confirmacao
                block++; //Acança o numero do bloco
                FILES.clear();
                total = 9 + 4 + entry.getKey().length() + 8 + 8;
            }
            FILES.put(entry.getKey(), entry.getValue());
        }
        FILES files = new FILES(FILES, block);
        int repetitions=5; //Possui um ciclo de saida alternativo com o repetitions com o intuito de poder sair caso perca o ultimo ack
        do {
            sendPacket(files, address, port);
            repetitions--;
        } while (waitACK()!=block&&repetitions!=0);
    }

    public Map<String, LongTuple> sendFolderName(InetAddress address, int port) throws IOException {
        Map<String, LongTuple> map = new HashMap<>();
        getFilesInFolder(map, folder, "");
        do {
            sendPacket(new FolderName(folder.getAbsolutePath(),nrBlocksFILES(map)),address,port); //Envia o FolderName enquanto não receber confirmação
        } while (waitACK()!=-1);
        return map;
    }

    public int waitACK() throws IOException { //Espera por um ACK
        try {
            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            ACK ack = new ACK(buf);
            if(ack.isOK()){
                if(showPL) packetLogs.received(ack.toLogInput());
                return ack.getNBloco();
            } else {
                if(showPL) packetLogs.received("bad ACK");
                return -2;
            }
        }
        catch (SocketTimeoutException e) {
            if(showPL) this.packetLogs.timeOut("ACK");
            return -2;
        }
    }

    public void analisePacket(byte[] array, InetAddress address, int port) throws IOException { //Analisa os pacotes de RRQFolder, RRQFile e FIN e ignora o resto
        UDP_Packet udpPacket;
        if((udpPacket=new RRQFolder(array)).isOK()){    //RRQFolder
            Map<String, LongTuple> map = sendFolderName(address,port);
            sendFiles(map,address, port);
            this.socket.setSoTimeout(2000); // Sendo o RRQFolder pode ser a ultima operação depois do FIN, este timeout vai fazer com que o servidor feche caso o fin se perca
        } else{
            if ((udpPacket=new RRQFile(array)).isOK()){  //RRQFile
                RRQFile pacote =new RRQFile(array);
                Thread ds = new Thread(new FT_Rapid_Sender(pacote,address,port,logs,showPL,packetLogs)); //Criar Sender para enviar ficheiro
                ds.start();
                threads.add(ds);
            } else {
                if ((udpPacket=new FIN(array)).isOK()){    //FIN
                    running=false;
                } else {
                    if(showPL) packetLogs.received(" Ignored packet");
                    return;
                }
            }
        } if(showPL) packetLogs.received(udpPacket.toLogInput());
    }

    public void run() {
        try {
            while (running) { //Enquanto corre, só fica a atender pedidos
                byte[] buf = new byte[1200];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    this.socket.receive(packet);
                }catch (SocketTimeoutException ste) { //Caso receba timeOut vai assumir que o FIN se perdeu
                    if(showPL) this.packetLogs.timeOut("FIN");
                    break;
                }
                analisePacket(buf, packet.getAddress(), packet.getPort());
            }

            for (Thread t: threads) { //Espera por todas as threads que criou
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(showPL) packetLogs.close(); //Fecha o leitor de escrita do packetLogs
            socket.close();
            System.out.println("Server closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}