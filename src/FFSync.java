import packets.Pacote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;


public class FFSync {

    public static boolean isReachable(String[] args) throws IOException {
        for (int i=1;i<args.length;i++){ //Verificação de conexão
            InetAddress inet=InetAddress.getByName(args[i]);
            if(!inet.isReachable(500)){
                System.out.println("The following ip address is unreachable:" + args[i]);
                return false;
            }
        }
        return true;
    }
    public static boolean passwordIsValida(String password){
        boolean valid = true;
        char[] array= password.toCharArray();
        if(array.length>= 50) return false;

        for (char character: password.toCharArray()) {
            valid = !Character.isDigit(character);
            if (!valid) {
                break;
            }
        }
        return valid;
    }
    public static boolean verificaPassword(DatagramSocket socket,InetAddress address,int port) throws IOException {
        Scanner sc= new Scanner(System.in);

        byte[] bytes = new byte[50];
        DatagramPacket pacote = new DatagramPacket(bytes,50,address,port);

        while (true){
            System.out.println("Ensira a password (tamanho maximo 50, não digite números): ");
            String password = sc.next();
            if(passwordIsValida(password)){
                bytes=password.getBytes(StandardCharsets.UTF_8);
                socket.send(pacote);
                socket.receive(pacote);

                String recebido = new String(bytes);
                if(password.equals(recebido)){
                    socket.send(pacote); //Enviar extras para confirmar que o parceiro recebe
                    socket.send(pacote); //Enviar extras para confirmar que o parceiro recebe
                    socket.send(pacote); //Enviar extras para confirmar que o parceiro recebe
                    return true;
                }
                else {
                    System.out.println("Password errada, tente outra vez");
                }
            } else{
                System.out.println("Formato errado, tente outra vez");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if(!isReachable(args)) return;

        //Start of verificação de password

        //end of verificação de password
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
            Date start = new Date(); //Hora de começo
            if(args.length==2) { //Cenario normal
                DatagramSocket socket = new DatagramSocket(defaultPort);
                verificaPassword(socket,InetAddress.getByName(args[1]),defaultPort);
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
        } catch (SocketException | InterruptedException e) {
        System.out.println("Porta em uso, tente novamente mais tarde");
        }
    }
}
