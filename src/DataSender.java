import packets.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;


class DataSender implements Runnable {
    private DatagramSocket socket;
    private final int port;
    private final InetAddress address;
    private String fileName;
    private final int datablock = 1191;

    public DataSender(RRQFile rrqFile,InetAddress address,int port) throws SocketException {
        this.address=address;
        this.port=port;
        this.socket = new DatagramSocket();
        this.fileName=rrqFile.getFileName();
    }

    public void sendPacket(Pacote p, InetAddress address, int port) throws IOException {
        byte[] buf = p.getContent();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public int sendWRQ(File file) throws IOException {
        int nrblocks = blocksNeeded(file.length());
        do {
            sendPacket(new WRQFile(nrblocks), address, port);
        } while (waitACK()!=-1);
        return nrblocks;
    }

    public int blocksNeeded(Long l){
        return (int) (Math.floorDiv(l, datablock) + 1);
    }

    public int waitACK() throws IOException {
        byte[] buf = new byte[1200];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            if(buf[0]!=5){
                return -2;
            } else {
                ACK ack = new ACK(buf);
                return ack.getNBloco();
            }
        }
        catch (SocketTimeoutException e) {
            return -2;
        }
    }

    public void sendFile(String fileName, int nBlocos, InetAddress address, int port) throws IOException{
        File f = new File(fileName);
        int filesize = (int) f.length();
        FileInputStream fis = new FileInputStream(fileName);

        for(int i = 0,repeticoes=5; i < nBlocos; i++){  //Repetições vai ser usado na ultima iteração para quebrar o ciclo caso não receba o ultimo ack(pode ter sido perdido)
            byte[] blockContent;
            if(i == nBlocos - 1){
                blockContent = new byte[filesize - (i * datablock)]; //Ultimo bloco
            }
            else{
                blockContent = new byte[datablock]; //Outros
            }

            fis.read(blockContent);
            DATA d = new DATA(i, blockContent); //Guardar conteudo num DATA packet

            do{
                sendPacket(d, address, port); //Enviar pacote
                if(i==nBlocos-1){ //Caso seja a ultima iteração
                    repeticoes--;
                    if(repeticoes==0) break;
                }
            } while (waitACK()!=(i)); //Repetir enquanto não receber ack do pacote
        }
        fis.close();
    }

    public void run(){
        try {
            File f = new File(this.fileName);
            int filesize = (int) f.length();
            int nrBlocks = sendWRQ(f);
            sendFile(fileName,nrBlocks,address,port);
            this.socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}