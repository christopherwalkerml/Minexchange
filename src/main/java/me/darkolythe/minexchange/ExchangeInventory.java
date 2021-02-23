package me.darkolythe.minexchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class ExchangeInventory implements Listener {
	
	public MineXChange main; //initialize the main variable as Type main class
	public ExchangeInventory(MineXChange plugin) {
		this.main = plugin; //set it equal to an instance of main
	}
	
	Boolean canClean = true;
	
	public Inventory createRequestsInventory(Player player, int page) {

		main.configmanager.writedebug(player.getDisplayName() + " opens requests inventory");
		
		Inventory exchangeinv = Bukkit.getServer().createInventory(null, 54, ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests");
		
		ItemStack invstack = new ItemStack(Material.CHEST, 1);
		ItemMeta invmeta = invstack.getItemMeta();
		invmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Rewards Inventory");
		invstack.setItemMeta(invmeta);
		exchangeinv.setItem(51, invstack);
		
		invstack = new ItemStack(Material.ENDER_CHEST, 1);
		invmeta = invstack.getItemMeta();
		invmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Requests Inventory");
		invmeta.setLore(Arrays.asList(ChatColor.GREEN.toString() + "Currently Open", ChatColor.GRAY + "Player: " + player.getName()));
		invstack.setItemMeta(invmeta);
		exchangeinv.setItem(47, invstack);
		
		if (main.getRequestsInventory(player.getUniqueId()).size() > (45 * page)) {
			invstack = new ItemStack(Material.ARROW, 1);
			invmeta = invstack.getItemMeta();
			invmeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Next Page");
			invmeta.setLore(Arrays.asList(ChatColor.GREEN.toString() + "Current page: " + page));
			invstack.setItemMeta(invmeta);
			exchangeinv.setItem(53, invstack);
		}
		
		createBottomRow(exchangeinv, page);

		createStacks(main.requestsinventory, exchangeinv, player, 45 * (page - 1));
		
		cleanInventory(main.requestsinventory, player);
		
		return exchangeinv;
	}
	
	public Inventory createRewardsInventory(Player player, int page) {

		main.configmanager.writedebug(player.getDisplayName() + " opens rewards inventory");
		
		Inventory exchangeinv = Bukkit.getServer().createInventory(null, 54, ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Rewards");
		
		ItemStack invstack = new ItemStack(Material.ENDER_CHEST, 1);
		ItemMeta invmeta = invstack.getItemMeta();
		invmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Rewards Inventory");
		invmeta.setLore(Arrays.asList(ChatColor.GREEN.toString() + "Currently Open", ChatColor.GRAY + "Player: " + player.getName()));
		invstack.setItemMeta(invmeta);
		exchangeinv.setItem(51, invstack);
		
		invstack = new ItemStack(Material.CHEST, 1);
		invmeta = invstack.getItemMeta();
		invmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Requests Inventory");
		invstack.setItemMeta(invmeta);
		exchangeinv.setItem(47, invstack);
		
		if (main.getRewardsInventory(player.getUniqueId()).size() > (45 * page)) {
			invstack = new ItemStack(Material.ARROW, 1);
			invmeta = invstack.getItemMeta();
			invmeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Next Page");
			invmeta.setLore(Arrays.asList(ChatColor.GREEN.toString() + "Current page: " + page));
			invstack.setItemMeta(invmeta);
			exchangeinv.setItem(53, invstack);
		}

		createBottomRow(exchangeinv, page);
		
		createStacks(main.rewardsinventory, exchangeinv, player, 45 * (page - 1));
		
		cleanInventory(main.rewardsinventory, player);
		
		return exchangeinv;
	}
	
	
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getType() == InventoryType.CHEST) {
			if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests") ||
					event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Rewards")) {
				Player player = getPlayerOpen(event.getInventory());

				int page = 1;
				if (event.getInventory().getItem(53) != null) {
					page = Integer.parseInt(event.getInventory().getItem(53).getItemMeta().getLore().get(0).replaceAll("[^\\d]", ""));
				} else if (event.getInventory().getItem(45) != null) {
					page = Integer.parseInt(event.getInventory().getItem(45).getItemMeta().getLore().get(0).replaceAll("[^\\d]", ""));
				}

				if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests")) {
					for (int i = 0; i < MineXChange.clamp(main.requestsinventory.get(player.getUniqueId()).size() - ((page - 1) * 45), 0, 45); i++) {
						if (event.getInventory().getItem(i) != null) {
							main.requestsinventory.get(player.getUniqueId()).set(i + ((page - 1) * 45), event.getInventory().getItem(i)); //loop through the requests list and save items
						} else {
							main.requestsinventory.get(player.getUniqueId()).set(i + ((page - 1) * 45), new ItemStack(Material.AIR, 1)); //it will never have to loop more than the list because items cannot be added
						}
					}
				} else if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Rewards")) {
					for (int i = 0; i < MineXChange.clamp(main.rewardsinventory.get(player.getUniqueId()).size() - ((page - 1) * 45), 0, 45); i++) { //loop through the rewards list and save items
						if (event.getInventory().getItem(i) != null) {
							main.rewardsinventory.get(player.getUniqueId()).set(i + ((page - 1) * 45), event.getInventory().getItem(i));
						} else {
							main.rewardsinventory.get(player.getUniqueId()).set(i + ((page - 1) * 45), new ItemStack(Material.AIR, 1));
						}
					}
				}
				if (canClean) {
					cleanInventory(main.requestsinventory, player);
					cleanInventory(main.rewardsinventory, player);
				} else {
					canClean = true;
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory().getType() == InventoryType.CHEST) {
			if (event.getInventorySlots().size() > 1 && (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests") ||
					event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Rewards"))) { //don't allow the user to drag an item more than one slot. not allowing drag at all, makes placing items in the inventory difficult
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		
		if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
			if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests") ||
					event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Rewards")) { //if its the right inventory
				if (event.getClickedInventory() != player.getInventory()) {
					Player openplr = getPlayerOpen(event.getClickedInventory());
					if (player.getItemOnCursor().getType() == Material.AIR) {
						if (event.getRawSlot() >= 45 && event.getRawSlot() <= 53) {
							ItemStack clickstack = event.getCurrentItem();
							event.setCancelled(true);

							if (clickstack != null) {
								if (clickstack.getType() == Material.CHEST) {
									if (clickstack.getItemMeta().hasDisplayName()) {
										if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Requests Inventory")) {
											player.openInventory(createRequestsInventory(openplr, 1));
										} else if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Rewards Inventory")) {
											player.openInventory(createRewardsInventory(openplr, 1));
										}
									}
								} else if (clickstack.getType() == Material.REDSTONE_BLOCK) {
									if (clickstack.getItemMeta().hasDisplayName()) {
										if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Back to Menu")) {
											player.openInventory(main.menu.createInventory(player));
										}
									}
								} else if (clickstack.getType() == Material.ARROW) {
									canClean = false;
									int page = Integer.parseInt(clickstack.getItemMeta().getLore().get(0).replaceAll("[^\\d]", ""));
									if (clickstack.getItemMeta().hasDisplayName()) {
										if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Next Page")) {
											if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests")) {
												player.openInventory(createRequestsInventory(openplr, page + 1));
											} else {
												player.openInventory(createRewardsInventory(openplr, page + 1));
											}
										} else if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Previous Page")) {
											if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests")) {
												player.openInventory(createRequestsInventory(openplr, page - 1));
											} else {
												player.openInventory(createRewardsInventory(openplr, page - 1));
											}
										}
									}
								}
							}
						}
					} else if (event.getCurrentItem() == null || player.getItemOnCursor().getType() != event.getCurrentItem().getType()) {
						event.setCancelled(true);
					}
				} else {
					if (event.isShiftClick()) {
						event.setCancelled(true);
					}
				}
			}
		} else {
			if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
				if (event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Requests") ||
						event.getView().getTitle().equals(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Exchange Inventory - Rewards")) { //if its the right inventory
					event.setCancelled(true);
				}
			}
		}
	}
	
	
	
	public void cleanInventory(HashMap<UUID, ArrayList<ItemStack>> inv, Player player) {
		int index = 0;
		if (inv.get(player.getUniqueId()) != null) {
			while (index < inv.get(player.getUniqueId()).size()) {
				if (inv.get(player.getUniqueId()).get(index).getType() == Material.AIR || inv.get(player.getUniqueId()).get(index).getAmount() == 0) {
					inv.get(player.getUniqueId()).remove(index);
					index = 0;
				} else {
					index += 1;
				}
			}
		}
	}
	
	
	
	public void createStacks(HashMap<UUID, ArrayList<ItemStack>> hashinv, Inventory inv, Player player, int startindex) {
		if (hashinv.get(player.getUniqueId()) != null) {
			for (int i = 0; i < MineXChange.clamp(hashinv.get(player.getUniqueId()).size() - startindex, 0, 45); i++) {
				inv.setItem(i, hashinv.get(player.getUniqueId()).get(i + startindex));
			}
		}
	}
	
	
	
	public void createBottomRow(Inventory inv, int page) {
		
		ItemStack invstack = new ItemStack(Material.REDSTONE_BLOCK, 1);
		ItemMeta invmeta = invstack.getItemMeta();
		invmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Back to Menu");
		invstack.setItemMeta(invmeta);
		inv.setItem(49, invstack);
		
		if (page > 1) {
			invstack = new ItemStack(Material.ARROW, 1);
			invmeta = invstack.getItemMeta();
			invmeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Previous Page");
			invmeta.setLore(Arrays.asList(ChatColor.GREEN.toString() + "Current page: " + page));
			invstack.setItemMeta(invmeta);
			inv.setItem(45, invstack);
		}
	}


	public Player getPlayerOpen(Inventory inv) {
		int index;
		if (inv.getItem(47).getType() == Material.ENDER_CHEST) {
			index = 47;
		} else {
			index = 51;
		}
		String playername = inv.getItem(index).getItemMeta().getLore().get(1).replace(ChatColor.GRAY + "Player: ", "");
		for (Player plr: Bukkit.getOnlinePlayers()) {
			if (playername.equals(plr.getName())) {
				return plr;
			}
		}
		return null;
	}
}
