package dev.jaczerob.delfino.maplestory.provider.wz;

import dev.jaczerob.delfino.maplestory.provider.DataEntity;
import dev.jaczerob.delfino.maplestory.provider.DataEntry;

public class WZEntry implements DataEntry {
    private final String name;
    private final int size;
    private final DataEntity parent;

    public WZEntry(String name, int size, DataEntity parent) {
        super();
        this.name = name;
        this.size = size;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public DataEntity getParent() {
        return parent;
    }
}
