package dev.jaczerob.delfino.maplestory.provider;

import java.util.List;

/**
 * @author Matze
 */
public interface DataDirectoryEntry extends DataEntry {
    List<DataDirectoryEntry> getSubdirectories();

    List<DataFileEntry> getFiles();

    DataEntry getEntry(String name);
}
