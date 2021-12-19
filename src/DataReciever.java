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
        //this.socket.setSoTimeout(1000);
        this.fileName=fileName;
        this.newFileName = newFileName;
        this.fileSize=fileSize;
        this.nBloco=-1;
    }

    public void sendPacket(UDP_Packet p) throws IOException {
        byte[] buf = p.getContent();
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
            socket.setSoTimeout(300);
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

    public byte[] buildFileContent(byte[][] ficheiro, Long filesize){
        byte[] fileContent = new byte[Math.toIntExact(filesize)];

        for(int i = 0; i < ficheiro.length ; i++){
            int datablock = 1187;
            System.arraycopy(ficheiro[i], 0, fileContent, i * datablock, ficheiro[i].length);
        }

        return fileContent;
    }


    public void writeToFile(File file, byte[] buf) throws IOException{
        OutputStream os = new FileOutputStream(file);
        os.write(buf);
        os.close();
    }

    public int returnIndex(List<Integer> lista,int target){
        int i=0;
        for (int c:lista){
            if (c==target) return i;
            i++;
        }
        return -1;
    }

    public void writeFile(File f, int nrblocks, Long filesize) throws IOException{
        byte[][] ficheiro = new byte[nrblocks][];
        for (int i=0;i<nrblocks;i++){
            ficheiro[i] = new byte[1];
        }
        boolean sendFirst = true;
        socket.setSoTimeout(50);

        sendPacket(new ACK(-1));
        List<Integer> missing = new ArrayList<>();
        for (int i=0;i<nrblocks;i++){
            missing.add(i);
        }
        while (!missing.isEmpty()) {
            try {
                byte[] buf = new byte[1200]; //Receber pacote
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                this.socket.receive(packet); //Receber pacote
                DATA pacote = new DATA(buf);
                if (pacote.isOK()) { //Verifica se é um pacote de DATA intacto
                    int blocoPacote = pacote.getNBloco();
                    sendFirst=false;
                    System.out.println("Bloco recebido:" + blocoPacote);
                    int index;
                    sendPacket(new ACK(blocoPacote));
                    if ((index=returnIndex(missing,blocoPacote))!=-1) {
                        ficheiro[blocoPacote] = (pacote.getConteudo()); //Guardar conteudo
                        missing.remove(index);
                    }
                }
            } catch (SocketTimeoutException ste){
                if(sendFirst) sendPacket(new ACK(-1));
            }
        }

        byte[] fileContent = buildFileContent(ficheiro, filesize);
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
