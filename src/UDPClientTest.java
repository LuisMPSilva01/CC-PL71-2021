import java.io.IOException;

public class UDPClientTest {
    public static void UDPClientTest() throws IOException {
        EchoClient client = new EchoClient();
        client.sendEcho("server is working");

        client.sendEcho("end");
        client.close();
    }
    public static void main(String[] args) throws IOException {
        System.out.println("Client Started");
        UDPClientTest();
    }
}
