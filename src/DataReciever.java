import packets.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


class DataReciever implements Runnable {
    private DatagramSocket socket;
    private int port;
    private InetAddress address;
    private String fileName;
    private String newFileName;
    private long fileSize;
    private final int datablock = 1191;
    private final int timeOut = 100;
    private int nBloco;

    public DataReciever(InetAddress address,int serverPort,String fileName,String newFileName,long fileSize) throws SocketException{
        this.address=address;
        this.port=serverPort;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(timeOut);
        this.fileName=fileName;
        this.newFileName = newFileName;
        this.fileSize=fileSize;
        this.nBloco=-1;
    }

    public void sendPacket(Pacote p) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
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
            if(buf[0] == (byte) 3){
                this.address = packet.getAddress();
                this.port = packet.getPort();
                return new WRQFile(buf);
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
            System.arraycopy(l.get(i), 0, fileContent, i * datablock, l.get(i).length);
        }

        return fileContent;
    }


    public void writeToFile(File file, byte[] buf) throws FileNotFoundException, IOException{
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
                if (buf[0] == 4) { //Verifica se é um pacote de DATA
                    DATA pacote = new DATA(buf);
                    if (pacote.getNBloco() == nBloco) { //Verifica se é o bloco desejado
                        list.add(pacote.getConteudo()); //Guardar conteudo
                    }
                } else { //Caso de receber um pacote errado
                    nBloco--;
                    sendACK=false;
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
