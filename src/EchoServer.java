import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class EchoServer extends Thread {
    private DatagramSocket socket;
    private boolean running;

    public EchoServer() throws SocketException {
        socket = new DatagramSocket(8888);
    }

    public void run() {
        running = true;

        while (running) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String received = new String(packet.getData(), 0, packet.getLength());

            if (received.contentEquals("end")) {
                running = false;
                continue;
            }
            else {
                System.out.println("Mensagem recebida: "+received);
            }
        }
        socket.close();
    }
}