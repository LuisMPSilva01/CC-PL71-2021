import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClientTest {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        System.out.println("Client Started");
        EchoClient client = new EchoClient(8888);
        client.start();
    }
}
