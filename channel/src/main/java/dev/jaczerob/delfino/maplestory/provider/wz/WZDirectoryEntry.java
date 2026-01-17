package dev.jaczerob.delfino.maplestory.provider.wz;

import dev.jaczerob.delfino.maplestory.provider.DataDirectoryEntry;
import dev.jaczerob.delfino.maplestory.provider.DataEntity;
import dev.jaczerob.delfino.maplestory.provider.DataEntry;
import dev.jaczerob.delfino.maplestory.provider.DataFileEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WZDirectoryEntry extends WZEntry implements DataDirectoryEntry {
    private final List<DataDirectoryEntry> subdirs = new ArrayList<>();
    private final List<DataFileEntry> files = new ArrayList<>();
    private final Map<String, DataEntry> entries = new HashMap<>();

    public WZDirectoryEntry(String name, int size, DataEntity parent) {
        super(name, size, parent);
    }

    public void addDirectory(DataDirectoryEntry dir) {
        subdirs.add(dir);
        entries.put(dir.getName(), dir);
    }

    public void addFile(DataFileEntry fileEntry) {
        files.add(fileEntry);
        entries.put(fileEntry.getName(), fileEntry);
    }

    public List<DataDirectoryEntry> getSubdirectories() {
        return Collections.unmodifiableList(subdirs);
    }

    public List<DataFileEntry> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public DataEntry getEntry(String name) {
        return entries.get(name);
    }
}
