package dev.barzzy.RatePlayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("§aRatePlayer enabled!");
        saveDefaultConfig();

        // Register event listener
        getServer().getPluginManager().registerEvents(new dev.barzzy.RatePlayer.listeners.GUIListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("§cRatePlayer disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("rate")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cHanya player bisa pakai command ini!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("§cUsage: /rate <player>");
                return true;
            }

            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer tidak online!");
                return true;
            }

            // Open rating GUI
            openRatingGUI(player, target);
            return true;

        } else if (command.getName().equalsIgnoreCase("rating")) {
            sender.sendMessage("§6Rating system coming soon!");
            return true;
        }

        return false;
    }

    private void openRatingGUI(Player rater, Player target) {
        org.bukkit.inventory.Inventory gui = org.bukkit.Bukkit.createInventory(null, 27,
                "§6§lRate: " + target.getName());

        // Add star items
        gui.setItem(11, createStar(1));
        gui.setItem(12, createStar(2));
        gui.setItem(13, createStar(3));
        gui.setItem(14, createStar(4));
        gui.setItem(15, createStar(5));

        // Add info item
        gui.setItem(22, createInfo(target));

        // Fill borders with glass
        org.bukkit.inventory.ItemStack glass = createGlass();
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }

        rater.openInventory(gui);
        rater.sendMessage("§aBuka rating menu untuk " + target.getName());
    }

    private org.bukkit.inventory.ItemStack createStar(int stars) {
        org.bukkit.Material material;
        String name;

        switch (stars) {
            case 1:
                material = org.bukkit.Material.YELLOW_DYE;
                name = "§e★☆☆☆☆";
                break;
            case 2:
                material = org.bukkit.Material.ORANGE_DYE;
                name = "§6★★☆☆☆";
                break;
            case 3:
                material = org.bukkit.Material.MAGENTA_DYE;
                name = "§d★★★☆☆";
                break;
            case 4:
                material = org.bukkit.Material.LIGHT_BLUE_DYE;
                name = "§b★★★★☆";
                break;
            case 5:
                material = org.bukkit.Material.LIME_DYE;
                name = "§a★★★★★";
                break;
            default:
                material = org.bukkit.Material.YELLOW_DYE;
                name = "§e★☆☆☆☆";
        }

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name + " §7(" + stars + " Stars)");
        meta.setLore(java.util.Arrays.asList(
                "§7Click untuk kasih " + stars + " bintang",
                "§7ke player"
        ));
        item.setItemMeta(meta);

        return item;
    }

    private org.bukkit.inventory.ItemStack createInfo(Player target) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bInfo Player");
        meta.setLore(java.util.Arrays.asList(
                "§7Player: §f" + target.getName(),
                "§7Klik bintang untuk rating",
                "",
                "§e1★ = Kurang",
                "§62★ = Cukup",
                "§d3★ = Bagus",
                "§b4★ = Sangat Bagus",
                "§a5★ = Luar Biasa"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private org.bukkit.inventory.ItemStack createGlass() {
        org.bukkit.inventory.ItemStack glass = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }
}