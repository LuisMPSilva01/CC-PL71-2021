import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import packets.*;

public class EchoClient extends Thread{
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int defaultPort;
    private final int SO=1; //Linux -> 0 | Windows -> everything else


    public EchoClient(int defaultPort) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        this.defaultPort=defaultPort;
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, defaultPort);
        socket.send(packet);
    }

    Map<String, Long> decodeFILES(byte[] buf) {
        Map<String, Long> m = new HashMap<>();

        for(int i = 1; i < buf.length && buf[i] != -1; ){
            byte[] s_fn = new byte[4];
            System.arraycopy(buf, i, s_fn, 0, 4);
            int size_filename = ByteBuffer.wrap(s_fn).getInt();
            i += 4;

            String filename = new String(buf, i, size_filename, StandardCharsets.UTF_8);
            i += size_filename;

            byte[] fs = new byte[8];
            System.arraycopy(buf, i, fs, 0, 8);
            Long filesize = ByteBuffer.wrap(fs).getLong();
            i += 8;

            m.put(filename, filesize);
        }

        return m;
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

    public static Map<String, Long> partSynchronized(Map<String, Long> mine, Map<String, Long> other){
        Map<String, Long> res = new HashMap<>();
        Long l;

        for(Map.Entry<String, Long> entry: other.entrySet()){
            if((l = mine.get(entry.getKey())) != null){
                if(!entry.getValue().equals(l)){
                    res.put(entry.getKey(), entry.getValue());
                }
            }
            else{
                res.put(entry.getKey(), entry.getValue());
            }
        }

        return res;
    }

    public void getFilesHandler(Pacote pacote, File myFolder, File otherFolder) throws IOException, InterruptedException {
        Map<String, Long> mine = new HashMap<>();
        getFilesInFolder(mine, myFolder, "");
        Map<String, Long> other = decodeFILES(pacote.getContent());
        Map<String, Long> missing = partSynchronized(mine, other);
        Thread[] missingFiles = new Thread[missing.size()];
        int i=0;
        System.out.println("size: " + missing.size());
        if(SO==0){
            for(Map.Entry<String, Long> entry: missing.entrySet()){
                String fileName = otherFolder.getAbsolutePath() + "/" + entry.getKey();
                String newFileName = myFolder.getAbsolutePath() + "/" + entry.getKey();
                missingFiles[i] = new Thread(new DataReciever(InetAddress.getByName("localhost"), defaultPort, fileName, newFileName, entry.getValue()));
                missingFiles[i].start();
                i++;
            }
        } else {
            for(Map.Entry<String, Long> entry: missing.entrySet()){
                String fileName = otherFolder.getAbsolutePath() + "\\" + entry.getKey();
                String newFileName = myFolder.getAbsolutePath() + "\\" + entry.getKey();
                missingFiles[i] = new Thread(new DataReciever(InetAddress.getByName("localhost"), defaultPort, fileName, newFileName, entry.getValue()));
                missingFiles[i].start();
                i++;
            }
        }

        for(int j=0;j<i;j++){
            missingFiles[j].join();
        }
    }

    public Pacote analisePacket(byte[] array) throws IOException {
        Pacote pacote = new Pacote(1200);
        switch (array[0]) {
            case 1: //RRQFolder
                System.out.println("RRQFolder");
                break;
            case 2: //RRQFile
                System.out.println("RRQFile");
                break;
            case 3: //WRQFile
                System.out.println("WRQFile");
                break;
            case 4: //DATA
                System.out.println("DATA");
                break;
            case 5: //ACK
                System.out.println("ACK");
                break;
            case 6: //FILES
                System.out.println("Packet recieved FILES");
                pacote = new FILES(array);
                break;
            case 7: //FIN
                System.out.println("Packet recieved FIN");
                break;
            default:
                return null;
        }
        return pacote;
    }

    public void run() {
        File folder1,folder2;
        if (SO==0){
            folder1 = new File("/home/ray/Downloads/teste3");
            folder2 = new File("/home/ray/Downloads/teste2");
        }else {
            folder1 = new File("C:\\Users\\Acer\\Desktop\\teste1");
            folder2 = new File("C:\\Users\\Acer\\Desktop\\teste2");
        }

        Map<String, Long> m = new HashMap<>();
        Map<String, Long> m2 = new HashMap<>();

        getFilesInFolder(m, folder1, "");
        getFilesInFolder(m2, folder2, "");

        RRQFolder rrqf = new RRQFolder(folder2.getAbsolutePath());

        try{
            sendPacket(rrqf);

            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.socket.receive(packet);

            Pacote pacote = analisePacket(buf);
            getFilesHandler(pacote, folder1, folder2);

            sendPacket(new FIN());
        }
        catch(IOException | InterruptedException ioe){
            ioe.printStackTrace();
        }
        this.socket.close();
    }
}