package net.sirenize;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class Items {

    public static void registerCrafts() {
        // Registering the spawn dust recipe
        ItemStack spawnDust = createSpawnDust();
        ShapedRecipe spawnDustRecipe = new ShapedRecipe(new NamespacedKey(Roulette.getInstance(), "spawn_dust"), spawnDust);
        spawnDustRecipe.shape("XYX", "YZY", "XYX");
        spawnDustRecipe.setIngredient('X', Material.DIAMOND);
        spawnDustRecipe.setIngredient('Y', Material.DIAMOND_BLOCK);
        spawnDustRecipe.setIngredient('Z', Material.TOTEM_OF_UNDYING);
        Bukkit.getServer().addRecipe(spawnDustRecipe);

        // Registering the respawn beacon recipe
        ItemStack respawnBeacon = createRespawnBeacon();
        ShapedRecipe respawnBeaconRecipe = new ShapedRecipe(new NamespacedKey(Roulette.getInstance(), "respawn_beacon"), respawnBeacon);
        respawnBeaconRecipe.shape("XYX", "ZWZ", "XYX");
        respawnBeaconRecipe.setIngredient('X', Material.DIAMOND_BLOCK);
        respawnBeaconRecipe.setIngredient('Y', new RecipeChoice.ExactChoice(spawnDust));
        respawnBeaconRecipe.setIngredient('Z', Material.NETHERITE_INGOT);
        respawnBeaconRecipe.setIngredient('W', Material.BEACON);
        Bukkit.getServer().addRecipe(respawnBeaconRecipe);
    }

    public static ItemStack createSpawnDust() {
        ItemStack spawndust = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = spawndust.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Spawndust");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Adds chance when used!"));
            spawndust.setItemMeta(meta);
        }
        return spawndust;
    }

    public static ItemStack createRespawnBeacon() {
        ItemStack respawnBeacon = new ItemStack(Material.BEACON);
        ItemMeta meta = respawnBeacon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Respawn Beacon");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Respawns a player!"));
            respawnBeacon.setItemMeta(meta);
        }
        return respawnBeacon;
    }
}