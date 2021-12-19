public record DataPlusBlock(byte[] data, int block) implements Comparable<DataPlusBlock> {
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