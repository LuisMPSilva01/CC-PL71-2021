public class DataPlusBlock implements Comparable<DataPlusBlock> {
    private final byte[] data;
    private final int block;
    public DataPlusBlock(byte[] data, int block) {
        this.data = data.clone();
        this.block = block;
    }

    public byte[] getData() {
        return this.data.clone();
    }

    public int getBlock() {
        return this.block;
    }

    @Override
    public int compareTo(DataPlusBlock dpb) {
        return this.getBlock() - dpb.getBlock();
    }
}