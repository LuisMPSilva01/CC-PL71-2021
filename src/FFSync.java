import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;


public class FFSync {
    private static boolean netIsAvailable() {
        try {
            URL url = new URL("https://www.google.com");
            URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args){
        if(args.length!=2){
            System.out.println("Formato errado, tente : FFSync pasta1 10.1.1.1");
            return;
        }
        System.out.println("Net is: "+netIsAvailable()); //Adicionar returns caso falso
        System.out.println("Ficheiro existe: "+Files.exists(Path.of(args[0]))); //Adicionar returns caso falso
    }
}
