package dev.jaczerob.delfino.maplestory.provider.wz;

import org.springframework.core.io.ClassPathResource;

import java.nio.file.Path;

public enum WZFiles {
    QUEST("Quest"),
    ETC("Etc"),
    ITEM("Item"),
    CHARACTER("Character"),
    STRING("String"),
    LIST("List"),
    MOB("Mob"),
    MAP("Map"),
    NPC("Npc"),
    REACTOR("Reactor"),
    SKILL("Skill"),
    SOUND("Sound"),
    UI("UI");

    public static final String DIRECTORY = getWzDirectory();

    private final String fileName;

    WZFiles(String name) {
        this.fileName = name + ".wz";
    }

    public Path getFile() {
        return Path.of(DIRECTORY, fileName);
    }

    public String getFilePath() {
        return getFile().toString();
    }

    private static String getWzDirectory() {
        return new ClassPathResource("wz").getPath();
    }
}
