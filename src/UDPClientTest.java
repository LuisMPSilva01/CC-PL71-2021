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

    //verifica se uma pasta tem todos os elementos de outra
    public static boolean partSynchronized(Map<String, Long> mine, Map<String, Long> other){
        boolean res = true;
        Long l;

        for(Map.Entry<String, Long> entry: other.entrySet()){
            if((l = mine.get(entry.getKey())) != null){
                if(!entry.getValue().equals(l)){
                    res = false; break;
                }
            }
            else{
                res = false; break;
            }
        }

        return res;
    }

    public static void UDPClientTest() throws IOException {
        EchoClient client = new EchoClient();

        File folder = new File("/home/ray/Downloads/testes");
        File folder2 = new File("/home/ray/Downloads/teste");

        Map<String, Long> m = new HashMap<String, Long>();
        Map<String, Long> m2 = new HashMap<String, Long>();


        getFilesInFolder(m, folder, "");
        getFilesInFolder(m2, folder2, "");

        //FILES f = new FILES(m);

        File test = new File("/home/ray/Downloads/testes/server.c");
        byte[] fileContent = Files.readAllBytes(test.toPath());

        DATA d = new DATA(1, fileContent);
        client.sendPacket(d);

        System.out.println(partSynchronized(m, m2));
        
        //client.sendEcho("end");
        //client.close();
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
