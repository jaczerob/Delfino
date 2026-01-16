package dev.jaczerob.delfino.maplestory.provider.wz;

import dev.jaczerob.delfino.maplestory.provider.DataEntity;
import dev.jaczerob.delfino.maplestory.provider.DataFileEntry;

public class WZFileEntry extends WZEntry implements DataFileEntry {
    private int offset;

    public WZFileEntry(String name, int size, int checksum, DataEntity parent) {
        super(name, size, checksum, parent);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
