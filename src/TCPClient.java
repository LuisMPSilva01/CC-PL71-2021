import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient implements Runnable{
    private InetAddress address;

    public TCPClient(InetAddress address){
        this.address = address;
    }

    public void run() {
        Socket client;
        try {
            client = new Socket(this.address, 80);
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println("close");
            out.flush();
            client.shutdownInput();
            client.shutdownOutput();
            client.close();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
