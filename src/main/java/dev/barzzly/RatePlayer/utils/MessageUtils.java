package dev.barzzy.RatePlayer.utils;

import dev.barzzy.RatePlayer.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageUtils {
    
    private static Main plugin;
    private static FileConfiguration messages;
    
    public static void init(Main plugin) {
        MessageUtils.plugin = plugin;
        reloadMessages();
    }
    
    public static void reloadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }
    
    public static String get(String path) {
        return color(messages.getString(path, "&cMessage not found: " + path));
    }
    
    public static String get(String path, String... replacements) {
        String message = get(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
    
    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}