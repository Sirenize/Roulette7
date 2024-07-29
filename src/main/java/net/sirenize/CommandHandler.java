package net.sirenize;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("smpstart")) {
            return handleSmpStart(sender);
            //} else if (command.getName().equalsIgnoreCase("setrespawnchance")) {
            //    return handleSetRespawnChance(sender, args);
            //} else if (command.getName().equalsIgnoreCase("debug")) {
            //   return handleDebug(sender);
        } else if (command.getName().equalsIgnoreCase("withdraw")) {
            return handleWithdraw(sender, args);
        }
        return false;
    }

    private boolean handleSmpStart(CommandSender sender) {
        if (!sender.hasPermission("roulette.smpstart")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Roulette plugin = Roulette.getInstance();
        if (plugin.isSmpStarted()) {
            sender.sendMessage(ChatColor.RED + "SMP has already started.");
            return true;
        }

        plugin.setSmpStarted(true);

        // Assign initial respawn chances to all players online
        Random random = new Random();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            if (!plugin.getRespawnChances().containsKey(playerId)) {
                int respawnChance = random.nextInt(11) * 5 + 50;  // Random number divisible by 5 between 50 and 100
                plugin.setRespawnChance(playerId, respawnChance);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "SMP has started!");
        return true;
    }

    private boolean handleSetRespawnChance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("roulette.setrespawnchance")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setrespawnchance <player> <chance>");
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        int chance;
        try {
            chance = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number format.");
            return true;
        }

        Roulette plugin = Roulette.getInstance();
        plugin.setRespawnChance(target.getUniqueId(), chance);

        sender.sendMessage(ChatColor.GREEN + "Set respawn chance of " + playerName + " to " + chance + "%.");
        return true;
    }

    private boolean handleDebug(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(Items.createSpawnDust());
        player.getInventory().addItem(Items.createRespawnBeacon());

        player.sendMessage(ChatColor.GREEN + "debug !!");
        return true;
    }

    private boolean handleWithdraw(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /withdraw <amount>");
            return true;
        }

        Player player = (Player) sender;
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format.");
            return true;
        }

        Roulette plugin = Roulette.getInstance();
        UUID playerId = player.getUniqueId();
        int currentChance = plugin.getRespawnChances().getOrDefault(playerId, 0);
        int totalChanceToWithdraw = amount * 5;

        if (totalChanceToWithdraw > currentChance) {
            player.sendMessage(ChatColor.RED + "Not enough chance!");
            return true;
        }

        plugin.decreaseRespawnChance(playerId, totalChanceToWithdraw);

        ItemStack spawnDust = Items.createSpawnDust();
        spawnDust.setAmount(amount);
        player.getInventory().addItem(spawnDust);

        player.sendMessage(ChatColor.GREEN + "Withdrawn!");
        return true;
    }
}