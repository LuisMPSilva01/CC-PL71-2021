import java.io.*;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class cliente {

    public static void main(String[] args) throws IOException {
        if(args.length!=1){
            System.out.println("Format: FCliente ficheiro");
            return;
        }

        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh = null;

        try {
            try {
                fh = new FileHandler("C:\\Users\\Acer\\Desktop\\logs.txt");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            assert fh != null;
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            logger.info("FCliente " + args[0]);

        } catch(SecurityException e) {
            e.printStackTrace();
        }
        String host = "127.0.0.1";

        Socket socket = new Socket(host, 1025);


        File file = new File(args[0]);
        byte[] bytes = new byte[16 * 1024];
        InputStream in = new FileInputStream(file);
        OutputStream out = socket.getOutputStream();

        int count;
        while ((count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
        }

        out.close();
        in.close();
        socket.close();
    }
}