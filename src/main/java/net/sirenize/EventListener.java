package net.sirenize;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;
import java.util.UUID;

public class EventListener implements Listener {

    private final Roulette plugin;

    public EventListener() {
        this.plugin = Roulette.getInstance();

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!plugin.isSmpStarted()) {
            if (!plugin.hasActionBarShown(player)) {
                plugin.updateActionBarForPlayer(player);
                plugin.setActionBarShown(player, true);
            }
            return;
        }

        if (!plugin.getRespawnChances().containsKey(playerId)) {
            Random random = new Random();
            int respawnChance = random.nextInt(11) * 5 + 50; // Random number divisible by 5 between 50 and 100
            plugin.setRespawnChance(playerId, respawnChance);
        }

        plugin.updateActionBarForPlayer(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isSmpStarted()) return;

        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Decrease respawn chance


        // Check if player should be banned
        int respawnChance = plugin.getRespawnChances().getOrDefault(playerId, 0);
        Random random = new Random();
        int roll = random.nextInt(99) + 1;

        if (roll > respawnChance) {
            plugin.banPlayer(playerId);
        } else {
            plugin.decreaseRespawnChance(playerId, 5);
            Player killer = player.getKiller();
            if (killer != null) {
                UUID killerId = killer.getUniqueId();
                int killerChance = plugin.getRespawnChances().getOrDefault(killerId, 0);

                if (killerChance < 100) {
                    plugin.increaseRespawnChance(killerId, 5);
                } else {
                    killer.getWorld().dropItemNaturally(killer.getLocation(), Items.createSpawnDust());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isSmpStarted()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the item is the respawn beacon (adjust this condition based on your item identification logic)

        if (item != null && item.getType() == Material.BEACON && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            // Assuming the item has a specific display name or lore to identify it as the respawn beacon
            if (meta != null && meta.getDisplayName().equals(ChatColor.GOLD + "Respawn Beacon")) {
                if (meta.getLore() != null && meta.getLore().contains(ChatColor.GRAY + "Respawns a player!")) {
                    event.setCancelled(true); // Cancel the event to prevent default interaction

                    // Open the banned players inventory or any specific inventory
                    player.openInventory(UnbanInventory.getBannedPlayersInventory());
                }
            }
        }

        else if (item != null && item.getType() == Material.PRISMARINE_CRYSTALS && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getDisplayName().equals(ChatColor.GOLD + "Spawndust")) {
                if (meta.getLore() != null && meta.getLore().contains(ChatColor.GRAY + "Adds chance when used!")) {
                    event.setCancelled(true);

                    if (plugin.getRespawnChances().getOrDefault(player.getUniqueId(), 0) < 100) {
                        plugin.increaseRespawnChance(player.getUniqueId(), 5);
                        item.setAmount(item.getAmount() - 1);
                    }
                }
            }
        }
    }
}