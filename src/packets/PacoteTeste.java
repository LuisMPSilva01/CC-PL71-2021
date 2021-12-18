package packets;


import java.nio.charset.StandardCharsets;

public class PacoteTeste {
    public static void main(String[] args) {
        byte[] bytes = new byte[100];
        String password = "joaozito";
        bytes= password.getBytes(StandardCharsets.UTF_8);

        System.out.println(password.equals(new String(bytes)));
    }
}
