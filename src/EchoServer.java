import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor.STRING;
import javax.print.attribute.HashAttributeSet;

import packets.Pacote;

public class EchoServer extends Thread {
    private DatagramSocket socket;
    private boolean running;

    public EchoServer() throws SocketException {
        this.socket = new DatagramSocket(8888);
    }

    void printMsg(byte[] buf){
        for(byte b: buf){
            System.out.printf("%x", b);
        }
    }

    Map<String, Long> decodeFILES(byte[] buf) throws IOException{
        Map<String, Long> m = new HashMap<String, Long>();
        
        for(int i = 1; i < buf.length && buf[i] != -1; ){
            byte[] s_fn = new byte[4];
            System.arraycopy(buf, i, s_fn, 0, 4);
            int size_filename = ByteBuffer.wrap(s_fn).getInt();
            i += 4;

            String filename = new String(buf, i, size_filename, StandardCharsets.UTF_8);
            i += size_filename;

            byte[] fs = new byte[8];
            System.arraycopy(buf, i, fs, 0, 8);
            Long filesize = ByteBuffer.wrap(fs).getLong();
            i += 8;

            m.put(filename, filesize);
        }

        return m;
    }

    public void writeToFile(File file, byte[] buf) throws FileNotFoundException, IOException{
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public int findEOF(byte[] buf){
        int i;
        for(i = 5; buf[i] != 0 && i < buf.length; i++);
        return i;
    }

    public void run() {
        running = true;

        while (running) {
            byte[] buf = new byte[1200]; // mudar isto
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                this.socket.receive(packet);

                File test = new File("/home/ray/Downloads/testes/teste2/server.c");
                int EOF = findEOF(buf);
                byte[] tmp = new byte[EOF - 5];
                System.arraycopy(buf, 5, tmp, 0, EOF - 5);

                writeToFile(test, tmp);
                /*
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.contentEquals("end")) {
                    running = false;
                    continue;
                }
                */
                //Map<String, Long> m = decodeFILES(buf);
                //for(Map.Entry<String, Long> entry: m.entrySet())
                //    System.out.println(entry.getKey() + " | " + entry.getValue());

            }
            catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        socket.close();
    }
}