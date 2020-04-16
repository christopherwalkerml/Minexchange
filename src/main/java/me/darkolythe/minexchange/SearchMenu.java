package me.darkolythe.minexchange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SearchMenu implements Listener {

    public MineXChange main;
    public SearchMenu(MineXChange plugin) {
        this.main = plugin;
    }

    private Inventory createMenu(ArrayList<Material> mats) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "MXC Search For Item");

        for (Material mat : mats) {
            inv.addItem(new ItemStack(mat));
        }
        return inv;
    }

    private ArrayList<Material> searchMaterials(String str) {
        ArrayList<Material> mats = new ArrayList<>();
        boolean hit = false;
        for (Material mat : Material.values()) {
            if (mat.toString().contains(str.toUpperCase())) {
                mats.add(mat);
                hit = true;
            }
        }
        if (!hit) {
            for (Material mat : Material.values()) {
                float hits = 0;
                float count = 0;
                for (int i = 0; i < mat.toString().length(); i++) {
                    if (i < str.length()) {
                        if (mat.toString().toCharArray()[i] == str.toUpperCase().toCharArray()[i]) {
                            hits++;
                        }
                        count++;
                    }
                }
                if (hits / count >= 0.6) {
                    mats.add(mat);
                }
            }
        }
        return mats;
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != null) {
            if (event.getView().getTitle().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "MXC Search For Item")) { //if its the right clicked inventory

                ItemStack item = event.getCurrentItem();

                Inventory inv = MineXChange.storedCreateInventory.get(player);
                if (inv != null && item != null && item.getType() != Material.AIR) {
                    String type = MineXChange.textList.get(player);

                    if (type.equals("desired")) {
                        inv.setItem(13, item);
                    } else {
                        inv.setItem(31, item);
                    }

                    player.openInventory(inv);

                    MineXChange.storedCreateInventory.remove(player);
                    MineXChange.textList.remove(player);
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "MXC Search For Item") && MineXChange.storedCreateInventory.containsKey(player)) {

            Inventory inv = MineXChange.storedCreateInventory.get(player);
            MineXChange.storedCreateInventory.remove(player);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                @Override
                public void run() {
                    player.openInventory(inv);
                }
            }, 1L);

            MineXChange.textList.remove(player);
        }
    }

    @EventHandler
    private void onAsyncChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        if (MineXChange.textList.containsKey(player)) {
            String input = event.getMessage();
            MineXChange.bufferText.put(player, input);
            event.setCancelled(true);
        }
    }

    public void useText() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
            @Override
            public void run() {
                for (Player player : MineXChange.bufferText.keySet()) {
                    String input = MineXChange.bufferText.get(player);
                    if (!input.equals("cancel")) {
                        ArrayList<Material> list = searchMaterials(input);
                        player.openInventory(createMenu(list));
                    } else {
                        Inventory inv = MineXChange.storedCreateInventory.get(player);
                        player.openInventory(inv);

                        MineXChange.storedCreateInventory.remove(player);
                        MineXChange.textList.remove(player);
                    }
                    MineXChange.bufferText.remove(player);
                }
            }
        }, 1L, 5L);
    }
}
