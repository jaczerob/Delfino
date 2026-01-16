package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.inventory.Item;

/**
 * @author Matze
 */
public class PlayerShopItem {
    private final Item item;
    private short bundles;
    private final int price;
    private boolean doesExist;

    public PlayerShopItem(Item item, short bundles, int price) {
        this.item = item;
        this.bundles = bundles;
        this.price = price;
        this.doesExist = true;
    }

    public void setDoesExist(boolean tf) {
        this.doesExist = tf;
    }

    public boolean isExist() {
        return doesExist;
    }

    public Item getItem() {
        return item;
    }

    public short getBundles() {
        return bundles;
    }

    public int getPrice() {
        return price;
    }

    public void setBundles(short bundles) {
        this.bundles = bundles;
    }
}