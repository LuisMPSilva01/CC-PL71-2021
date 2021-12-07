import packets.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


class DataReciever implements Runnable {
    private DatagramSocket socket;
    private int port;
    private InetAddress address;
    private String fileName;
    private long fileSize;
    private final int datablock = 1195;
    private final int timeOut = 10000;

    public DataReciever(InetAddress address,int serverPort,String fileName,long fileSize) throws SocketException, UnknownHostException {
        this.address=address;
        this.port=serverPort;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(timeOut);
        this.fileName=fileName;
        this.fileSize=fileSize;
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        return;
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
            socket.receive(packet);
            if(buf[0]==3){
                return new WRQFile(buf);
            }
            else return null;
        }
        catch (SocketTimeoutException e) {
            return null;
        }
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
    public void writeToFile(File file, byte[] buf) throws FileNotFoundException, IOException{
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public void writeFile(File f, int nrblocks, Long filesize)throws FileNotFoundException, IOException{
        List<byte[]> list = new ArrayList<>();

        for(int i = 0; i < nrblocks; i++){
            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            this.socket.receive(packet);
            this.port=packet.getPort();
            this.address=packet.getAddress();
            sendPacket(new ACK(i));
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

    @Override
    public void run(){
        try {
            int nrBlocks = sendRRQ();
            File f = new File(fileName);
            writeFile(f,nrBlocks,fileSize);
            this.socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
