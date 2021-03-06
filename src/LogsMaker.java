import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class LogsMaker {
    private final FileOutputStream logs;
    private final ReentrantLock lock= new ReentrantLock();
    private final long startSize;
    private final Date startTime;
    private final String filename;
    private final String peer;
    private final DateFormat DFormat=  DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault()); //Date format
    private final List<String> semEspaco= new ArrayList<>();
    private final List<String> enviados= new ArrayList<>();
    private final List<String> recebidos= new ArrayList<>();

    public LogsMaker(String filename, String peer) throws IOException {
        this.filename=filename;
        this.peer=peer;
        this.logs = new FileOutputStream("logs", true);
        this.startSize = Files.walk(Paths.get(filename)) //Get folder starting size
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
        this.startTime = new Date();
    }

    public void recebido(String filename){
        this.lock.lock();
        try {
            recebidos.add(filename);
        } finally {
            this.lock.unlock();
        }
    }
    public void noSpace(String filename){
        this.lock.lock();
        try {
            semEspaco.add(filename);
        } finally {
            this.lock.unlock();
        }
    }

    public void enviado(String filename){
        this.lock.lock();
        try {
            enviados.add(filename);
        } finally {
            this.lock.unlock();
        }
    }

    public void finish() throws IOException {
        this.lock.lock();
        try {
            Date finish = new Date();
            long timeTaken = finish.getTime()-startTime.getTime();
            long endSize = Files.walk(Paths.get(filename)) //Get folder ending size
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
            long dataTransferida = endSize-startSize;

            logs.write(("INFO: FFSync "+ filename +" " +  peer + "\n" +
                    "Date: "+ DFormat.format(startTime.getTime()) +"\n"+
                    "Time taken: " + (timeTaken) + " miliseconds\n" +
                    "Tamanho inicial da pasta: " + (startSize) + " bytes\n" +
                    "Tamanho final da pasta: " + (endSize) + " bytes\n" +
                    "Data transferida: " + dataTransferida + " bytes\n" +
                    "Bitrate: " + ((float)dataTransferida/timeTaken*1000*8) + " bits/segundo\n" + //Como conseguir o d??bito real?
                    "Ficheiros enviados:\n"+
                    enviados + "\n"+
                    "Ficheiros recebidos:\n"+
                    recebidos+ "\n").getBytes());
            if(semEspaco.size()>0){
                logs.write(("Sem espaco para os seguintes ficheiros\n:"+ semEspaco + "\n").getBytes(StandardCharsets.UTF_8));
            }
            logs.write("-----------------------------------------------------\n".getBytes(StandardCharsets.UTF_8));
            logs.close();
        } finally {
            this.lock.unlock();
        }
    }
}
