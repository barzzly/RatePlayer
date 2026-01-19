package dev.barzzy.RatePlayer.managers;

import dev.barzzy.RatePlayer.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class RatingManager {
    
    private final Main plugin;
    private final DatabaseManager database;
    
    public RatingManager(Main plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }
    
    public void openRatingMenu(Player rater, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lRate: " + target.getName());
        
        // Add star items
        gui.setItem(11, createStarItem(1, "§e★☆☆☆☆ §7(1 Star)"));
        gui.setItem(12, createStarItem(2, "§6★★☆☆☆ §7(2 Stars)"));
        gui.setItem(13, createStarItem(3, "§d★★★☆☆ §7(3 Stars)"));
        gui.setItem(14, createStarItem(4, "§b★★★★☆ §7(4 Stars)"));
        gui.setItem(15, createStarItem(5, "§a★★★★★ §7(5 Stars)"));
        
        // Add info item
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§bInfo");
        
        DatabaseManager.RatingData data = database.getRating(target.getUniqueId());
        double rating = data.getAverage();
        int votes = data.getTotalVotes();
        
        meta.setLore(Arrays.asList(
                "§7Player: §f" + target.getName(),
                "§7Current Rating: §e" + String.format("%.1f", rating) + "★",
                "§7Total Votes: §f" + votes,
                "",
                "§eClick stars to rate!"
        ));
        info.setItemMeta(meta);
        gui.setItem(22, info);
        
        // Fill borders
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }
        
        rater.openInventory(gui);
    }
    
    private ItemStack createStarItem(int stars, String name) {
        Material material;
        switch (stars) {
            case 1: material = Material.YELLOW_DYE; break;
            case 2: material = Material.ORANGE_DYE; break;
            case 3: material = Material.MAGENTA_DYE; break;
            case 4: material = Material.LIGHT_BLUE_DYE; break;
            case 5: material = Material.LIME_DYE; break;
            default: material = Material.YELLOW_DYE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(
                "§7Click to give " + stars + " star" + (stars > 1 ? "s" : ""),
                "§7to this player"
        ));
        
        // Store star count in custom data
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "stars"),
            org.bukkit.persistence.PersistentDataType.INTEGER,
            stars
        );
        
        item.setItemMeta(meta);
        return item;
    }
    
    public void handleRating(Player rater, int stars, Player target) {
        UUID raterUuid = rater.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        
        // Check if can rate
        if (!database.canRate(raterUuid)) {
            long cooldown = database.getRemainingCooldown(raterUuid);
            if (cooldown > 0) {
                rater.sendMessage("§cTunggu " + cooldown + " detik lagi!");
                return;
            }
            rater.sendMessage("§cSudah mencapai batas rating hari ini!");
            return;
        }
        
        // Check self-rating
        if (raterUuid.equals(targetUuid) && !plugin.getConfig().getBoolean("rating.allow-self-rating")) {
            rater.sendMessage("§cTidak bisa rate diri sendiri!");
            return;
        }
        
        // Add rating to database
        database.addRating(raterUuid, rater.getName(), targetUuid, target.getName(), stars);
        
        // Send messages
        rater.sendMessage("§aKamu memberi " + stars + "★ kepada " + target.getName());
        
        if (target.isOnline()) {
            target.sendMessage("§6" + rater.getName() + " §amemberi kamu " + stars + "★");
        }
        
        // Broadcast
        if (plugin.getConfig().getBoolean("rating.broadcast-rating")) {
            Bukkit.broadcastMessage("§6" + rater.getName() + " §7memberi §6" + 
                    target.getName() + " §7rating §e" + stars + "★");
        }
    }
    
    public String getRatingDisplay(UUID playerUuid) {
        DatabaseManager.RatingData data = database.getRating(playerUuid);
        if (data.getTotalVotes() == 0) {
            return "§7No ratings yet";
        }
        
        double avg = data.getAverage();
        int fullStars = (int) avg;
        boolean halfStar = (avg - fullStars) >= 0.5;
        
        StringBuilder stars = new StringBuilder("§6");
        for (int i = 0; i < fullStars; i++) stars.append("★");
        if (halfStar) stars.append("✫");
        stars.append("§7");
        for (int i = (halfStar ? fullStars + 1 : fullStars); i < 5; i++) stars.append("☆");
        
        return stars.toString() + " §7(" + String.format("%.1f", avg) + " from " + data.getTotalVotes() + " votes)";
    }
}