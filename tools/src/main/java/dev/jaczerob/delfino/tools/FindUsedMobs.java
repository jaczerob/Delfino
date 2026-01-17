package dev.jaczerob.delfino.tools;

import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class FindUsedMobs {
    public static void main(final String... args) throws Exception {
        final var builderFactory = DocumentBuilderFactory.newInstance();
        final var xpath = XPathFactory.newInstance().newXPath().compile("//imgdir/imgdir[@name='life']/imgdir[string[@name='type' and @value='m']]/string[@name='id']");

        final var mobIds = new HashSet<String>();
        try (final var ds = Files.newDirectoryStream(Path.of("wz/Map.wz/Map/Map0"))) {
            for (final var path : ds) {
                try (final var fis = Files.newInputStream(path)) {
                    final var builder = builderFactory.newDocumentBuilder();
                    final var document = builder.parse(fis);
                    final var nodes = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        final var node = nodes.item(i);
                        final var mobId = node.getAttributes().getNamedItem("value").getNodeValue();
                        mobIds.add(mobId);
                    }
                }
            }
        }

        final var mobFileNames = mobIds.stream()
                .mapToInt(Integer::parseInt)
                .sorted()
                .mapToObj(id -> String.format("%07d.img.xml", id))
                .toList();

        final var currentMobFiles = mobFileNames.stream()
                .map(name -> Path.of("wz/Mob.wz", name))
                .filter(Files::exists)
                .toList();

        final var newMobFiles = mobFileNames.stream()
                .map(name -> Path.of("wz/NewMob.wz", name))
                .filter(Files::exists)
                .toList();

        final var backupDir = Path.of("wz/Mob.wz.bak");
        if (Files.notExists(backupDir)) {
            Files.createDirectories(backupDir);
        }

        for (final var path : currentMobFiles) {
            final var backupPath = Path.of(backupDir.toString(), path.getFileName().toString());
            System.out.println("Backing up " + path + " to " + backupPath);

            Files.writeString(Path.of(backupDir.toString(), path.getFileName().toString()), Files.readString(path));
        }
    }
}
