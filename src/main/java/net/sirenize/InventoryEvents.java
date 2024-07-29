package net.sirenize;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryEvents implements Listener {

    private void consumeItem(Player player, ItemStack item) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.getInventory().remove(itemInHand);
        }
        player.updateInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String title = event.getView().getTitle();
        // Bukkit.getLogger().info("Inventory Title: " + title); // Debug

        if (title.equals("Revive a Player")) {
            event.setCancelled(true);
            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
                String playerName = skullMeta.getDisplayName().substring(4); // Assumes color code is 4 chars
                //Bukkit.getLogger().info("Opening confirmation for: " + playerName); // Debug
                player.openInventory(UnbanInventory.getConfirmationInventory(playerName));
            }
        } else if (title.equals("Confirm")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            //Bukkit.getLogger().info("Clicked slot: " + slot); // Debug

            if (slot < 4 && clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
                String playerName = event.getInventory().getItem(4).getItemMeta().getDisplayName().split(" ")[3];
                Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
                Bukkit.broadcastMessage("§c§l" + playerName + " has been revived!");

                consumeItem(player, clickedItem);

                player.closeInventory();
            } else if (slot > 4 && clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                player.closeInventory();
            }
        }
    }
}