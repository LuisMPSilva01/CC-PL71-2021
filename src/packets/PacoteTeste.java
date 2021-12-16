package packets;


public class PacoteTeste {
    public static void main(String[] args) {
        byte[] bytes = new byte[2];
        bytes[0]=1;
        bytes[1]=10;
        DATA pacote = new DATA(2,bytes);
        System.out.println(pacote.verificaIntegridade());
    }
}
