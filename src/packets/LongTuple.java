package packets;

public class LongTuple {
    private long A;
    private long B;

    public LongTuple(long A, long B){
        this.A = A;
        this.B = B;
    }

    public long getA(){return this.A;}

    public long getB(){return this.B;}

    public void setA(long A){this.A = A;}

    public void setB(long B){this.B = B;}
}
