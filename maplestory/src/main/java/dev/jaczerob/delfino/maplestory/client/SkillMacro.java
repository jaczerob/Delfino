package dev.jaczerob.delfino.maplestory.client;

public class SkillMacro {
    private int skill1;
    private int skill2;
    private int skill3;
    private final String name;
    private final int shout;
    private final int position;

    public SkillMacro(int skill1, int skill2, int skill3, String name, int shout, int position) {
        this.skill1 = skill1;
        this.skill2 = skill2;
        this.skill3 = skill3;
        this.name = name;
        this.shout = shout;
        this.position = position;
    }

    public int getSkill1() {
        return skill1;
    }

    public int getSkill2() {
        return skill2;
    }

    public int getSkill3() {
        return skill3;
    }

    public void setSkill1(int skill) {
        skill1 = skill;
    }

    public void setSkill2(int skill) {
        skill2 = skill;
    }

    public void setSkill3(int skill) {
        skill3 = skill;
    }

    public String getName() {
        return name;
    }

    public int getShout() {
        return shout;
    }

    public int getPosition() {
        return position;
    }
}
