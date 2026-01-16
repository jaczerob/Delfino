package dev.jaczerob.delfino.maplestory.server.life;

import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataProvider;
import dev.jaczerob.delfino.maplestory.provider.DataProviderFactory;
import dev.jaczerob.delfino.maplestory.provider.DataTool;
import dev.jaczerob.delfino.maplestory.provider.wz.WZFiles;
import dev.jaczerob.delfino.maplestory.tools.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Danny (Leifde)
 */
public class MobAttackInfoFactory {
    private static final Map<String, MobAttackInfo> mobAttacks = new HashMap<>();
    private static final DataProvider dataSource = DataProviderFactory.getDataProvider(WZFiles.MOB);

    public static MobAttackInfo getMobAttackInfo(Monster mob, int attack) {
        MobAttackInfo ret = mobAttacks.get(mob.getId() + "" + attack);
        if (ret != null) {
            return ret;
        }
        synchronized (mobAttacks) {
            ret = mobAttacks.get(mob.getId() + "" + attack);
            if (ret == null) {
                Data mobData = dataSource.getData(StringUtil.getLeftPaddedStr(mob.getId() + ".img", '0', 11));
                if (mobData != null) {
//					MapleData infoData = mobData.getChildByPath("info");
                    String linkedmob = DataTool.getString("link", mobData, "");
                    if (!linkedmob.equals("")) {
                        mobData = dataSource.getData(StringUtil.getLeftPaddedStr(linkedmob + ".img", '0', 11));
                    }
                    Data attackData = mobData.getChildByPath("attack" + (attack + 1) + "/info");

                    if (attackData == null) {
                        return null;
                    }

                    Data deadlyAttack = attackData.getChildByPath("deadlyAttack");
                    int mpBurn = DataTool.getInt("mpBurn", attackData, 0);
                    int disease = DataTool.getInt("disease", attackData, 0);
                    int level = DataTool.getInt("level", attackData, 0);
                    int mpCon = DataTool.getInt("conMP", attackData, 0);
                    ret = new MobAttackInfo(mob.getId(), attack);
                    ret.setDeadlyAttack(deadlyAttack != null);
                    ret.setMpBurn(mpBurn);
                    ret.setDiseaseSkill(disease);
                    ret.setDiseaseLevel(level);
                    ret.setMpCon(mpCon);
                }
                mobAttacks.put(mob.getId() + "" + attack, ret);
            }
            return ret;
        }
    }
}
