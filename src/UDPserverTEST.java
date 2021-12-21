import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;

public class UDPserverTEST {
    public static void main(String[] args) throws IOException {
        System.out.println("SERVER STARTED");

        int SO =0;
        File folder;
        if (SO==0){
            folder = new File("/home/ray/Downloads/teste3");
        }else {
            folder = new File("C:\\Users\\Acer\\Desktop\\teste2");
        }
        LogsMaker logs = new LogsMaker("C:\\Users\\Acer\\Desktop\\teste2","myself");
        DatagramSocket socket = new DatagramSocket(8888);
        Server server = new Server(socket,folder,logs,false);
        server.start();
    }
}
