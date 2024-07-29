package net.sirenize;

import org.bukkit.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import net.kyori.adventure.text.Component;

import java.util.Iterator;

public class UnbanInventory {

    public static Inventory getBannedPlayersInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "Revive a Player");
        Iterator var1 = Bukkit.getBanList(BanList.Type.NAME).getBanEntries().iterator();

        while(var1.hasNext()) {
            BanEntry ban = (BanEntry)var1.next();
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta)head.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(ban.getTarget()));
            meta.setDisplayName("§6§l" + ban.getTarget());
            head.setItemMeta(meta);
            inv.addItem(new ItemStack[]{head});
        }

        return inv;
    }

    public static Inventory getConfirmationInventory(String playerName) {
        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST, Component.text("Confirm"));

        ItemStack yes = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemStack question = new ItemStack(Material.BOOK, 1);

        ItemMeta yesMeta = yes.getItemMeta();
        if (yesMeta != null) {
            yesMeta.displayName(Component.text("§aYes"));
            yes.setItemMeta(yesMeta);
        }

        ItemMeta noMeta = no.getItemMeta();
        if (noMeta != null) {
            noMeta.displayName(Component.text("§cNo"));
            no.setItemMeta(noMeta);
        }

        ItemMeta questionMeta = question.getItemMeta();
        if (questionMeta != null) {
            questionMeta.displayName(Component.text("§eThis will revive " + playerName));
            question.setItemMeta(questionMeta);
        }

        // Fill slots with yes (0-3), question (4), no (5-8)
        for (int i = 0; i < 4; i++) {
            inv.setItem(i, yes);
        }
        inv.setItem(4, question);
        for (int i = 5; i < 9; i++) {
            inv.setItem(i, no);
        }

        return inv;
    }
}
