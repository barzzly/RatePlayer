package dev.barzzy.RatePlayer.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Cek jika inventory yang diklik adalah rating GUI
        String title = event.getView().getTitle();
        if (title.startsWith("§6§lRate: ")) {
            // Cancel event agar item tidak bisa diambil
            event.setCancelled(true);

            // Cek jika yang klik adalah player
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            // Cek jika item yang diklik valid
            if (event.getCurrentItem() == null) return;
            if (!event.getCurrentItem().hasItemMeta()) return;

            // Ambil nama item
            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

            // Cek jika item adalah bintang
            if (itemName.contains("★")) {
                // Ambil rating dari nama item (1-5)
                int stars = 0;
                if (itemName.contains("1")) stars = 1;
                else if (itemName.contains("2")) stars = 2;
                else if (itemName.contains("3")) stars = 3;
                else if (itemName.contains("4")) stars = 4;
                else if (itemName.contains("5")) stars = 5;

                if (stars > 0) {
                    // Ambil nama target dari judul GUI
                    String targetName = title.substring(9); // Hapus "§6§lRate: "
                    Player target = Bukkit.getPlayer(targetName);

                    if (target != null) {
                        // Berikan rating
                        player.sendMessage("§aKamu memberikan " + stars + "★ kepada " + target.getName());
                        if (target.isOnline()) {
                            target.sendMessage("§6" + player.getName() + " §amemberikan kamu " + stars + "★");
                        }

                        // Broadcast ke server
                        Bukkit.broadcastMessage(
                                "§6" + player.getName() + " §7memberi §6" + target.getName() + " §7rating §e" + stars + "★"
                        );

                        // Tutup GUI
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cPlayer sudah offline!");
                        player.closeInventory();
                    }
                }
            }
        }
    }
}