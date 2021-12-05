package packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class pacoteTeste {
    public static void main(String[] args) {
        RRQFile teste = new RRQFile("ARROZ");
        System.out.println(teste.getFileName());
    }
}
