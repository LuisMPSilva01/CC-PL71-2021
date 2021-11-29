import java.io.IOException;
import java.net.*;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;

    public EchoClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("192.168.56.1");
    }

    public void sendEcho(String msg) throws IOException {
        byte[] buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 8888);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        return;
    }

    public void close() {
        socket.close();
    }
}