package dev.jaczerob.delfino.maplestory.net.server;

import dev.jaczerob.delfino.maplestory.client.Disease;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.tools.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Danny//changed to map :3
 * @author Ronan//debuffs to storage as well
 */
public class PlayerBuffStorage {
    private final int id = (int) (Math.random() * 100);
    private final Lock lock = new ReentrantLock(true);
    private final Map<Integer, List<PlayerBuffValueHolder>> buffs = new HashMap<>();
    private final Map<Integer, Map<Disease, Pair<Long, MobSkill>>> diseases = new HashMap<>();

    public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) {
        lock.lock();
        try {
            buffs.put(chrid, toStore);//Old one will be replaced if it's in here.
        } finally {
            lock.unlock();
        }
    }

    public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) {
        lock.lock();
        try {
            return buffs.remove(chrid);
        } finally {
            lock.unlock();
        }
    }

    public void addDiseasesToStorage(int chrid, Map<Disease, Pair<Long, MobSkill>> toStore) {
        lock.lock();
        try {
            diseases.put(chrid, toStore);
        } finally {
            lock.unlock();
        }
    }

    public Map<Disease, Pair<Long, MobSkill>> getDiseasesFromStorage(int chrid) {
        lock.lock();
        try {
            return diseases.remove(chrid);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerBuffStorage other = (PlayerBuffStorage) obj;
        return id == other.id;
    }
}
