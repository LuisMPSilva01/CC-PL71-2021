import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
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

    public static byte[] retiraZeros(byte[] bytes){
        int size = 0;
        for (byte c:bytes){
            if (c== (byte) 0) break;
            else size++;
        }
        byte[] newArray = new byte[size];
        System.arraycopy(bytes, 0, newArray, 0, size);
        return newArray;
    }

    public static void verificaPassword(DatagramSocket socket, InetAddress address, int port) throws IOException {
        Scanner sc= new Scanner(System.in);

        while (true){
            System.out.println("Ensira a password (tamanho maximo 50, não digite números): ");
            String password = sc.next();
            if(passwordIsValida(password)){
                byte[] bytesEnvio=password.getBytes(StandardCharsets.UTF_8);
                DatagramPacket enviado = new DatagramPacket(bytesEnvio,bytesEnvio.length,address,port);
                socket.send(enviado);

                byte[] bytesRecebidos = new byte[50];
                DatagramPacket recebido = new DatagramPacket(bytesRecebidos,bytesRecebidos.length,address,port);
                socket.receive(recebido);
                String teste = new String(retiraZeros(bytesRecebidos));
                System.out.println("Teste:" + teste + " | Password:" +password);
                if(password.equals(new String(retiraZeros(bytesRecebidos)))){
                    socket.send(enviado); //Enviar extras para confirmar que o parceiro recebe
                    socket.send(enviado); //Enviar extras para confirmar que o parceiro recebe
                    socket.send(enviado); //Enviar extras para confirmar que o parceiro recebe
                    return;
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

        //if(!isReachable(args)) return;
        /*
        if(args.length!=2){
            System.out.println("Formato errado, tente : FFSync pasta1 10.1.1.1");
            return;
        }
        System.out.println("Net is: "+netIsAvailable()); //Adicionar returns caso falso
        System.out.println("Ficheiro existe: "+Files.exists(Path.of(args[0]))); //Adicionar returns caso falso
        */

        int defaultPort=8888;
        int SO = 1; //SO==0 LINUX || ELSE WINDOWS
        long startSize = Files.walk(Paths.get(args[0])) //Get folder starting size
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        try {
            Date start;
            if(args.length==2) { //Cenario normal
                DatagramSocket socket = new DatagramSocket(defaultPort);
                verificaPassword(socket,InetAddress.getByName(args[1]),defaultPort);
                start = new Date(); //Hora de começo
                Thread servidor = new Thread(new EchoServer(socket,new File(args[0])));
                servidor.start();

                Thread cliente = new Thread(new EchoClient(defaultPort, InetAddress.getByName(args[1]),new File(args[0])));
                cliente.start();

                servidor.join();
                cliente.join();
            } else { //Cenario de teste
                start = new Date(); //Hora de começo
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
