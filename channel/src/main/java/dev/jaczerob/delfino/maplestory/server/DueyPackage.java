package dev.jaczerob.delfino.maplestory.server;

import dev.jaczerob.delfino.maplestory.client.inventory.Item;

import java.sql.Timestamp;
import java.util.Calendar;

import static java.util.concurrent.TimeUnit.DAYS;

public class DueyPackage {
    private String sender = null;
    private Item item = null;
    private int mesos = 0;
    private String message = null;
    private Calendar timestamp;
    private int packageId = 0;

    public DueyPackage(int pId, Item item) {
        this.item = item;
        packageId = pId;
    }

    public DueyPackage(int pId) { // Meso only package.
        this.packageId = pId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String name) {
        sender = name;
    }

    public Item getItem() {
        return item;
    }

    public int getMesos() {
        return mesos;
    }

    public void setMesos(int set) {
        mesos = set;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String m) {
        message = m;
    }

    public int getPackageId() {
        return packageId;
    }

    public long sentTimeInMilliseconds() {
        Calendar ts = timestamp;
        if (ts != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(ts.getTime());
            cal.add(Calendar.MONTH, 1);  // duey representation is in an array of months.

            return cal.getTimeInMillis();
        } else {
            return 0;
        }
    }

    public boolean isDeliveringTime() {
        Calendar ts = timestamp;
        if (ts != null) {
            return ts.getTimeInMillis() >= System.currentTimeMillis();
        } else {
            return false;
        }
    }

    public void setSentTime(Timestamp ts, boolean quick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());

        if (quick) {
            if (System.currentTimeMillis() - ts.getTime() < DAYS.toMillis(1)) {  // thanks inhyuk for noticing quick delivery packages unavailable to retrieve from the get-go
                cal.add(Calendar.DATE, -1);
            }
        }

        this.timestamp = cal;
    }
}
