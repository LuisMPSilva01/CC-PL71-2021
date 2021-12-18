import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPserverTEST {
    public static void main(String[] args) throws IOException {
        System.out.println("SERVER STARTED");

        int SO =1;
        File folder;
        if (SO==0){
            folder = new File("/home/ray/Downloads/teste3");
        }else {
            folder = new File("C:\\Users\\Acer\\Desktop\\teste2");
        }
        DatagramSocket socket = new DatagramSocket(8888);
        FFSync.verificaPassword(socket, InetAddress.getByName("localhost"),8889);
        EchoServer server = new EchoServer(socket,folder);
        server.start();
    }
}
