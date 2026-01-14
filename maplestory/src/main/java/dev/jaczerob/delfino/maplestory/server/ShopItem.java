package dev.jaczerob.delfino.maplestory.server;

/**
 * @author Matze
 */
public class ShopItem {
    private final short buyable;
    private final int itemId;
    private final int price;
    private final int pitch;

    public ShopItem(short buyable, int itemId, int price, int pitch) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.pitch = pitch;
    }

    public short getBuyable() {
        return buyable;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPrice() {
        return price;
    }

    public int getPitch() {
        return pitch;
    }
}
