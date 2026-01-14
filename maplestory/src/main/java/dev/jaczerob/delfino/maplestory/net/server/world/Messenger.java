package dev.jaczerob.delfino.maplestory.net.server.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Messenger {

    private final int id;
    private final List<MessengerCharacter> members = new ArrayList<>(3);
    private final boolean[] pos = new boolean[3];

    public Messenger(int id, MessengerCharacter chrfor) {
        this.id = id;
        for (int i = 0; i < 3; i++) {
            pos[i] = false;
        }
        addMember(chrfor, chrfor.getPosition());
    }

    public int getId() {
        return id;
    }

    public Collection<MessengerCharacter> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(MessengerCharacter member, int position) {
        members.add(member);
        member.setPosition(position);
        pos[position] = true;
    }

    public void removeMember(MessengerCharacter member) {
        int position = member.getPosition();
        pos[position] = false;
        members.remove(member);
    }

    public int getLowestPosition() {
        for (byte i = 0; i < 3; i++) {
            if (!pos[i]) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionByName(String name) {
        for (MessengerCharacter messengerchar : members) {
            if (messengerchar.getName().equals(name)) {
                return messengerchar.getPosition();
            }
        }
        return -1;
    }
}

