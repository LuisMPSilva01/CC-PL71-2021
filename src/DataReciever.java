import packets.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


class DataReciever implements Runnable {
    private DatagramSocket socket;
    private int port;
    private InetAddress address;
    private String fileName;
    private String newFileName;
    private long fileSize;
    private final int datablock = 1191;
    private final int timeOut = 10000;

    public DataReciever(InetAddress address,int serverPort,String fileName,String newFileName,long fileSize) throws SocketException{
        this.address=address;
        this.port=serverPort;
        this.socket = new DatagramSocket();
        //this.socket.setSoTimeout(timeOut);
        this.fileName=fileName;
        this.newFileName = newFileName;
        this.fileSize=fileSize;
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
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
            socket.receive(packet);
            System.out.println("address: " + address + " | port: " + port);
            for(int j = 0; j < 10; j++)
                System.out.println("[" + j + "]: " + buf[j]);
            if(buf[0] == (byte) 3){
                this.address = packet.getAddress();
                this.port = packet.getPort();
                sendPacket(new ACK(1));
                return new WRQFile(buf);
            }
            else {
                System.out.println("é null?");
                return null;
            }
        }
        catch (SocketTimeoutException e) {
            return null;
        }
    }

    public byte[] buildFileContent(List<byte[]> l, Long filesize){
        System.out.println("filesize: " + filesize);
        byte[] fileContent = new byte[Math.toIntExact(filesize)];

        for(int i = 0; i < l.size(); i++){
            System.out.println("DATA(" + i + " ): " + l.get(i).length);
            System.arraycopy(l.get(i), 0, fileContent, i * datablock, l.get(i).length);
        }

        return fileContent;
    }


    public void writeToFile(File file, byte[] buf) throws FileNotFoundException, IOException{
        System.out.println("newfile: " + file.getAbsolutePath());
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public void writeFile(File f, int nrblocks, Long filesize)throws FileNotFoundException, IOException{
        List<byte[]> list = new ArrayList<>();

        System.out.println("Nr blocos: " + nrblocks);
        for(int i = 0; i < nrblocks; i++){
            byte[] buf = new byte[1200];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            System.out.println(i + ": antes");
            this.socket.receive(packet);
            System.out.println("depois");
            this.port=packet.getPort();
            this.address=packet.getAddress();

            sendPacket(new ACK(i));
            byte[] blockSize = new byte[4];
            System.arraycopy(buf, 5, blockSize, 0, 4);
            int bSize = ByteBuffer.wrap(blockSize).getInt();
            byte[] tmp = new byte[bSize];
            System.arraycopy(buf, 9, tmp, 0, bSize);
            list.add(tmp);
        }
        System.out.println("depois de ti");

        byte[] fileContent = buildFileContent(list, filesize);
        writeToFile(f, fileContent);
    }


    @Override
    public void run(){
        try {
            int nrBlocks = sendRRQ();
            File f = new File(newFileName);
            writeFile(f, nrBlocks, fileSize);
            this.socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
