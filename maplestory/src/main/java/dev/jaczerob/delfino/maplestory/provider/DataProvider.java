package dev.jaczerob.delfino.maplestory.provider;

public interface DataProvider {
    Data getData(String path);

    DataDirectoryEntry getRoot();
}
