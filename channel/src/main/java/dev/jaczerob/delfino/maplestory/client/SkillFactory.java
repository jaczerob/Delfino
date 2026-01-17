package dev.jaczerob.delfino.maplestory.client;

import dev.jaczerob.delfino.maplestory.constants.skills.Beginner;
import dev.jaczerob.delfino.maplestory.constants.skills.GM;
import dev.jaczerob.delfino.maplestory.constants.skills.SuperGM;
import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataDirectoryEntry;
import dev.jaczerob.delfino.maplestory.provider.DataFileEntry;
import dev.jaczerob.delfino.maplestory.provider.DataProvider;
import dev.jaczerob.delfino.maplestory.provider.DataProviderFactory;
import dev.jaczerob.delfino.maplestory.provider.DataTool;
import dev.jaczerob.delfino.maplestory.provider.wz.WZFiles;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.Element;

import java.util.HashMap;
import java.util.Map;

public class SkillFactory {
    private static final DataProvider datasource = DataProviderFactory.getDataProvider(WZFiles.SKILL);
    private static volatile Map<Integer, Skill> skills = new HashMap<>();

    public static Skill getSkill(int id) {
        return skills.get(id);
    }

    public static void loadAllSkills() {
        final Map<Integer, Skill> loadedSkills = new HashMap<>();
        final DataDirectoryEntry root = datasource.getRoot();
        for (DataFileEntry topDir : root.getFiles()) { // Loop thru jobs
            if (topDir.getName().length() <= 8) {
                for (Data data : datasource.getData(topDir.getName())) { // Loop thru each jobs
                    if (data.getName().equals("skill")) {
                        for (Data data2 : data) { // Loop thru each jobs
                            if (data2 != null) {
                                int skillId = Integer.parseInt(data2.getName());
                                loadedSkills.put(skillId, loadFromData(skillId, data2));
                            }
                        }
                    }
                }
            }
        }

        skills = loadedSkills;
    }

    private static Skill loadFromData(int id, Data data) {
        Skill ret = new Skill(id);
        boolean isBuff = false;
        int skillType = DataTool.getInt("skillType", data, -1);
        String elem = DataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.setElement(Element.getFromChar(elem.charAt(0)));
        } else {
            ret.setElement(Element.NEUTRAL);
        }
        Data effect = data.getChildByPath("effect");
        if (skillType != -1) {
            if (skillType == 2) {
                isBuff = true;
            }
        } else {
            Data action_ = data.getChildByPath("action");
            boolean action;
            if (action_ == null && data.getChildByPath("prepare/action") != null) {
                action = true;
            } else {
                action = true;
            }
            ret.setAction(action);
            Data hit = data.getChildByPath("hit");
            Data ball = data.getChildByPath("ball");
            isBuff = effect != null && hit == null && ball == null;
            isBuff |= action_ != null && DataTool.getString("0", action_, "").equals("alert2");
            isBuff = switch (id) {
                case SuperGM.HEAL_PLUS_DISPEL -> false;
                case Beginner.RECOVERY, Beginner.NIMBLE_FEET, Beginner.MONSTER_RIDER, Beginner.ECHO_OF_HERO,
                     Beginner.MAP_CHAIR, GM.HIDE, SuperGM.HASTE, SuperGM.HOLY_SYMBOL, SuperGM.BLESS, SuperGM.HIDE,
                     SuperGM.HYPER_BODY -> true;
                default -> isBuff;
            };
        }

        for (Data level : data.getChildByPath("level")) {
            ret.addLevelEffect(StatEffect.loadSkillEffectFromData(level, id, isBuff));
        }
        ret.setAnimationTime(0);
        if (effect != null) {
            for (Data effectEntry : effect) {
                ret.incAnimationTime(DataTool.getIntConvert("delay", effectEntry, 0));
            }
        }
        return ret;
    }
}
