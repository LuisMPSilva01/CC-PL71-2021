import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

import packets.*;

public class EchoServer extends Thread {
    private final DatagramSocket socket;
    boolean running = true;
    private final File folder;
    private final List<Thread> threads = new ArrayList<>();
    public int nThreads=0;

    public EchoServer(DatagramSocket socket,File folder) throws SocketException{
        this.socket = socket;
        this.folder= folder;
    }

    public void sendPacket(UDP_Packet p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public static void getFilesInFolder(Map<String, Long> m, File folder, String path) {
        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(m, fileEntry,(path+fileEntry.getName()+"||"));
            } else {
                m.put(path + fileEntry.getName(), fileEntry.length());
            }
        }
    }

    public void sendFILES(InetAddress address, int port) throws IOException {
        HashMap<String, Long> map = new HashMap<>();
        getFilesInFolder(map, folder, "");

        FILES files = new FILES(map);

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
        if((new RRQFolder(array)).isOK()){
            System.out.println("Packet recieved RRQFolder");
            sendFolderName(address,port);
            sendFILES(address, port);
            //this.socket.setSoTimeout(2000); // Sendo o RRQFolder pode ser a ultima operação depois do FIN, este timeout vai fazer com que o servidor feche caso o fin se perca
        } else{
            if ((new RRQFile(array)).isOK()){
                System.out.println("Packet recieved RRQFile");
                RRQFile pacote =new RRQFile(array);
                Thread ds = new Thread(new DataSender(pacote,address,port));
                nThreads++;
                ds.start();
                threads.add(ds);
            } else {
                if ((new FIN(array)).isOK()){
                    System.out.println("Packet recieved FIN");
                    running=false;
                } else System.out.println("Pacote recebido ignorado");
            }
        }
    }

    public void run() {
        while (running) {
            try {
                byte[] buf = new byte[1200];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    this.socket.receive(packet);
                }catch (SocketTimeoutException ste) {
                    System.out.println("Took to long to recieve fin");
                    break;
                }
                analisePacket(buf, packet.getAddress(), packet.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Thread t: threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }
}