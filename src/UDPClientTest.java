import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

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

    public static void UDPClientTest() throws IOException, InterruptedException {
        EchoClient client = new EchoClient();
        client.start();
    }

    public static void main(String[] args){
        try{
            UDPClientTest();
        }
        catch(IOException | InterruptedException ioe){
            ioe.printStackTrace();
        }
    }
}
