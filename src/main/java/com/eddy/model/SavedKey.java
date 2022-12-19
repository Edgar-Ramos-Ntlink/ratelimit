package com.eddy.model;

public class SavedKey {
    private final long blockId;
    private final long blocks;
    private final long trimBefore;
    private final String countKey;
    private final String tsKey;

    public SavedKey(long now, int duration, int precision) {
        precision = Math.min(precision, duration);
        this.blocks = (long) Math.ceil((double) duration / (double) precision);
        this.blockId = (long) Math.floor((double) now / (double) precision);
        this.trimBefore = this.blockId - this.blocks + 1L;
        this.countKey = "" + duration + ':' + precision + ':';
        this.tsKey = this.countKey + 'o';
    }

    public long getBlockId() {
        return blockId;
    }

    public long getBlocks() {
        return blocks;
    }

    public long getTrimBefore() {
        return trimBefore;
    }

    public String getCountKey() {
        return countKey;
    }

    public String getTsKey() {
        return tsKey;
    }
}
