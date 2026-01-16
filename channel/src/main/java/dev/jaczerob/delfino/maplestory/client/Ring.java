package dev.jaczerob.delfino.maplestory.client;

import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.CashIdGenerator;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;
import dev.jaczerob.delfino.maplestory.tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Danny
 */
public class Ring implements Comparable<Ring> {
    private final int ringId;
    private final int ringId2;
    private final int partnerId;
    private final int itemId;
    private final String partnerName;
    private boolean equipped = false;

    public Ring(int id, int id2, int partnerId, int itemid, String partnername) {
        this.ringId = id;
        this.ringId2 = id2;
        this.partnerId = partnerId;
        this.itemId = itemid;
        this.partnerName = partnername;
    }

    public static Ring loadFromDb(int ringId) {
        Ring ret = null;
        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM rings WHERE id = ?")) {
            ps.setInt(1, ringId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret = new Ring(ringId, rs.getInt("partnerRingId"), rs.getInt("partnerChrId"), rs.getInt("itemid"), rs.getString("partnerName"));
                }
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void removeRing(final Ring ring) {
        try {
            if (ring == null) {
                return;
            }

            try (Connection con = DatabaseConnection.getStaticConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM rings WHERE id=?")) {
                    ps.setInt(1, ring.getRingId());
                    ps.addBatch();

                    ps.setInt(1, ring.getPartnerRingId());
                    ps.addBatch();

                    ps.executeBatch();
                }

                CashIdGenerator.freeCashId(ring.getRingId());
                CashIdGenerator.freeCashId(ring.getPartnerRingId());

                try (PreparedStatement ps = con.prepareStatement("UPDATE inventoryequipment SET ringid=-1 WHERE ringid=?")) {
                    ps.setInt(1, ring.getRingId());
                    ps.addBatch();

                    ps.setInt(1, ring.getPartnerRingId());
                    ps.addBatch();

                    ps.executeBatch();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Pair<Integer, Integer> createRing(int itemid, final Character partner1, final Character partner2) {
        try {
            if (partner1 == null) {
                return new Pair<>(-3, -3);
            } else if (partner2 == null) {
                return new Pair<>(-2, -2);
            }

            int[] ringID = new int[2];
            ringID[0] = CashIdGenerator.generateCashId();
            ringID[1] = CashIdGenerator.generateCashId();

            try (Connection con = DatabaseConnection.getStaticConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO rings (id, itemid, partnerRingId, partnerChrId, partnername) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, ringID[0]);
                    ps.setInt(2, itemid);
                    ps.setInt(3, ringID[1]);
                    ps.setInt(4, partner2.getId());
                    ps.setString(5, partner2.getName());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO rings (id, itemid, partnerRingId, partnerChrId, partnername) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, ringID[1]);
                    ps.setInt(2, itemid);
                    ps.setInt(3, ringID[0]);
                    ps.setInt(4, partner1.getId());
                    ps.setString(5, partner1.getName());
                    ps.executeUpdate();
                }
            }
            return new Pair<>(ringID[0], ringID[1]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new Pair<>(-1, -1);
        }
    }

    public int getRingId() {
        return ringId;
    }

    public int getPartnerRingId() {
        return ringId2;
    }

    public int getPartnerChrId() {
        return partnerId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public boolean equipped() {
        return equipped;
    }

    public void equip() {
        this.equipped = true;
    }

    public void unequip() {
        this.equipped = false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Ring ring) {
            return ring.getRingId() == getRingId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.ringId;
        return hash;
    }

    @Override
    public int compareTo(Ring other) {
        if (ringId < other.getRingId()) {
            return -1;
        } else if (ringId == other.getRingId()) {
            return 0;
        }
        return 1;
    }
}
