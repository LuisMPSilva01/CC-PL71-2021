package packets;

import java.util.Arrays;

public class Pacote {
    byte[] bytes;

    public Pacote(int size) {
        bytes = new byte[size];
    }

    public String toString(){
        return Arrays.toString(bytes);
    }
}
