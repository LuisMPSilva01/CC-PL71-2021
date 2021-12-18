import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClientTest {
    public static void main(String[] args) throws IOException {
        System.out.println("Client Started");
        int SO=1;
        File folder;
        if (SO==0){
            folder = new File("/home/ray/Downloads/teste2");
        }else {
            folder = new File("C:\\Users\\Acer\\Desktop\\teste1");
        }
        DatagramSocket socket = new DatagramSocket(8889);
        FFSync.verificaPassword(socket, InetAddress.getByName("localhost"),8888);
        EchoClient client = new EchoClient(8888, InetAddress.getByName("localhost"),folder);
        client.start();
    }
}
