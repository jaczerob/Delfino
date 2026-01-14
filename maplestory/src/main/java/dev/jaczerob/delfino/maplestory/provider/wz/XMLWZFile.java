package dev.jaczerob.delfino.maplestory.provider.wz;

import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataDirectoryEntry;
import dev.jaczerob.delfino.maplestory.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLWZFile implements DataProvider {
    private static final Logger log = LoggerFactory.getLogger(DataProvider.class);
    private final Path root;
    private final WZDirectoryEntry rootForNavigation;

    public XMLWZFile(Path fileIn) {
        root = fileIn;
        rootForNavigation = new WZDirectoryEntry(fileIn.getFileName().toString(), 0, 0, null);
        fillMapleDataEntitys(root, rootForNavigation);
    }

    private void fillMapleDataEntitys(Path lroot, WZDirectoryEntry wzdir) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lroot)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                if (Files.isDirectory(path) && !fileName.endsWith(".img")) {
                    WZDirectoryEntry newDir = new WZDirectoryEntry(fileName, 0, 0, wzdir);
                    wzdir.addDirectory(newDir);
                    fillMapleDataEntitys(path, newDir);
                } else if (fileName.endsWith(".xml")) {
                    wzdir.addFile(new WZFileEntry(fileName.substring(0, fileName.length() - 4), 0, 0, wzdir));
                }
            }
        } catch (IOException e) {
            log.warn("Can not open file/directory at " + lroot.toAbsolutePath().toString());
        }
    }

    @Override
    public synchronized Data getData(String path) {
        Path dataFile = root.resolve(path + ".xml");
        Path imageDataDir = root.resolve(path);
        if (!Files.exists(dataFile)) {
            return null;
        }
        final XMLDomMapleData domMapleData;
        try (FileInputStream fis = new FileInputStream(dataFile.toString())) {
            domMapleData = new XMLDomMapleData(fis, imageDataDir.getParent());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Datafile " + path + " does not exist in " + root.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return domMapleData;
    }

    @Override
    public DataDirectoryEntry getRoot() {
        return rootForNavigation;
    }
}