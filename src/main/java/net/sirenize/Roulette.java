package net.sirenize;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Roulette extends JavaPlugin implements Listener {

    private static Roulette instance;
    private boolean smpStarted = false;
    private final Map<UUID, Integer> respawnChances = new HashMap<>();
    private final Map<UUID, Boolean> actionBarShown = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Register command executors
        this.getCommand("smpstart").setExecutor(new CommandHandler());
        this.getCommand("withdraw").setExecutor(new CommandHandler());

        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new InventoryEvents(), this);

        Items.createSpawnDust();
        Items.createRespawnBeacon();
        Items.registerCrafts();

        // Load configuration
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        smpStarted = getConfig().getBoolean("smpStarted", false);

        // Load respawn chances from config
        loadRespawnChances();

        // Schedule task to update action bar every second (20 ticks = 1 second)
        getServer().getScheduler().runTaskTimer(this, this::updateGlobalActionBar, 0L, 20L);

        getLogger().info("Roulette plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;
        // Save configuration
        getConfig().set("smpStarted", smpStarted);
        saveConfig();

        // Save respawn chances to config
        saveRespawnChances();

        getLogger().info("Roulette plugin has been disabled!");
    }

    public static Roulette getInstance() {
        return instance;
    }

    public boolean isSmpStarted() {
        return smpStarted;
    }

    public void setSmpStarted(boolean smpStarted) {
        this.smpStarted = smpStarted;
        getConfig().set("smpStarted", smpStarted);
        saveConfig();
    }

    public Map<UUID, Integer> getRespawnChances() {
        return respawnChances;
    }

    public void setRespawnChance(UUID playerId, int chance) {
        int limitedChance = Math.min(chance, 100);
        respawnChances.put(playerId, limitedChance);
        saveRespawnChance(playerId, limitedChance);
        updateActionBarForPlayer(Bukkit.getPlayer(playerId));  // Update action bar immediately
    }

    public void decreaseRespawnChance(UUID playerId, int amount) {
        if (respawnChances.containsKey(playerId)) {
            int currentChance = respawnChances.get(playerId);
            int newChance = Math.max(currentChance - amount, 0);
            respawnChances.put(playerId, newChance);
            saveRespawnChance(playerId, newChance);
            updateActionBarForPlayer(Bukkit.getPlayer(playerId));  // Update action bar immediately
        }
    }

    public void increaseRespawnChance(UUID playerId, int amount) {
        if (respawnChances.containsKey(playerId)) {
            int currentChance = respawnChances.get(playerId);
            int newChance = Math.min(currentChance + amount, 100);
            respawnChances.put(playerId, newChance);
            saveRespawnChance(playerId, newChance);
            updateActionBarForPlayer(Bukkit.getPlayer(playerId));  // Update action bar immediately
        }
    }

    private void loadRespawnChances() {
        ConfigurationSection section = getConfig().getConfigurationSection("respawnChances");
        if (section != null) {
            section.getKeys(false).forEach(uuidStr -> {
                UUID playerId = UUID.fromString(uuidStr);
                int chance = getConfig().getInt("respawnChances." + uuidStr);
                respawnChances.put(playerId, chance);
            });
        } else {
            getLogger().warning("Configuration section 'respawnChances' not found or is empty.");
        }
    }

    private void saveRespawnChances() {
        getConfig().set("respawnChances", null); // Clear old data
        respawnChances.forEach((playerId, chance) -> getConfig().set("respawnChances." + playerId.toString(), chance));
        saveConfig();
    }

    private void saveRespawnChance(UUID playerId, int chance) {
        getConfig().set("respawnChances." + playerId.toString(), chance);
        saveConfig();
    }

    public void banPlayer(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    setRespawnChance(playerId, 90);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "Unlucky! You gambled and you lost!", null, null);
                    player.kickPlayer("Unlucky! You gambled and you lost!");
                }
            }.runTaskLater(this, 1L);
        }
    }

    private void updateGlobalActionBar() {
        if (isSmpStarted()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateActionBarForPlayer(player);
            }
        }
    }

    public void updateActionBarForPlayer(Player player) {
        if (player != null && isSmpStarted()) {
            int chance = respawnChances.getOrDefault(player.getUniqueId(), 0);
            String message = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + chance + "%";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }

    public boolean hasActionBarShown(Player player) {
        return actionBarShown.getOrDefault(player.getUniqueId(), false);
    }

    public void setActionBarShown(Player player, boolean shown) {
        actionBarShown.put(player.getUniqueId(), shown);
    }
}
