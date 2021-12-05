import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import packets.*;

public class UDPClientTest {
    public static void listFilesInFolder(File folder,String path) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesInFolder(fileEntry, path + fileEntry.getName() + "||");
            } else {
                System.out.println(path + fileEntry.getName());
            }
        }
    }

    public static void getFilesInFolder(Map<String, Long> m, File folder, String path) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getFilesInFolder(m, fileEntry,(path+fileEntry.getName()+"||"));
            } else {
                m.put(path + fileEntry.getName(), fileEntry.length());
            }
        }
    }

    public static void listFiles(File folder) {
        for (File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
        }
    }

    public static Map<String, Long> partSynchronized(Map<String, Long> mine, Map<String, Long> other){
        Map<String, Long> res = new HashMap<String, Long>();
        Long l;

        for(Map.Entry<String, Long> entry: other.entrySet()){
            if((l = mine.get(entry.getKey())) != null){
                if(!entry.getValue().equals(l)    ){
                    res.put(entry.getKey(), entry.getValue());
                }
            }
            else{
                res.put(entry.getKey(), entry.getValue());
            }
        }

        return res;
    }

    public static boolean isInSubfolder(String filename){
        boolean res = false;
        char[] v = filename.toCharArray();

        for(int i = 0; i < filename.length() - 1; i++){
            if(v[i] == '|' && v[i + 1] == '|'){
                res = true;
                break;
            }
        }

        return res;
    }

    public static void UDPClientTest() throws IOException {
        EchoClient client = new EchoClient();

        File folder = new File("/home/ray/Downloads/testes");
        File folder2 = new File("/home/ray/Downloads/testes2");

        Map<String, Long> m = new HashMap<String, Long>();
        Map<String, Long> m2 = new HashMap<String, Long>();


        getFilesInFolder(m, folder, "");
        getFilesInFolder(m2, folder2, "");

        FILES f = new FILES(m);
<<<<<<< HEAD
        File test = new File("/home/ray/Downloads/testes/blah.c");
        client.sendFile(test);
=======
        System.out.println(f);

        client.sendPacket(f);
        //File test = new File("/home/ray/Downloads/testes/server.c");
        //byte[] fileContent = Files.readAllBytes(test.toPath());
>>>>>>> b53ab6dcf5d47b91ec8b27837e59a7cfcf58ada9

        //DATA d = new DATA(1, fileContent);
        //client.sendPacket(d);

<<<<<<< HEAD
        //client.sendEcho("end");
        //client.close();
=======
        System.out.println("Pastas syncronizadas: "+(partSynchronized(m, m2)&&partSynchronized(m2,m)));

        FIN end = new FIN();
        client.sendPacket(end);
        client.close();
>>>>>>> b53ab6dcf5d47b91ec8b27837e59a7cfcf58ada9
    }

    public static void main(String[] args){
        try{
            UDPClientTest();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
