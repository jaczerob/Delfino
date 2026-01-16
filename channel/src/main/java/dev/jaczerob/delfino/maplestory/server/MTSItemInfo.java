package dev.jaczerob.delfino.maplestory.server;

import dev.jaczerob.delfino.maplestory.client.inventory.Item;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * @author Traitor
 */
public class MTSItemInfo {
    private final int price;
    private final Item item;
    private final String seller;
    private final int id;
    private final int year;
    private final int month;
    private int day = 1;

    public MTSItemInfo(Item item, int price, int id, int cid, String seller, String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate sellEnd = LocalDate.parse(date, formatter);

        this.item = item;
        this.price = price;
        this.seller = seller;
        this.id = id;
        this.year = sellEnd.getYear();
        this.month = sellEnd.getMonthValue();
        this.day = sellEnd.getDayOfMonth();
    }

    public Item getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }

    public int getTaxes() {
        return 100 + price / 10;
    }

    public int getID() {
        return id;
    }

    public long getEndingDate() {
        Calendar now = Calendar.getInstance();
        now.set(year, month - 1, day);
        return now.getTimeInMillis();
    }

    public String getSeller() {
        return seller;
    }
}
