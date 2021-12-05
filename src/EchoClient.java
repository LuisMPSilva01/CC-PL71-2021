import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import packets.*;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;
    private final int datablock = 1195;


    public EchoClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void sendEcho(String msg) throws IOException {
        byte[] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8888);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        return;
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8888);
        socket.send(packet);
        return;
    }

    public int blocksNeeded(Long l){
        return (int) (Math.floorDiv(l, datablock) + 1);
    }

    public void sendFile(File f) throws IOException{
        byte[] fileContent = Files.readAllBytes(f.toPath());
        int nrblocks = blocksNeeded(f.length());

        for(int i = 0; i < nrblocks; i++){
            byte[] blockContent;
            if(i == nrblocks - 1){
                blockContent = new byte[fileContent.length - (i * datablock)];
            }
            else{
                blockContent = new byte[datablock];
            }
            System.arraycopy(fileContent, i * datablock, blockContent, 0, blockContent.length);
            DATA d = new DATA(i + 1, blockContent);
            sendPacket(d);
        }
    }

    public void close() {
        socket.close();
    }
}