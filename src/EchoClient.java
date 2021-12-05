import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import packets.Pacote;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;

    public EchoClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8888);
        socket.send(packet);
        //packet = new DatagramPacket(buf, buf.length);
        return;
    }

    public void close() {
        socket.close();
    }
}