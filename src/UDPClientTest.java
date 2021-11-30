import java.io.File;
import java.io.IOException;

public class UDPClientTest {
    public static void listFilesForFolder(final File folder,String path) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry,(path+fileEntry.getName()+"\\\\"));
            } else {
                System.out.println(path+fileEntry.getName());
            }
        }
    }
    public static void listFiles(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName());
        }
    }

    public static void UDPClientTest() throws IOException {
        EchoClient client = new EchoClient();
        client.sendEcho("server is working");

        client.sendEcho("end");
        client.close();
    }
    public static void main(String[] args) throws IOException {
        final File folder = new File("C:\\Users\\Acer\\Desktop");
        listFilesForFolder(folder,"");
        //System.out.println("Client Started");
        //UDPClientTest();
    }
}
