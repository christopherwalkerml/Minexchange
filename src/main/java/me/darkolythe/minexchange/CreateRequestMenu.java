package me.darkolythe.minexchange;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CreateRequestMenu implements Listener {
	
	/*
	 * The requestmenu class is in control of getting the player's item and reward
	 */
	
	public MineXChange main; //initialize the main variable as Type main class
	public CreateRequestMenu(MineXChange plugin) {
		this.main = plugin; //set it equal to an instance of main
	}
	public Request request;
	
	private int middle1 = 13; //inventory slots that the middle objects are in
	private int middle2 = 31;
	
	public Inventory createInventory(int maxrequestamount) {
		
		
		Inventory crinv = Bukkit.getServer().createInventory(null, 54, ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Create A Request");
		
		
		ItemStack reqstack = new ItemStack(Material.BEDROCK, 1);
		ItemMeta reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Desired Item Here"); //Placeholder item
		reqmeta.setLore(Arrays.asList(ChatColor.GRAY + "or click with empty cursor to search for item"));
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle1, reqstack);
		
		createIncDecRow(crinv, middle1, maxrequestamount, 0);
		
		reqstack = new ItemStack(Material.BEDROCK, 1);
		reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Reward Item Here"); //Placeholder item
		reqmeta.setLore(Arrays.asList(ChatColor.GRAY + "or click with empty cursor to search for item"));
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle2, reqstack);
		
		createIncDecRow(crinv, middle2, maxrequestamount, 0);
		
		reqstack = new ItemStack(Material.BARRIER, 1);
		reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Cancel Request"); //middle items that can be replaced
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(47, reqstack);
		
		reqstack = new ItemStack(Material.EMERALD_BLOCK, 1);
		reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Create Request");
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(51, reqstack);

		
		return crinv;
		
		
	}
	
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getView().getTopInventory() != null) {
			if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Create A Request")) { //if its the right inventory
				for (int i = 0; i < 54; i++) {
					if (event.getRawSlots().contains(i)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		/*
		 * This method deals with the interaction of the request menu.
		 */
		if (!event.getClick().equals(ClickType.DOUBLE_CLICK)) {
			Inventory crinv = event.getClickedInventory();
			Player player = (Player) event.getWhoClicked();
			if (event.getClickedInventory() != null && event.getClickedInventory() != player.getInventory()) {
				if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Create A Request")) { //if its the right clicked inventory
					if (event.isShiftClick()) {
						event.setCancelled(true);
						return;
					}
					ItemStack clickstack = event.getCurrentItem();
			
					if (clickstack != null && clickstack.getType() != Material.AIR) {

						ItemStack miditem = new ItemStack(Material.BEDROCK, 1);
						int middle = 0;
						if (event.getSlot() <=  (middle1 + 4) && event.getSlot() >= (middle1 - 4)) { //if the player interacted with the first row (the requesting item row), make that the target item
							miditem = crinv.getItem(middle1);
							middle = middle1;
						} else if (event.getSlot() <= (middle2 + 4) && event.getSlot() >= (middle2 - 4)) { //if its the second row, the reward row, make that the target item
							miditem = crinv.getItem(middle2);
							middle = middle2;
						}

						/*
						 * Cancel Request
						 */
						if (player.getItemOnCursor().getType() == Material.AIR) { //if the player has nothing on their cursor
							if (clickstack.getType() == Material.BARRIER) {
								if (clickstack.getItemMeta().hasDisplayName()) {
									if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Cancel Request")) {
										event.setCancelled(true);
										player.openInventory(main.menu.createInventory(player));
									}
								}
								/*
								 * Create Request
								 */
							} else if (clickstack.getType() == Material.EMERALD_BLOCK) {
								if (clickstack.getItemMeta().hasDisplayName()) {
									if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Create Request")) {
										event.setCancelled(true);
										ItemStack rewarditem = crinv.getItem(middle2); //get the reward item that the player chose
										ItemStack requestitem = crinv.getItem(middle1);
										ItemMeta rewardmeta = rewarditem.getItemMeta();
										ItemMeta requestmeta = requestitem.getItemMeta();

										boolean validitem = true; //this variable makes it so the method can't continue if valid options weren't entered

										/*
										 * Invalid Request Item
										 */
										if (requestmeta.hasDisplayName()) {
											if (requestmeta.getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Desired Item Here")) {
												player.closeInventory();
												player.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Request Item");
												validitem = false;
											}
										}
										/*
										 * Invalid Reward Item
										 */
										if (rewardmeta.hasDisplayName()) {
											if (rewardmeta.getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Reward Item Here")) {
												player.closeInventory();
												player.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Reward Item");
												validitem = false;
											}
										}
										/*
										 * Checking the player has all the reward items
										 */
										if (validitem) { //continue if valid options were entered
											int requestnum = Integer.parseInt(crinv.getItem(middle1 - 1).getItemMeta().getLore().get(0).replace(ChatColor.GRAY.toString(), "").replaceAll("[^\\d]", ""));
											int rewardnum = Integer.parseInt(crinv.getItem(middle2 - 1).getItemMeta().getLore().get(0).replace(ChatColor.GRAY.toString(), "").replaceAll("[^\\d]", ""));
											for (int i = 0; i < main.requestlist.length; i++) {
												if (main.requestlist[i] == null) {
													if (removeFromInventory(player.getInventory(), rewarditem, rewardnum)) {
														main.configmanager.writedebug(player.getDisplayName() + " creates request: " + requestnum + " " + requestitem.getType()); //DEBUG DEBUG DEBUG
														main.configmanager.writedebug(player.getDisplayName() + " creates reward: " + rewardnum + " " + rewarditem.getType()); //DEBUG DEBUG DEBUG
														Request req = new Request(requestitem, requestnum, requestnum, rewarditem, rewardnum, rewardnum, player.getName(), player.getUniqueId(), null, 0);
														main.requestlist[i] = req;
														main.setCooldown(player.getUniqueId(), main.cooldownamount);
														player.updateInventory();
														main.configmanager.saveRequestList();
														player.openInventory(main.menu.createInventory(player));
														return;
													} else {
														player.sendMessage(MineXChange.prefix + ChatColor.RED + "You do not have the specified amount of items to give as a reward in your inventory");
														return;
													}
												}
											}
											player.sendMessage(MineXChange.prefix + ChatColor.RED + "There are currently too many active exchanges."); //if there are no empty spots in requestlist
										}
									}
								}
								/*
								 * If player increases or decreases amount
								 */
							} else if (miditem.getType() != Material.BEDROCK && (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE)) { //if an item has been set to request
								String clickname = clickstack.getItemMeta().getDisplayName();
								char namechar = clickname.charAt(4);
								int namenum = Integer.parseInt(clickname.replaceAll("[^\\d]", "")); //remove non numbers
								int amt = Integer.parseInt(event.getCurrentItem().getItemMeta().getLore().get(0).replace(ChatColor.GRAY.toString(), "").replaceAll("[^\\d]", ""));
								if (namechar == 'I') {
									amt = (int)main.clamp(amt + namenum, 1, main.maxrequestamount);
									event.setCancelled(true);
								} else if (namechar == 'D') {
									amt = (int)main.clamp(amt - namenum, 1, main.maxrequestamount);
									event.setCancelled(true);
								}
								if (event.getRawSlot() >= middle1 - 4 && event.getRawSlot() <= middle1 + 4) {
									createIncDecRow(crinv, middle1, main.maxrequestamount, amt);
								} else {
									createIncDecRow(crinv, middle2, main.maxrequestamount, amt);
								}
							} else {
								/*
								 * If player removes items from reward / request with empty hand
								 */
								if (event.getRawSlot() == middle1) {
									if (crinv.getItem(middle1).getType() == Material.BEDROCK) {
										MineXChange.textList.put(player, "desired");
										MineXChange.storedCreateInventory.put(player, event.getInventory());
										player.closeInventory();
										player.sendMessage(MineXChange.prefix + "Enter item to search for. Type 'cancel' to return.");
									} else {
										ItemStack reqstack = new ItemStack(Material.BEDROCK, 1);
										ItemMeta reqmeta = reqstack.getItemMeta();
										reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Desired Item Here"); //Placeholder item
										reqmeta.setLore(Arrays.asList(ChatColor.GRAY + "or click with empty cursor to search for item"));
										reqstack.setItemMeta(reqmeta);
										crinv.setItem(middle1, reqstack);
										createIncDecRow(crinv, middle1, main.maxrequestamount, 0);
									}
								} else if (event.getRawSlot() == middle2) {
									if (crinv.getItem(middle2).getType() == Material.BEDROCK) {
										MineXChange.textList.put(player, "reward");
										MineXChange.storedCreateInventory.put(player, event.getInventory());
										player.closeInventory();
										player.sendMessage(MineXChange.prefix + "Enter item to search for. Type 'cancel' to return.");
									} else {
										ItemStack reqstack = new ItemStack(Material.BEDROCK, 1);
										ItemMeta reqmeta = reqstack.getItemMeta();
										reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Reward Item Here"); //Placeholder item
										reqmeta.setLore(Arrays.asList(ChatColor.GRAY + "or click with empty cursor to search for item"));
										reqstack.setItemMeta(reqmeta);
										crinv.setItem(middle2, reqstack);
										createIncDecRow(crinv, middle2, main.maxrequestamount, 0);
									}
								}
							}

							/*
							 * If player clicks bedrock placeholder with something in their hand, it will be placed in that spot
							 */
						} else {
							if (clickstack.getType() == Material.BEDROCK) {
								if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Desired Item Here") || clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Reward Item Here")) {
									ItemStack handstack = player.getItemOnCursor().clone(); //copy the item into the middle slot
									handstack.setAmount(1);
									crinv.setItem(middle, handstack); //set the item to the item with it's changed name
									event.setCancelled(true);
									if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Drag Desired Item Here")) {
										createIncDecRow(crinv, middle1, main.maxrequestamount, 1);
									} else {
										createIncDecRow(crinv, middle2, main.maxrequestamount, 1);
									}
								}
							}
						}
					}
					event.setCancelled(true);
				}
			} else if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
				if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Create A Request")) { //if its the right clicked inventory
					if (event.isShiftClick()) {
						event.setCancelled(true);
					}
				}
			}
		} else {
			if (event.getClickedInventory() != null) {
				if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Create A Request")) { //if its the right clicked inventory
					event.setCancelled(true);
				}
			}
		}
	}

	public void createIncDecRow(Inventory crinv, int middle, int maxrequestamount, int itemamount) {
		/*
		 * This creates the increase and decrease row
		 */

		List<String> lore = Arrays.asList(ChatColor.GRAY + "Current amount: " + itemamount, ChatColor.YELLOW + "Maximum is " + maxrequestamount);
		
		ItemStack reqstack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		ItemMeta reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Increase By 1"); //Increase by 1 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle - 1, reqstack);
		
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Increase By 10"); //Increase by 10 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle - 2, reqstack);
		
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Increase By 100"); //Increase by 100 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle - 3, reqstack);
		
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Increase By 1000"); //Increase by 1000 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle - 4, reqstack);
		
		
		reqstack = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Decrease By 1"); //Decrease by 1 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle + 1, reqstack);
		
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Decrease By 10"); //Decrease by 10 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle + 2, reqstack);
		
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Decrease By 100"); //Decrease by 100 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle + 3, reqstack);
		
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Decrease By 1000"); //Decrease by 1000 stack
		reqmeta.setLore(lore);
		reqstack.setItemMeta(reqmeta);
		crinv.setItem(middle + 4, reqstack);
	}
	
	public Boolean removeFromInventory(Inventory plri, ItemStack itemstack, int amount) {
		/*
		 * This method loops through the inventory and removes the amount of itemstacks equal to amount. If it cannot find enough, it returns false, and removes nothing
		 */
		ItemStack[] plrinv = plri.getContents();
		ItemMeta itemmeta = itemstack.getItemMeta();
		int plrnum = 0;
		for (int x = 0; x < plrinv.length; x++) { //loop through their inv to check if the required amount is there
			ItemStack i = plrinv[x];
			if (i != null) {
				ItemStack iclone = i.clone();
				ItemMeta imeta = iclone.getItemMeta();
				imeta.setDisplayName(itemmeta.getDisplayName());
				iclone.setAmount(1);
				iclone.setItemMeta(imeta);
				if (iclone.equals(itemstack)) {
					if (i.getAmount() == amount) { //if the player has the exact amount of items, delete the itemstack, and end the loop
						plri.setItem(x, new ItemStack(Material.AIR, 0));
						return true;
					} else if (i.getAmount() > amount) { //if the player has more than needed, set the itemstack to whatever should be left
						i.setAmount(i.getAmount() - amount);
						return true;
					} else {
						plrnum += i.getAmount(); //if the reward amount is greater than 1 stack, keep looking until reward amount is met
						if (plrnum >= amount) { //if the reward amount is met, this will remove the reward amount from the inv
							for (int y = 0; y < plrinv.length; y++) {
								ItemStack s = plrinv[y];
								if (s != null) {
									ItemStack sclone = s.clone();
									ItemMeta smeta = sclone.getItemMeta();
									smeta.setDisplayName(itemmeta.getDisplayName());
									sclone.setAmount(1);
									sclone.setItemMeta(imeta);
									if (sclone.equals(itemstack)) {
										if (s.getAmount() == amount) {
											plri.setItem(y, new ItemStack(Material.AIR, 0));
											return true;
										} else if (s.getAmount() > amount) {
											s.setAmount(s.getAmount() - amount);
											return true;
										} else { //if the stack found is less than the required amount, remove it, and decrease the amount left
											amount -= s.getAmount();
											plri.setItem(y, new ItemStack(Material.AIR, 0));
										}
									}
								}
							}
							return false;
						}
					}
				}
			}
		}
		return false;
	}
	
}
