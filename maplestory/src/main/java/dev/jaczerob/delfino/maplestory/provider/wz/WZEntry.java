package dev.jaczerob.delfino.maplestory.provider.wz;

import dev.jaczerob.delfino.maplestory.provider.DataEntity;
import dev.jaczerob.delfino.maplestory.provider.DataEntry;

public class WZEntry implements DataEntry {
    private final String name;
    private final int size;
    private final int checksum;
    private int offset;
    private final DataEntity parent;

    public WZEntry(String name, int size, int checksum, DataEntity parent) {
        super();
        this.name = name;
        this.size = size;
        this.checksum = checksum;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getChecksum() {
        return checksum;
    }

    public int getOffset() {
        return offset;
    }

    public DataEntity getParent() {
        return parent;
    }
}
