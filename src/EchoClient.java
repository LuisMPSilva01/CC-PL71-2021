import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import packets.*;

public class EchoClient extends Thread{
    private DatagramSocket socket;
    private InetAddress address;
    private final int datablock = 1195;


    public EchoClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8888);
        socket.send(packet);
        return;
    }

    public int blocksNeededFILES(Map<String, Long> m){
        int r = 0;
        r += m.size() * 12;
        for(Map.Entry<String, Long> entry: m.entrySet())
            r += entry.getKey().length();

        int blocksNeeded = Math.floorDiv(r, datablock) + 1;
        r += blocksNeeded * 5;
        return r;
    }

    Map<String, Long> decodeFILES(byte[] buf) throws IOException{
        Map<String, Long> m = new HashMap<String, Long>();

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

    public static void listFilesInFolder(File folder,String path) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesInFolder(fileEntry, path + fileEntry.getName() + "||");
            } else {
                System.out.println(path + fileEntry.getName());
            }
        }
    }

    public static void getFilesInFolder(Map<String, Long> m, File folder, String path) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(m, fileEntry,(path+fileEntry.getName()+"||"));
            } else {
                m.put(path + fileEntry.getName(), fileEntry.length());
            }
        }
    }

    public static void listFiles(File folder) {
        for (File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
        }
    }

    public static Map<String, Long> partSynchronized(Map<String, Long> mine, Map<String, Long> other){
        Map<String, Long> res = new HashMap<String, Long>();
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

    public static boolean isInSubfolder(String filename){
        boolean res = false;
        char[] v = filename.toCharArray();

        for(int i = 0; i < filename.length() - 1; i++){
            if(v[i] == '|' && v[i + 1] == '|'){
                res = true;
                break;
            }
        }

        return res;
    }


    public Pacote analisePacket(byte[] array) throws IOException {

        Pacote pacote = new Pacote(3); //mudar obviamente
        switch (array[0]) {
            case 1:
                RRQFolder p = new RRQFolder(array);
                //pacote = new RRQFolder(array);
                //sendFILES((RRQFolder) p);
                break;
            case 2:
                pacote = new RRQFile(array);
                //Cria aqui a thread sender para enviar a data e receber acks?
                //List<DATA> data = sendWRQ((RRQFile) pacote);
                //sendDATA(data);
                break;
            case 3: //WRQFile
                pacote = new WRQFile(array);
                WRQFile tmp = (WRQFile) pacote;
                int nBlocos = tmp.getNBlocos();
                sendPacket(new ACK(nBlocos));
                //Criar aqui a thread reciever so a receber data e enviar acks?
                break;
            case 4:
                pacote = new DATA(array);
                //Isto iria passar a ser trabalho da thread reciever
                break;
            case 5:
                pacote = new ACK(array);
                //Isto iria passar a ser trabalho da thread reciever
                break;
            case 6:
                pacote = new FILES(array);
                sendPacket(new ACK(1));
                break;
            case 7:
                pacote = new FIN(array);
                break;
            default:
                return null;
        }
        return pacote;
    }

    // está a fazer para vários, mudar para um só
    public String sendRRQFile(Pacote pacote, File myFolder, File otherFolder) throws IOException{
        String fileName = "";
        Map<String, Long> mine = new HashMap<String, Long>();
        getFilesInFolder(mine, myFolder, "");
        Map<String, Long> other = decodeFILES(pacote.getContent());
        Map<String, Long> missing = partSynchronized(mine, other);

        for(Map.Entry<String, Long> entry: missing.entrySet()){
            fileName += myFolder.getAbsolutePath() + "/" + entry.getKey();
            System.out.println(entry.getKey() + " | " + entry.getValue());
            RRQFile rrq = new RRQFile(otherFolder.getAbsolutePath() + "/" + entry.getKey());
            sendPacket(rrq);
        }

        return fileName;
    }

    public void getFilesHandler(Pacote pacote, File myFolder, File otherFolder) throws IOException, InterruptedException {
        Map<String, Long> mine = new HashMap<String, Long>();
        getFilesInFolder(mine, myFolder, "");
        Map<String, Long> other = decodeFILES(pacote.getContent());
        Map<String, Long> missing = partSynchronized(mine, other);
        Thread[] missingFiles = new Thread[missing.size()];
        int i=0;
        for(Map.Entry<String, Long> entry: missing.entrySet()){                 //verificar erros
            missingFiles[i] = new Thread(new DataReciever(InetAddress.getByName("localhost"),
                    1025,"C:\\Users\\Acer\\Desktop\\tp2.rar",13389959));
            missingFiles[i].start();

            RRQFile rrq = new RRQFile(otherFolder.getAbsolutePath() + "/" + entry.getKey());
            sendPacket(rrq);
            getFileHandler(myFolder.getAbsolutePath() + "/" + entry.getKey(), entry.getValue());
            i++;
        }

        for(int j=0;j<i;j++){                 //verificar erros
            missingFiles[i].join();
        }
    }

    void getFileHandler(String newFileName, Long filesize) throws FileNotFoundException, IOException{
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        System.out.println(newFileName);
        this.socket.receive(packet);
        Pacote pacote = analisePacket(buf);
        WRQFile tmp = (WRQFile) pacote;
        writeFile(newFileName, tmp.getNBlocos(), filesize);
    }

    public void writeToFile(File file, byte[] buf) throws FileNotFoundException, IOException{
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public void writeFile(String fileName, int nrblocks, Long filesize)throws FileNotFoundException, IOException{
        File f = new File(fileName);
        List<byte[]> list = new ArrayList<byte[]>();

        for(int i = 0; i < nrblocks; i++){
            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            //guardar numa lista para os ACKS
            this.socket.receive(packet);
            byte[] tmp;
            if(i == nrblocks - 1){
                int EOF = findEOF(buf);
                tmp = new byte[EOF - 5];
                System.arraycopy(buf, 5, tmp, 0, EOF - 5);
            }
            else{
                tmp = new byte[datablock];
                System.arraycopy(buf, 5, tmp, 0, datablock);
            }
            list.add(tmp);
        }

        byte[] fileContent = buildFileContent(list, filesize);
        writeToFile(f, fileContent);
    }

    public byte[] buildFileContent(List<byte[]> l, Long filesize){
        byte[] fileContent = new byte[Math.toIntExact(filesize)];

        for(int i = 0; i < l.size(); i++){
            System.arraycopy(l.get(i), 0, fileContent, i * datablock, l.get(i).length);
        }

        return fileContent;
    }

    public int findEOF(byte[] buf){
        int i;
        for(i = 5; i < buf.length && buf[i] != 0; i++);
        return i;
    }

    public void close() {
        socket.close();
    }

    public void run() {
        File folder1 = new File("C:\\Users\\Acer\\Desktop\\teste1");
        File folder2 = new File("C:\\Users\\Acer\\Desktop\\teste2");

        Map<String, Long> m = new HashMap<String, Long>();
        Map<String, Long> m2 = new HashMap<String, Long>();


        getFilesInFolder(m, folder1, "");
        getFilesInFolder(m2, folder2, "");

        RRQFolder rrq = new RRQFolder(folder1.getAbsolutePath());

        try{
            sendPacket(rrq);

            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.socket.receive(packet);

            Pacote pacote = analisePacket(buf);
            getFilesHandler(pacote, folder2, folder1);

            sendPacket(new FIN());
        }
        catch(IOException | InterruptedException ioe){
            ioe.printStackTrace();
        }
    }
}