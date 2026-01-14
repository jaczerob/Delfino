package dev.jaczerob.delfino.maplestory.server;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matze
 */
public class ShopFactory {
    private static final ShopFactory instance = new ShopFactory();

    public static ShopFactory getInstance() {
        return instance;
    }

    private final Map<Integer, Shop> shops = new HashMap<>();
    private final Map<Integer, Shop> npcShops = new HashMap<>();

    private Shop loadShop(int id, boolean isShopId) {
        Shop ret = Shop.createFromDB(id, isShopId);
        if (ret != null) {
            shops.put(ret.getId(), ret);
            npcShops.put(ret.getNpcId(), ret);
        } else if (isShopId) {
            shops.put(id, null);
        } else {
            npcShops.put(id, null);
        }
        return ret;
    }

    public Shop getShop(int shopId) {
        if (shops.containsKey(shopId)) {
            return shops.get(shopId);
        }
        return loadShop(shopId, true);
    }

    public Shop getShopForNPC(int npcId) {
        if (npcShops.containsKey(npcId)) {
            return npcShops.get(npcId);
        }
        return loadShop(npcId, false);
    }

    public void reloadShops() {
        shops.clear();
        npcShops.clear();
    }
}
