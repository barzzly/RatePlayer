package dev.barzzy.RatePlayer.commands;

import dev.barzzy.RatePlayer.Main;
import dev.barzzy.RatePlayer.managers.RatingManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RateCommand implements CommandExecutor {
    
    private final Main plugin;
    private final RatingManager ratingManager;
    
    public RateCommand(Main plugin, RatingManager ratingManager) {
        this.plugin = plugin;
        this.ratingManager = ratingManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (command.getName().equalsIgnoreCase("rate")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cHanya player yang bisa menggunakan command ini!");
                return true;
            }
            
            if (!player.hasPermission("rateplayer.use")) {
                player.sendMessage("§cTidak ada permission!");
                return true;
            }
            
            if (args.length == 0) {
                player.sendMessage("§cUsage: /rate <player>");
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer tidak online!");
                return true;
            }
            
            ratingManager.openRatingMenu(player, target);
            player.sendMessage("§aMembuka rating menu untuk " + target.getName());
            
        } else if (command.getName().equalsIgnoreCase("rating")) {
            Player target;
            
            if (args.length > 0) {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("§cPlayer tidak ditemukan!");
                    return true;
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cUsage: /rating <player>");
                    return true;
                }
                target = (Player) sender;
            }
            
            String ratingDisplay = ratingManager.getRatingDisplay(target.getUniqueId());
            sender.sendMessage("§6" + target.getName() + "'s Rating: " + ratingDisplay);
        }
        
        return true;
    }
}