package dev.jaczerob.delfino.maplestory.client.inventory;

import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataProvider;
import dev.jaczerob.delfino.maplestory.provider.DataProviderFactory;
import dev.jaczerob.delfino.maplestory.provider.DataTool;
import dev.jaczerob.delfino.maplestory.provider.wz.WZFiles;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Danny (Leifde)
 */
public class PetDataFactory {
    private static final DataProvider dataRoot = DataProviderFactory.getDataProvider(WZFiles.ITEM);
    private static final Map<String, PetCommand> petCommands = new HashMap<>();
    private static final Map<Integer, Integer> petHunger = new HashMap<>();

    public static PetCommand getPetCommand(int petId, int skillId) {
        PetCommand ret = petCommands.get(petId + "" + skillId);
        if (ret != null) {
            return ret;
        }
        synchronized (petCommands) {
            ret = petCommands.get(petId + "" + skillId);
            if (ret == null) {
                Data skillData = dataRoot.getData("Pet/" + petId + ".img");
                int prob = 0;
                int inc = 0;
                if (skillData != null) {
                    prob = DataTool.getInt("interact/" + skillId + "/prob", skillData, 0);
                    inc = DataTool.getInt("interact/" + skillId + "/inc", skillData, 0);
                }
                ret = new PetCommand(petId, skillId, prob, inc);
                petCommands.put(petId + "" + skillId, ret);
            }
            return ret;
        }
    }

    public static int getHunger(int petId) {
        Integer ret = petHunger.get(petId);
        if (ret != null) {
            return ret;
        }
        synchronized (petHunger) {
            ret = petHunger.get(petId);
            if (ret == null) {
                ret = DataTool.getInt(dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry"), 1);
            }
            return ret;
        }
    }
}
