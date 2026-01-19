package dev.barzzy.RatePlayer.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    // Constructor dengan Material
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    // Constructor dengan ItemStack (opsional)
    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        meta.setLore(java.util.Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setCustomData(String key, String value) {
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("RatePlayer"), key),
                org.bukkit.persistence.PersistentDataType.STRING,
                value
        );
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}