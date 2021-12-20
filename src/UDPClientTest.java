import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class UDPClientTest {
    public static void main(String[] args) throws IOException {
        System.out.println("Client Started");
        int SO=0;
        File folder;
        if (SO==1){
            folder = new File("/home/ray/Downloads/teste2");
        }else {
            folder = new File("C:\\Users\\Acer\\Desktop\\teste1");
        }
        LogsMaker logs = new LogsMaker("C:\\Users\\Acer\\Desktop\\teste1","myself");
        Client client = new Client(8888, InetAddress.getByName("localhost"),folder,logs,false);
        client.start();
    }
}
