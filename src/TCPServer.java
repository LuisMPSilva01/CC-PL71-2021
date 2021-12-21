import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPServer implements Runnable{
    public TCPServer(){}

    private void sendAnswer(Socket client) throws IOException{
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
        clientOutput.write(("ContentType: text/html\r\n").getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("logs"), StandardCharsets.UTF_8));

        String line;
        while ((line = br.readLine()) != null) {
            clientOutput.write(line.getBytes());
            clientOutput.write("\r\n\r\n".getBytes());
        }

        clientOutput.flush();
        br.close();
        clientOutput.close();
        client.close();
    }

    public void run(){
        ServerSocket server;
        try{
            server = new ServerSocket(80);

            while(true){
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String s = in.readLine();
                if(s.equals("close"))
                    break;
                sendAnswer(client);
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
