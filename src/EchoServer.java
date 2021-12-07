import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;
import javax.print.attribute.HashAttributeSet;
import javax.swing.plaf.FileChooserUI;

import packets.*;

public class EchoServer extends Thread {
    private File folder;
    private DatagramSocket socket;
    private InetAddress address;
    private boolean running;
    private final int datablock = 1195;
    private int defaultPort = 8888;


    public EchoServer() throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(defaultPort);
        this.address = InetAddress.getByName("localhost");
    }

    public void sendPacket(Pacote p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    void printMsg(byte[] buf){
        for(byte b: buf){
            System.out.printf("%x", b);
        }
        System.out.println();
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

    public void writeToFile(File file, byte[] buf) throws FileNotFoundException, IOException{
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public void writeFile(File f, int nrblocks, Long filesize)throws FileNotFoundException, IOException{
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

    public static void getFilesInFolder(Map<String, Long> m, File folder, String path) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(m, fileEntry,(path+fileEntry.getName()+"||"));
            } else {
                m.put(path + fileEntry.getName(), fileEntry.length());
            }
        }
    }
    public int blocksNeededWRQ(RRQFile pacote){
        File f = new File(pacote.getFileName());
        return (int) (Math.floorDiv(f.length(), datablock) + 1);
    }
    public void sendFILES(RRQFolder pacote, InetAddress address, int port) throws IOException {
        HashMap<String, Long> map = new HashMap<String, Long>();
        byte[] fArray = new byte[(pacote.getContent().length - 1)];
        File folder = new File(pacote.getFolderName());
        getFilesInFolder(map, folder, "");

        FILES files = new FILES(map);
        sendPacket(files, address, port);
    }


    public Pacote analisePacket(byte[] array, InetAddress address, int port) throws IOException {
        Pacote pacote = new Pacote(1200);
        switch (array[0]) {
            case 1:
                pacote = new RRQFolder(array);
                sendFILES((RRQFolder) pacote, address, port);
                break;
            case 2:
                pacote=new RRQFile(array);
                RRQFile tmp = (RRQFile) pacote;
                System.out.println("identify: " + tmp.getFileName());
                Thread ds = new Thread(new DataSender((RRQFile) pacote,address,port));
                ds.start();
                break;
            case 3: //WRQFile
            case 4: //DATA
            case 5:
                pacote = new ACK(array);
                //System.out.println("ACK");
                break;
            case 6:
                pacote = new FILES(array);
                Map<String, Long> m = decodeFILES(pacote.getContent());
                break;
            case 7:
                pacote = new FIN(array);
                break;
            default:
                return null;
        }
        return pacote;
    }

    public void run() {
        running = true;
        int npacotes=0;
        while (running) {
            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                this.socket.receive(packet);
                Pacote pacote = analisePacket(buf, packet.getAddress(), packet.getPort());
                if (pacote.isFIN()){
                    //running=false; //??
                }
            }
            catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }
}