package dev.barzzy.RatePlayer.gui;

import dev.barzzy.RatePlayer.Main;
import dev.barzzy.RatePlayer.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class RatingGUI {

    private final Main plugin;

    public RatingGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openRatingGUI(Player rater, Player target) {
        String title = "§6§lRate: " + target.getName();
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Add star items
        gui.setItem(11, createStarItem(1));
        gui.setItem(12, createStarItem(2));
        gui.setItem(13, createStarItem(3));
        gui.setItem(14, createStarItem(4));
        gui.setItem(15, createStarItem(5));

        // Add info item
        gui.setItem(22, createInfoItem(target));

        // Fill empty slots with glass
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }

        rater.openInventory(gui);
    }

    private ItemStack createStarItem(int stars) {
        Material material;
        String name;

        switch (stars) {
            case 1:
                material = Material.YELLOW_DYE;
                name = "§e★☆☆☆☆ §7(1 Star)";
                break;
            case 2:
                material = Material.ORANGE_DYE;
                name = "§6★★☆☆☆ §7(2 Stars)";
                break;
            case 3:
                material = Material.MAGENTA_DYE;
                name = "§d★★★☆☆ §7(3 Stars)";
                break;
            case 4:
                material = Material.LIGHT_BLUE_DYE;
                name = "§b★★★★☆ §7(4 Stars)";
                break;
            case 5:
                material = Material.LIME_DYE;
                name = "§a★★★★★ §7(5 Stars)";
                break;
            default:
                material = Material.YELLOW_DYE;
                name = "§e★☆☆☆☆ §7(1 Star)";
        }

        ItemBuilder builder = new ItemBuilder(material)
                .setName(name)
                .setLore(Arrays.asList(
                        "§7Click to give " + stars + " star" + (stars > 1 ? "s" : ""),
                        "§7to the player"
                ))
                .setCustomData("stars", String.valueOf(stars));

        return builder.build();
    }

    private ItemStack createInfoItem(Player target) {
        ItemBuilder builder = new ItemBuilder(Material.PAPER)
                .setName("§bPlayer Info")
                .setLore(Arrays.asList(
                        "§7Player: §f" + target.getName(),
                        "§7Click stars above to rate!",
                        "",
                        "§e1★ = Poor",
                        "§62★ = Fair",
                        "§d3★ = Good",
                        "§b4★ = Very Good",
                        "§a5★ = Excellent"
                ));

        return builder.build();
    }
}