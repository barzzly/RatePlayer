package dev.barzzy.RatePlayer.managers;

import dev.barzzy.RatePlayer.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    
    private final Main plugin;
    private Connection connection;
    
    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
    }
    
    public void connect() {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            File dbFile = new File(dataFolder, "ratings.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            
            createTables();
            plugin.getLogger().info("§aSQLite database connected: " + dbFile.getAbsolutePath());
            
        } catch (Exception e) {
            plugin.getLogger().severe("§cFailed to connect to database!");
            e.printStackTrace();
        }
    }
    
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§cError closing database!");
        }
    }
    
    private void createTables() throws SQLException {
        String playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "total_score REAL DEFAULT 0," +
                "total_votes INTEGER DEFAULT 0," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        String ratingsTable = "CREATE TABLE IF NOT EXISTS ratings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "rater_uuid TEXT NOT NULL," +
                "target_uuid TEXT NOT NULL," +
                "stars INTEGER NOT NULL," +
                "rating_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (rater_uuid) REFERENCES players(uuid)," +
                "FOREIGN KEY (target_uuid) REFERENCES players(uuid)" +
                ")";
        
        String cooldownsTable = "CREATE TABLE IF NOT EXISTS cooldowns (" +
                "player_uuid TEXT PRIMARY KEY," +
                "cooldown_until TIMESTAMP," +
                "daily_count INTEGER DEFAULT 0," +
                "last_reset DATE DEFAULT CURRENT_DATE" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(playersTable);
            stmt.execute(ratingsTable);
            stmt.execute(cooldownsTable);
        }
    }
    
    public void addRating(UUID raterUuid, String raterName, UUID targetUuid, String targetName, int stars) {
        try {
            connection.setAutoCommit(false);
            
            // Update or insert players
            updatePlayer(raterUuid, raterName);
            updatePlayer(targetUuid, targetName);
            
            // Add rating
            String sql = "INSERT INTO ratings (rater_uuid, target_uuid, stars) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, raterUuid.toString());
                stmt.setString(2, targetUuid.toString());
                stmt.setInt(3, stars);
                stmt.executeUpdate();
            }
            
            // Update target's total score
            String updateSql = "UPDATE players SET total_score = total_score + ?, total_votes = total_votes + 1 WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                stmt.setInt(1, stars);
                stmt.setString(2, targetUuid.toString());
                stmt.executeUpdate();
            }
            
            // Update cooldown
            updateCooldown(raterUuid);
            
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                plugin.getLogger().severe("Rollback failed!");
            }
            plugin.getLogger().severe("Error adding rating: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
    
    private void updatePlayer(UUID uuid, String name) throws SQLException {
        String sql = "INSERT OR REPLACE INTO players (uuid, name) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }
    
    private void updateCooldown(UUID playerUuid) throws SQLException {
        // Reset daily count if new day
        String resetSql = "UPDATE cooldowns SET daily_count = 0, last_reset = DATE('now') WHERE player_uuid = ? AND last_reset < DATE('now')";
        try (PreparedStatement stmt = connection.prepareStatement(resetSql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        }
        
        // Update cooldown
        String sql = "INSERT OR REPLACE INTO cooldowns (player_uuid, cooldown_until, daily_count) " +
                    "VALUES (?, datetime('now', '+' || ? || ' seconds'), COALESCE((SELECT daily_count + 1 FROM cooldowns WHERE player_uuid = ?), 1))";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setInt(2, plugin.getConfig().getInt("rating.cooldown", 300));
            stmt.setString(3, playerUuid.toString());
            stmt.executeUpdate();
        }
    }
    
    public boolean canRate(UUID playerUuid) {
        try {
            // Check cooldown
            String sql = "SELECT cooldown_until, daily_count FROM cooldowns WHERE player_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Timestamp cooldownUntil = rs.getTimestamp("cooldown_until");
                    int dailyCount = rs.getInt("daily_count");
                    int maxDaily = plugin.getConfig().getInt("rating.max-rating-per-day", 10);
                    
                    if (cooldownUntil != null && cooldownUntil.after(new Timestamp(System.currentTimeMillis()))) {
                        return false; // Masih cooldown
                    }
                    
                    if (dailyCount >= maxDaily) {
                        return false; // Sudah mencapai batas harian
                    }
                }
            }
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking cooldown: " + e.getMessage());
            return false;
        }
    }
    
    public long getRemainingCooldown(UUID playerUuid) {
        try {
            String sql = "SELECT cooldown_until FROM cooldowns WHERE player_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Timestamp cooldownUntil = rs.getTimestamp("cooldown_until");
                    if (cooldownUntil != null) {
                        long remaining = cooldownUntil.getTime() - System.currentTimeMillis();
                        return Math.max(0, remaining / 1000);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting cooldown: " + e.getMessage());
        }
        return 0;
    }
    
    public RatingData getRating(UUID playerUuid) {
        try {
            String sql = "SELECT total_score, total_votes FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    double totalScore = rs.getDouble("total_score");
                    int totalVotes = rs.getInt("total_votes");
                    return new RatingData(totalScore, totalVotes);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting rating: " + e.getMessage());
        }
        return new RatingData(0, 0);
    }
    
    public static class RatingData {
        private final double totalScore;
        private final int totalVotes;
        
        public RatingData(double totalScore, int totalVotes) {
            this.totalScore = totalScore;
            this.totalVotes = totalVotes;
        }
        
        public double getAverage() {
            return totalVotes == 0 ? 0.0 : totalScore / totalVotes;
        }
        
        public int getTotalVotes() {
            return totalVotes;
        }
    }
}