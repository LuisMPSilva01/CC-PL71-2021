import java.io.File;
import java.io.FileOutputStream;
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
        Logs logs = new Logs("C:\\Users\\Acer\\Desktop\\teste2","myself");
        DatagramSocket socket = new DatagramSocket(8888);
        EchoServer server = new EchoServer(socket,folder,logs,false);
        server.start();
    }
}
