import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;

import packets.*;

public class EchoServer extends Thread {
    private final DatagramSocket socket;
    boolean running = true;
    private final File folder;
    private final List<Thread> threads = new ArrayList<>();
    private int sizeBlock = 1200;
    private Logs logs;
    private boolean showPL;
    private PacketLogs packetLogs;


    public EchoServer(DatagramSocket socket, File folder, Logs logs,boolean showPL) throws IOException {
        this.socket = socket;
        this.folder= folder;
        this.logs=logs;
        this.showPL=showPL;
        if(showPL) this.packetLogs= new PacketLogs("Servidor_packets.txt");
    }

    public void sendPacket(UDP_Packet p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        if(showPL) packetLogs.sent(p.toLogInput());
    }

    public static void getFilesInFolder(Map<String, LongTuple> m, File folder, String path) {
        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(m, fileEntry,(path+fileEntry.getName()+"||"));
            } else {
                LongTuple lt = new LongTuple(fileEntry.length(), fileEntry.lastModified());
                m.put(path + fileEntry.getName(), lt);
            }
        }
    }

    public int nrBlocksFILES(Map<String, LongTuple> map){
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

    public void sendFILES(InetAddress address, int port) throws IOException {
        Map<String, LongTuple> map = new HashMap<>();
        getFilesInFolder(map, folder, "");
        int nrBlocksFILES = nrBlocksFILES(map);
        int total = 9, block = 0;
        Map<String, LongTuple> FILES = new HashMap<>();

        for(Map.Entry<String, LongTuple> entry: map.entrySet()){
            if(sizeBlock > total + 4 + entry.getKey().length() + 8 + 8){
                total += 4 + entry.getKey().length() + 8 + 8;
                FILES.put(entry.getKey(), entry.getValue());
            }
            else{
                //send packet
                block++;
                FILES files = new FILES(FILES, block, nrBlocksFILES);
                do {
                    sendPacket(files, address, port);
                } while (waitACK()!=0);
                FILES.clear();
                total = 9 + 4 + entry.getKey().length() + 8 + 8;
                FILES.put(entry.getKey(), entry.getValue());
            }
        }
        block ++;
        FILES files = new FILES(FILES, block, nrBlocksFILES);
        do {
            sendPacket(files, address, port);
        } while (waitACK()!=0);
    }

    public void sendFolderName(InetAddress address, int port) throws IOException {
        do {
            sendPacket(new FolderName(folder.getAbsolutePath(),1),address,port);
        } while (waitACK()!=-1);
    }

    public int waitACK() throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            ACK ack = new ACK(buf);
            if(ack.isOK()){
                if(showPL) packetLogs.received(ack.toLogInput());
                return ack.getNBloco();
            } else {
                return -2;
            }
        }
        catch (SocketTimeoutException e) {
            return -2;
        }
    }

    public void analisePacket(byte[] array, InetAddress address, int port) throws IOException {
        UDP_Packet udpPacket;
        if((udpPacket=new RRQFolder(array)).isOK()){
            sendFolderName(address,port);
            sendFILES(address, port);
            this.socket.setSoTimeout(2000); // Sendo o RRQFolder pode ser a ultima operação depois do FIN, este timeout vai fazer com que o servidor feche caso o fin se perca
        } else{
            if ((udpPacket=new RRQFile(array)).isOK()){
                System.out.println("Packet recieved RRQFile");
                RRQFile pacote =new RRQFile(array);
                Thread ds = new Thread(new DataSender(pacote,address,port,logs,showPL,packetLogs));
                ds.start();
                threads.add(ds);
            } else {
                if ((udpPacket=new FIN(array)).isOK()){
                    System.out.println("Packet recieved FIN");
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
            while (running) {
                byte[] buf = new byte[1200];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    this.socket.receive(packet);
                }catch (SocketTimeoutException ste) {
                    System.out.println("Took to long to recieve fin");
                    break;
                }
                analisePacket(buf, packet.getAddress(), packet.getPort());
            }
            for (Thread t: threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            packetLogs.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}