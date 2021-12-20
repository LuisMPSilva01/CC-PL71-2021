import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import packets.*;

public class Client extends Thread{
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int defaultPort;
    private String serverFolder;
    private File folder;
    private final int SO=0; //Linux -> 0 | Windows -> everything else
    private LogsMaker logs;
    private boolean showPL;
    private PacketLogs packetLogs;


    public Client(int defaultPort, InetAddress address, File folder, LogsMaker logs, boolean showPL) throws IOException {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(100);        //TIMEOUT
        this.address = address;
        this.defaultPort=defaultPort;
        this.folder=folder;
        this.logs=logs;
        this.showPL=showPL;
        if(showPL) this.packetLogs= new PacketLogs("Cliente_packets.txt");
    }

    public void sendPacket(UDP_Packet p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, defaultPort);
        socket.send(packet);
        if(showPL) this.packetLogs.sent(p.toLogInput());
    }

    Map<String, LongTuple> decodeFILES(byte[] buf) {
        Map<String, LongTuple> m = new HashMap<>();

        for(int i = 9; i < buf.length && buf[i] != -1; ){
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

            byte[] lmd = new byte[8];
            System.arraycopy(buf, i, lmd, 0, 8);
            Long lastModifiedDate = ByteBuffer.wrap(lmd).getLong();
            i += 8;

            LongTuple lt = new LongTuple(filesize, lastModifiedDate);

            m.put(filename, lt);
        }

        return m;
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

    public static Map<String, LongTuple> partSynchronized(Map<String, LongTuple> mine, Map<String, LongTuple> other){
        Map<String, LongTuple> res = new HashMap<>();

        for(Map.Entry<String, LongTuple> entry: other.entrySet()){
            LongTuple lt;
            if((lt = mine.get(entry.getKey())) != null){
                Long my_lmd = lt.getB();
                Long lmd = entry.getValue().getB();

                if(my_lmd < lmd)
                    res.put(entry.getKey(), entry.getValue());
            }
            else{
                res.put(entry.getKey(), entry.getValue());
            }
        }

        return res;
    }

    public void getFilesHandler(FILES pacote, File myFolder) throws IOException, InterruptedException {
        Map<String, LongTuple> mine = new HashMap<>();
        getFilesInFolder(mine, myFolder, "");
        Map<String, LongTuple> other = decodeFILES(pacote.getContent());
        Map<String, LongTuple> missing = partSynchronized(mine, other);
        Thread[] missingFiles = new Thread[missing.size()];
        int i=0;
        System.out.println("size: " + missing.size());
        if(SO==0){
            for(Map.Entry<String, LongTuple> entry: missing.entrySet()){
                String fileName = serverFolder + "/" + entry.getKey();
                String newFileName = myFolder.getAbsolutePath() + "/" + entry.getKey();
                System.out.println("new Filename: " + newFileName);
                System.out.println("Filename: " + fileName);
                missingFiles[i] = new Thread(new FT_Rapid_Receiver(address, defaultPort, fileName, newFileName,logs,showPL,packetLogs));
                missingFiles[i].start();
                i++;
            }
        } else {
            for(Map.Entry<String, LongTuple> entry: missing.entrySet()){
                String fileName = serverFolder + "\\" + entry.getKey();
                String newFileName = myFolder.getAbsolutePath() + "\\" + entry.getKey();
                missingFiles[i] = new Thread(new FT_Rapid_Receiver(address, defaultPort, fileName, newFileName,logs,showPL,packetLogs));
                missingFiles[i].start();
                i++;
            }
        }

        for(int j=0;j<i;j++){
            missingFiles[j].join();
        }
    }

    public FILES waitFILES(int nrBlocos) throws IOException{
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        int nBloco=-1; //Numero
        boolean sendACK = true;

        while (true){ //Ciclo para esperar o pacote do FILES
            if(sendACK){  //Caso na iteração anterior tenha recebido o pacote errado, não vai enviar ack
                sendPacket(new ACK(nBloco)); //Enviar o ack para desbloquear o FILES
            } else sendACK=true;
            nBloco++;
            //if((nrblocks)==nBloco) break; //Caso seja o ultimo pacote sai do ciclo

            try { //Caso de sucesso
                this.socket.receive(packet); //Receber pacote
                FILES pacote = new FILES(buf);
                if (pacote.isOK()) { //Verifica se é um pacote de FILES
                    if(showPL) this.packetLogs.received(pacote.toLogInput());
                    if (pacote.getNbloco()==nBloco) {
                        sendPacket(new ACK(nBloco)); //Enviar o ack para desbloquear o FILES
                        return new FILES(buf);
                    } else {
                        sendACK=false;
                        nBloco--;
                    }
                } else {
                    nBloco--;
                }
            } catch (SocketTimeoutException e) {
                if(showPL) this.packetLogs.timeOut("FILES");
                nBloco--;
            }
        }
    }

    public int waitFolderName(RRQFolder rrqf) throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        FolderName fn;
        do { //Ciclo para esperar o pacote do folder name
            try {
                sendPacket(rrqf);
                this.socket.receive(packet);
            }catch (SocketTimeoutException ignored){
                if(showPL) this.packetLogs.timeOut("Foldername");
            }
            fn = new FolderName(buf);
            if(showPL) this.packetLogs.received("bad foldername");
        } while (!fn.isOK()); //Verificação do pacote

        if(showPL) this.packetLogs.received(fn.toLogInput());
        this.serverFolder = fn.getFolderName();
        return fn.getFilesBlocks();
    }

    public FILES waitFILESandName(RRQFolder rrqf){
        try {
            int nBlocos=waitFolderName(rrqf); //Guarda o nome do folder

            return waitFILES(nBlocos);
        } catch (IOException ioe){
            return null;
        }
    }

    public void run() {
        Map<String, LongTuple> m = new HashMap<>();
        getFilesInFolder(m, folder, "");

        RRQFolder rrqf = new RRQFolder();
        try{
            FILES files;
            files=waitFILESandName(rrqf);
            
            getFilesHandler(files, folder);

            sendPacket(new FIN());
        }
        catch(IOException | InterruptedException ioe){
            ioe.printStackTrace();
        }
        this.socket.close();
    }
}
