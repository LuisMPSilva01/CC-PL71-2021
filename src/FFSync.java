import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;


public class FFSync {
    private static boolean netIsAvailable() {
        try {
            URL url = new URL("https://www.google.com");
            URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        /*
        if(args.length!=2){
            System.out.println("Formato errado, tente : FFSync pasta1 10.1.1.1");
            return;
        }
        System.out.println("Net is: "+netIsAvailable()); //Adicionar returns caso falso
        System.out.println("Ficheiro existe: "+Files.exists(Path.of(args[0]))); //Adicionar returns caso falso
        */

        FileOutputStream fos = new FileOutputStream("logs", true);  ////Logs
        fos.write(("INFO: FFSync "+ args[0] +" " + Arrays.toString(Arrays.copyOfRange(args, 1, args.length)) + "\n" +
                   "Date: "+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +"\n"+
                   "-----------------------------------------------------\n").getBytes());
        fos.close(); ////End of Logs

        int defaultPort=8888;
        int SO = 1; //SO==0 LINUX || ELSE WINDOWS
        File folder;

        try {
            if(args.length==2) {
                DatagramSocket socket = new DatagramSocket(defaultPort);
                Thread servidor = new Thread(new EchoServer(socket,new File(args[0])));
                servidor.start();

                Thread cliente = new Thread(new EchoClient(defaultPort, InetAddress.getByName(args[1]),new File(args[0])));
                cliente.start();
            } else { //Cenario de teste
                File folder1,folder2;
                if (SO==0){
                    folder1 = new File("/home/ray/Downloads/teste3");
                    folder2 = new File("/home/ray/Downloads/teste2");
                }else {
                    folder1 = new File("C:\\Users\\Acer\\Desktop\\teste1");
                    folder2 = new File("C:\\Users\\Acer\\Desktop\\teste2");
                }

                DatagramSocket socket1 = new DatagramSocket(defaultPort);
                Thread servidor1 = new Thread(new EchoServer(socket1,folder1));
                servidor1.start();

                Thread cliente1 = new Thread(new EchoClient(8889, InetAddress.getByName("localhost"),folder1)); //change
                cliente1.start();

                ////////////////////////////////////
                DatagramSocket socket2 = new DatagramSocket(8889);
                Thread servidor2 = new Thread(new EchoServer(socket2,folder2));
                servidor2.start();

                Thread cliente2 = new Thread(new EchoClient(defaultPort, InetAddress.getByName("localhost"),folder2)); //change
                cliente2.start();
            }
        } catch (SocketException e) {
            System.out.println("Porta em uso, tente novamente mais tarde");
        }
    }
}
