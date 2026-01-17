package dev.jaczerob.delfino.maplestory.provider.wz;

import dev.jaczerob.delfino.maplestory.provider.DataEntity;
import dev.jaczerob.delfino.maplestory.provider.DataFileEntry;

public class WZFileEntry extends WZEntry implements DataFileEntry {
    public WZFileEntry(String name, int size, DataEntity parent) {
        super(name, size, parent);
    }
}
