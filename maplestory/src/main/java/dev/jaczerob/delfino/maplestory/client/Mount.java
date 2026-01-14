package dev.jaczerob.delfino.maplestory.client;

/**
 * @author PurpleMadness < Patrick :O >
 */
public class Mount {
    private int itemid;
    private int skillid;
    private int tiredness;
    private int exp;
    private int level;
    private Character owner;
    private boolean active;

    public Mount(Character owner, int id, int skillid) {
        this.itemid = id;
        this.skillid = skillid;
        this.tiredness = 0;
        this.level = 1;
        this.exp = 0;
        this.owner = owner;
        active = true;
    }

    public int getItemId() {
        return itemid;
    }

    public int getSkillId() {
        return skillid;
    }

    /**
     * 1902000 - Hog
     * 1902001 - Silver Mane
     * 1902002 - Red Draco
     * 1902005 - Mimiana
     * 1902006 - Mimio
     * 1902007 - Shinjou
     * 1902008 - Frog
     * 1902009 - Ostrich
     * 1902010 - Frog
     * 1902011 - Turtle
     * 1902012 - Yeti
     *
     * @return the id
     */
    public int getId() {
        if (this.itemid < 1903000) {
            return itemid - 1901999;
        }
        return 5;
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void setTiredness(int newtiredness) {
        this.tiredness = newtiredness;
        if (tiredness < 0) {
            tiredness = 0;
        }
    }

    public int incrementAndGetTiredness() {
        this.tiredness++;
        return this.tiredness;
    }

    public void setExp(int newexp) {
        this.exp = newexp;
    }

    public void setLevel(int newlevel) {
        this.level = newlevel;
    }

    public void setItemId(int newitemid) {
        this.itemid = newitemid;
    }

    public void setSkillId(int newskillid) {
        this.skillid = newskillid;
    }

    public void setActive(boolean set) {
        this.active = set;
    }

    public boolean isActive() {
        return active;
    }

    public void empty() {
        if (owner != null) {
            owner.getClient().getWorldServer().unregisterMountHunger(owner);
        }
        this.owner = null;
    }
}
