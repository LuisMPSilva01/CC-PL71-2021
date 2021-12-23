import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class FFSync {

    public static boolean isReachable(String endereco){ //Verifica se um endereço é valido e é reachable
        try {
            InetAddress inet=InetAddress.getByName(endereco);
            if(!inet.isReachable(500)){
                System.out.println("O endereço é unreachable");
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Endereco nao é valido");
            return false;
        }
        return true;
    }
    public static boolean passwordIsValida(String password){ //Verifica se a password enserida cumpre os requisitos
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

    public static byte[] retiraZeros(byte[] bytes){ //Retira os bytes=0 do array
        int size = 0;
        for (byte c:bytes){
            if (c== (byte) 0) break;
            else size++;
        }
        byte[] newArray = new byte[size];
        System.arraycopy(bytes, 0, newArray, 0, size);
        return newArray;
    }

    public static void verificaPassword(DatagramSocket socket, InetAddress address, int port) throws IOException {//Troca mensagens com o peer até confirmarem a password
        Scanner sc= new Scanner(System.in);
        socket.setSoTimeout(20000);
        while (true){
            try {
                System.out.println("Insira a password (tamanho maximo 50, não digite números): ");
                String password = sc.next();
                if (passwordIsValida(password)) {
                    byte[] bytesEnvio = password.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket enviado = new DatagramPacket(bytesEnvio, bytesEnvio.length, address, port);
                    socket.send(enviado); //Envia password

                    byte[] bytesRecebidos = new byte[50];
                    DatagramPacket recebido = new DatagramPacket(bytesRecebidos, bytesRecebidos.length, address, port);
                    socket.receive(recebido); //Recebe password do parçeiro
                    if (password.equals(new String(retiraZeros(bytesRecebidos)))) { //Confirma se as passwords são iguais
                        socket.send(enviado); //Enviar extras para confirmar que o parceiro recebe
                        socket.send(enviado); //Enviar extras para confirmar que o parceiro recebe
                        socket.send(enviado); //Enviar extras para confirmar que o parceiro recebe
                        return;
                    } else {
                        System.out.println("Password errada, tente outra vez");
                    }
                } else {
                    System.out.println("Formato errado, tente outra vez");
                }
            } catch (SocketTimeoutException ste){
                System.out.println("Não recebi nenhuma password, tente outra vez");
            }
        }
    }

    public static boolean showPacketLogs(){ //Opção para criar logs sobre todos os pacotes enviados (cria Cliente_packets e Servidor_packets)
        Scanner sc = new Scanner(System.in);

        System.out.println("Deseja observar os logs acerca dos pacotes enviados? (y/n)");
        while (true) {
            String resposta = sc.nextLine().trim().toLowerCase();
            if (resposta.equals("y")) {
                return true;
            } else if (resposta.equals("n")) {
                return false;
            } else {
                System.out.println("Tente outra vez. Responda com y/n");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("Formato errado, tente : FFSync pasta1 10.1.1.1");
            return;
        }
        if(!Files.exists(Path.of(args[0]))) {
            System.out.println("Pasta não existe");
            return;
        }
        if(!isReachable(args[1])) {
            return;
        }

        int defaultPort=80;
        boolean showPL = showPacketLogs();
        try {
            LogsMaker logs = new LogsMaker(args[0],args[1]); //Inicializa Logs
            DatagramSocket socket = new DatagramSocket(defaultPort);//Inicializa socket do servidor

            verificaPassword(socket,InetAddress.getByName(args[1]),defaultPort); //Verifica a password

            Thread servidor = new Thread(new Server(socket,new File(args[0]),logs,showPL)); //Inicializa servidor udp
            servidor.start();

            Thread servidorTCP = new Thread(new TCPServer()); //Inicializa servidor tcp
            servidorTCP.start();

            Thread cliente = new Thread(new Client(defaultPort, InetAddress.getByName(args[1]),new File(args[0]),logs,showPL)); //Inicializa cliente udp
            cliente.start();

            servidor.join();
            cliente.join();
            Thread clienteTCP = new Thread(new TCPClient(InetAddress.getByName(args[1]))); //Inicializa cliente tcp
            clienteTCP.start();
            servidorTCP.join();
            clienteTCP.join();
            logs.finish();
            System.out.println("FFSync terminado");
        }
        catch (SocketException | InterruptedException e) {
            System.out.println("Porta em uso, tente novamente mais tarde");
        }
    }
}
