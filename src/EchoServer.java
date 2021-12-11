import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

import packets.*;

public class EchoServer extends Thread {
    private final DatagramSocket socket;
    boolean running = true;
    public File folder;
    private final int SO=1; //Linux -> 0 | Windows -> everything else

    public EchoServer(DatagramSocket socket,File folder) throws SocketException{
        this.socket = socket;
        this.folder= folder;
    }

    public void sendPacket(Pacote p, InetAddress address, int port) throws IOException {
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
        sendPacket(files, address, port);
    }


    public void analisePacket(byte[] array, InetAddress address, int port) throws IOException {
        Pacote pacote;
        switch (array[0]) {
            case 1://RRQFolder
                System.out.println("RRQFolder");
                sendPacket(new FolderName(folder.getAbsolutePath()),address,port);
                sendFILES(address, port);
                break;
            case 2://RRQFile
                System.out.println("RRQFile");
                pacote=new RRQFile(array);
                RRQFile tmp = (RRQFile) pacote;
                System.out.println("identify: " + tmp.getFileName());
                Thread ds = new Thread(new DataSender((RRQFile) pacote,address,port));
                ds.start();
                break;
            case 3: //WRQFile
                System.out.println("WRQFile");
            case 4: //DATA
                System.out.println("DATA");
            case 5://ACK
                System.out.println("ACK");
                break;
            case 6://FILES
                System.out.println("Packet recieved FILES");
                break;
            case 7://FIN
                System.out.println("Packet recieved FIN");
                running=false;
            case 8://ServerFolderName
                System.out.println("Packet recieved ServerFolderName");
                break;
            default:
        }
    }

    public void run() {
        while (running) {
            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                this.socket.receive(packet);
                analisePacket(buf, packet.getAddress(), packet.getPort());
            } catch (IOException fnfe) {
                fnfe.printStackTrace();
            }
        }
        socket.close();
    }
}