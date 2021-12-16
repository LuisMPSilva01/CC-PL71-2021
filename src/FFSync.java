import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;


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

        //Start of verificação de password
        /*
        Scanner sc= new Scanner(System.in);
        String password;
        do {
            System.out.println("Ensira a password: ");
            password = sc.next();                     //Somos rewarded por perguntar ao peer qual é password?
        } while (password.equals("arroz\n"));
        //end of verificação de password
    */
        Date start = new Date(); //Hora de começo
        /*
        if(args.length!=2){
            System.out.println("Formato errado, tente : FFSync pasta1 10.1.1.1");
            return;
        }
        System.out.println("Net is: "+netIsAvailable()); //Adicionar returns caso falso
        System.out.println("Ficheiro existe: "+Files.exists(Path.of(args[0]))); //Adicionar returns caso falso
        */

        int defaultPort=8888;
        int SO = 0; //SO==0 LINUX || ELSE WINDOWS
        long startSize = Files.walk(Paths.get(args[0])) //Get folder starting size
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        try {
            if(args.length==2) { //Cenario normal
                DatagramSocket socket = new DatagramSocket(defaultPort);
                Thread servidor = new Thread(new EchoServer(socket,new File(args[0])));
                servidor.start();

                Thread cliente = new Thread(new EchoClient(defaultPort, InetAddress.getByName(args[1]),new File(args[0])));
                cliente.start();

                servidor.join();
                cliente.join();
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

                servidor1.join();
                cliente1.join();
                servidor2.join();
                cliente2.join();
            }
        } catch (SocketException | InterruptedException e) {
            System.out.println("Porta em uso, tente novamente mais tarde");
        }

        ////Start of Logs
        Date finish = new Date();
        long timeTaken = finish.getTime()-start.getTime();
        long endSize = Files.walk(Paths.get(args[0])) //Get folder ending size
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
        long dataTransferida = endSize-startSize;
        DateFormat DFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault()); //Date format

        FileOutputStream fos = new FileOutputStream("logs", true);
        fos.write(("INFO: FFSync "+ args[0] +" " + Arrays.toString(Arrays.copyOfRange(args, 1, args.length)) + "\n" +
                "Date: "+ DFormat.format(start) +"\n"+
                "Time taken: " + (timeTaken) + " miliseconds\n" +
                "StartSize: " + (startSize) + "\n" +
                "EndSize: " + (endSize) + "\n" +
                "Data transferida: " + dataTransferida + " bytes\n" +
                "Bitrate: " + ((float)dataTransferida/timeTaken*1000) + " bytes/segundo\n" + //Como conseguir o débito real?
                "-----------------------------------------------------\n").getBytes());
        fos.close();
        ////End of Logs
    }
}
