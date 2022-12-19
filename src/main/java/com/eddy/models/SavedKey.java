package com.eddy.models;

public class SavedKey {
    public final long blockId;
    public final long blocks;
    public final long trimBefore;
    public final String countKey;
    public final String tsKey;

    public SavedKey(long now, int duration, int precision) {
        precision = Math.min(precision, duration);
        this.blocks = (long)Math.ceil((double)duration / (double)precision);
        this.blockId = (long)Math.floor((double)now / (double)precision);
        this.trimBefore = this.blockId - this.blocks + 1L;
        this.countKey = "" + duration + ':' + precision + ':';
        this.tsKey = this.countKey + 'o';
    }
}
