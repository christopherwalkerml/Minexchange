package me.darkolythe.minexchange;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class MainMenu implements Listener {

	/*
	 * The mainmenu class is in control of the requests, and buttons to create a
	 * request
	 */

	public MineXChange main; // initialize the main variable as Type main class

	public MainMenu(MineXChange plugin) {
		this.main = plugin; // set it equal to an instance of main
	}

	public CreateRequestMenu createrequestmenu = new CreateRequestMenu(main);

	public Inventory createInventory(Player player) {

		Inventory menuinv = Bukkit.getServer().createInventory(null, 54, ChatColor.RED.toString() + ChatColor.BOLD.toString() + "MineXChange");

		for (int i = 0; i < main.requestlist.length; i++) {
			if (main.requestlist[i] != null) {
				if (main.requestlist[i].getTimeLeft() == 0) {
					while (main.requestlist[i].getRewardLeft() > 0) {
						ItemStack returnitem = main.requestlist[i].getRewardStack(1);
						returnitem.setAmount((int)MineXChange.clamp((long) main.requestlist[i].getRewardLeft(), 1, 64));
						main.addRequestsInventory(main.requestlist[i].getPlayerID(), returnitem);
						main.requestlist[i].setRewardLeft((int)(main.requestlist[i].getRewardLeft() - MineXChange.clamp((long) main.requestlist[i].getRewardLeft(), 1, 64)));
					}
					for (Player plr : Bukkit.getServer().getOnlinePlayers()) {
						if (plr.getUniqueId() == main.requestlist[i].getPlayerID()) {
							plr.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "Your request timed out. Your remaining reward was placed in your Exchange Requests Inventory.");
						}
					}
				}
			}
		}

		main.configmanager.saveRequestList();
		cleanMainMenu();

		for (int i = 0; i < main.requestlist.length; i++) {
			if (main.requestlist[i] != null) {
				if (main.requestlist[i].getRequestLeft() != 0) {
					menuinv.setItem(i, main.requestlist[i].getItemStack());
				}
			}
		}

		ItemStack menustack = new ItemStack(Material.SUNFLOWER, 1);
		ItemMeta menumeta = menustack.getItemMeta();
		menumeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Create Request"); // create the create request itemstack
		menumeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		menustack.setItemMeta(menumeta);
		menustack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		menuinv.setItem(47, menustack);

		menustack = new ItemStack(Material.PLAYER_HEAD, 1);
		menumeta = menustack.getItemMeta();
		SkullMeta skullmeta = (SkullMeta) menumeta;
		skullmeta.setOwningPlayer(player);
		skullmeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD + "Xchange Inventory");
		skullmeta.setLore(Arrays.asList(ChatColor.BLUE.toString() + "Player: " + player.getName(),
										ChatColor.YELLOW.toString() + "Exchanges aided: " + main.getStats(player.getUniqueId())[0],
										ChatColor.YELLOW + "Items contributed: " + main.getStats(player.getUniqueId())[1]));
		menustack.setItemMeta(skullmeta);
		
		menuinv.setItem(51, menustack);

		return menuinv;

	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory().getType() == InventoryType.CHEST) {
			if (event.getInventorySlots().size() > 1) {
				if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "MineXChange")) { //if its the right inventory
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); // if the right inventory is open
		System.out.println("MXC - debug - 114");
		if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "MineXChange")) {
			System.out.println("MXC - debug - 116");
			if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
				System.out.println("MXC - debug - 118");
				if (event.getClickedInventory() != null) {
					System.out.println("MXC - debug - 120");
					if (player.getItemOnCursor().getType() == Material.AIR) { // if the player clicks "create" request with nothing on their cursor
						System.out.println("MXC - debug - 122");
						ItemStack clickstack = event.getCurrentItem();
						if (clickstack != null && clickstack.getType() != Material.AIR) {
							System.out.println("MXC - debug - 125");
							if (clickstack.getItemMeta().hasDisplayName()) {
								System.out.println("MXC - debug - 127");
								if (clickstack.getType() == Material.SUNFLOWER) {
									System.out.println("MXC - debug - 129");
									if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Create Request")) {
										System.out.println("MXC - debug - 131");
										if (player.hasPermission("minexchange.add")) {
											System.out.println("MXC - debug - 133");
											if (main.getCooldown(player.getUniqueId()) == 0 || player.hasPermission("minexchange.nodelay")) {
												System.out.println("MXC - debug - 135");
												event.setCancelled(true); // create the requestmenu inventory, and make the player open it
												player.openInventory(createrequestmenu.createInventory(main.maxrequestamount));
											} else {
												player.sendMessage(MineXChange.prefix + ChatColor.RED + "You must wait before making another request!\nTime left: " + MineXChange.intToDHM(main.getCooldown(player.getUniqueId())));
												player.closeInventory(); // if they try opening it before their cooldown is done, it will close the menu and tell them to wait
											}
										} else {
											player.sendMessage(MineXChange.prefix + ChatColor.RED + "You don't have permission to do that!");
										}
									}
								} else if (clickstack.getType() == Material.PLAYER_HEAD && event.getRawSlot() == 51) {
									if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "Xchange Inventory")) {
										player.openInventory(main.exchangeinventory.createRequestsInventory(player, 1));
									}
								} else {
									if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "MineXChange")) { //if its the right clicked inventory
										if (event.getSlot() >= 0 && event.getSlot() <= 44) {
											if (main.requestlist[event.getSlot()] != null) {
												String reqplayer = ChatColor.BLUE.toString() + "Player: " + main.requestlist[event.getSlot()].getPlayerName();
												String clickplayer = clickstack.getItemMeta().getLore().get(1);
												if (reqplayer.equalsIgnoreCase(clickplayer)) {
													player.openInventory(main.requestlist[event.getRawSlot()].createInventory());
													MineXChange.setRequest(player, main.requestlist[event.getRawSlot()]);
												} else {
													player.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "This request has already been completed!");
												}
											} else {
												player.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "This request has already been completed!");
											}
										}
									}
								}
							}
						}
					}
				}
			}
			event.setCancelled(true);
		}
	}

	private void cleanMainMenu() {
		for (int i = 0; i < main.requestlist.length; i++) {
			if (main.requestlist[i] == null || main.requestlist[i].getRequestLeft() == 0 || main.requestlist[i].getTimeLeft() == 0) {
				for (int j = i; j < main.requestlist.length - i; j++) {
					if (j > i) {
						main.requestlist[j - 1] = main.requestlist[j];
					}
				}
			}
		}
	}

	@EventHandler
	public void onNPCInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("minexchange.interact")) {
			if (event.getRightClicked().getCustomName() != null && event.getRightClicked().getCustomName().equals(ChatColor.BLUE.toString() + "MineXChange")) {
				player.openInventory(createInventory(event.getPlayer()));
			}
		} else {
			player.sendMessage(MineXChange.prefix + ChatColor.RED + "You do not have permission to use that");
		}
	}

	ItemStack getToken() {
		ItemStack token = new ItemStack(Material.NAME_TAG);
		ItemMeta meta = token.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE.toString() + "MineXChange");
		token.setItemMeta(meta);
		return token;
	}
}
