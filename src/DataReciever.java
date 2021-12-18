import packets.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


class DataReciever implements Runnable {
    private final DatagramSocket socket;
    private int port;
    private InetAddress address;
    private final String fileName;
    private final String newFileName;
    private final long fileSize;
    private int nBloco;

    public DataReciever(InetAddress address,int serverPort,String fileName,String newFileName,long fileSize) throws SocketException{
        this.address=address;
        this.port=serverPort;
        this.socket = new DatagramSocket();
        int timeOut = 100;
        this.socket.setSoTimeout(timeOut);
        this.fileName=fileName;
        this.newFileName = newFileName;
        this.fileSize=fileSize;
        this.nBloco=-1;
    }

    public void sendPacket(UDP_Packet p) throws IOException {
        byte[] buf = p.getContent();
        System.out.println("Length:"+ buf.length);
        DatagramPacket packet = new DatagramPacket(buf,buf.length, address, port);
        socket.send(packet);
    }

    public int sendRRQ() throws IOException {
        WRQFile pacote;
        do {
            sendPacket(new RRQFile(fileName));
        } while ((pacote=getWRQ())==null);
        return pacote.getNBlocos();
    }

    public WRQFile getWRQ() throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            socket.receive(packet);
            WRQFile pacote = new WRQFile(buf);
            if(pacote.isOK()){
                this.address = packet.getAddress();
                this.port = packet.getPort();
                return pacote;
            }
            else return null;
        }
        catch (SocketTimeoutException e) {
            return null;
        }
    }

    public byte[] buildFileContent(List<byte[]> l, Long filesize){
        byte[] fileContent = new byte[Math.toIntExact(filesize)];

        for(int i = 0; i < l.size(); i++){
            int datablock = 1187;
            System.arraycopy(l.get(i), 0, fileContent, i * datablock, l.get(i).length);
        }

        return fileContent;
    }


    public void writeToFile(File file, byte[] buf) throws IOException{
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public void writeFile(File f, int nrblocks, Long filesize) throws IOException{
        List<byte[]> list = new ArrayList<>();

        boolean sendACK = true;
        while (true) {
            if(sendACK){  //Caso na iteração anterior tenha recebido o pacote errado, não vai enviar ack
                sendPacket(new ACK(nBloco)); //Enviar o ack para desbloquear o DataSender
            } else sendACK=true;
            nBloco++;
            if((nrblocks)==nBloco) break; //Caso seja o ultimo pacote sai do ciclo


            byte[] buf = new byte[1200]; //Receber pacote
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try { //Caso de sucesso
                this.socket.receive(packet); //Receber pacote
                DATA pacote = new DATA(buf);
                if (pacote.isOK()) { //Verifica se é um pacote de DATA intacto
                    if (pacote.getNBloco() == nBloco) { //Verifica se é o bloco desejado
                        list.add(pacote.getConteudo()); //Guardar conteudo
                    } else {
                        sendACK=false;
                        nBloco--;
                    }
                } else { //Caso de receber um pacote errado
                    nBloco--;
                }
            } catch (SocketTimeoutException ste){
                nBloco--; //Vai repetir iteração para receber o pacote do bloco desta iteração
            }
        }

        byte[] fileContent = buildFileContent(list, filesize);
        writeToFile(f, fileContent);
    }


    @Override
    public void run(){
        try {
            int nrBlocks = sendRRQ();
            File f = new File(newFileName);
            writeFile(f, nrBlocks, fileSize);
            this.socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
