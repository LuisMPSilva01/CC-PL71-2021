package packets;

import java.nio.charset.StandardCharsets;

public class ERROR extends Pacote{
    public ERROR(byte errorCode,String errMsg) {
        super(2+1+errMsg.length()+1);
        bytes[0]=0;
        bytes[1]=4;

        bytes[2]= errorCode;

        byte[] fArray = errMsg.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(fArray, 0, bytes, 3, fArray.length);

        bytes[bytes.length-1] = 0;
    }
}
