package dev.jaczerob.delfino.maplestory.net.server.task;

import dev.jaczerob.delfino.maplestory.client.Job;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Matze
 * @author Quit
 * @author Ronan
 */
public class RankingLoginTask implements Runnable {
    private long lastUpdate = System.currentTimeMillis();

    private void resetMoveRank(boolean job) throws SQLException {
        String query = "UPDATE characters SET " + (job ? "jobRankMove = 0" : "rankMove = 0");
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            PreparedStatement reset = con.prepareStatement(query);
            reset.executeUpdate();
        }
    }

    private void updateRanking(int job, int world) throws SQLException {
        String sqlCharSelect = "SELECT c.id, " + (job != -1 ? "c.jobRank, c.jobRankMove" : "c.rank, c.rankMove") + ", a.lastlogin AS lastlogin, a.loggedin FROM characters AS c LEFT JOIN accounts AS a ON c.accountid = a.id WHERE c.gm < 2 AND c.world = ? ";
        if (job != -1) {
            sqlCharSelect += "AND c.job DIV 100 = ? ";
        }
        sqlCharSelect += "ORDER BY c.level DESC , c.exp DESC , c.lastExpGainTime ASC, c.fame DESC , c.meso DESC";

        try (Connection con = DatabaseConnection.getStaticConnection();
             PreparedStatement charSelect = con.prepareStatement(sqlCharSelect)) {
            charSelect.setInt(1, world);
            if (job != -1) {
                charSelect.setInt(2, job);
            }

            try (ResultSet rs = charSelect.executeQuery();
                 PreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (job != -1 ? "jobRank = ?, jobRankMove = ? " : "rank = ?, rankMove = ? ") + "WHERE id = ?")) {
                int rank = 0;

                while (rs.next()) {
                    int rankMove = 0;
                    rank++;

                    final long lastlogin = rs.getTimestamp("lastlogin").getTime();
                    if (lastlogin < lastUpdate || rs.getInt("loggedin") > 0) {
                        rankMove = rs.getInt((job != -1 ? "jobRankMove" : "rankMove"));
                    }
                    rankMove += rs.getInt((job != -1 ? "jobRank" : "rank")) - rank;
                    ps.setInt(1, rank);
                    ps.setInt(2, rankMove);
                    ps.setInt(3, rs.getInt("id"));
                    ps.executeUpdate();
                }
            }
        }
    }

    @Override
    public void run() {
        try (Connection con = DatabaseConnection.getStaticConnection()) {
            con.setAutoCommit(false);

            try {
                if (YamlConfig.config.server.USE_REFRESH_RANK_MOVE) {
                    resetMoveRank(true);
                    resetMoveRank(false);
                }

                for (int j = 0; j < Server.getInstance().getWorldsSize(); j++) {
                    updateRanking(-1, j);    //overall ranking
                    for (int i = 0; i <= Job.getMax(); i++) {
                        updateRanking(i, j);
                    }
                    con.commit();
                }

                con.setAutoCommit(true);
                lastUpdate = System.currentTimeMillis();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
