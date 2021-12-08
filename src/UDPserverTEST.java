import java.io.IOException;

public class UDPserverTEST {
    public static void main(String[] args) throws IOException {
        System.out.println("SERVER STARTED");
        EchoServer server = new EchoServer(8888);
        server.start();
    }
}
