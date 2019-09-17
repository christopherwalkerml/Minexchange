package me.darkolythe.minexchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RequestInventory implements Listener {
	
	public MineXChange main; //initialize the main variable as Type main class
	public RequestInventory(MineXChange plugin) {
		this.main = plugin; //set it equal to an instance of main
	}

	private Map<UUID, Integer> confirmInv = new HashMap<>();
	private Map<Player, Boolean> openGUI = new HashMap<>();
	private Boolean closeInv = false;
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
	    if (event.getInventory().getType() == InventoryType.CHEST) {
            if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Request Inventory")) {
                ItemStack item49 = event.getInventory().getItem(49);
                Player player = (Player) event.getPlayer();
                giveBackItems(player, item49, event.getInventory());
            }
        }
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.getInventory().getType() == InventoryType.CHEST) {
            if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Request Inventory")) { //if its the right inventory
                if (event.getInventory() != player.getInventory()) {
                    if (event.getInventorySlots().size() > 1 && event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Request Inventory")) { //don't allow the user to drag an item more than one slot. not allowing drag at all, makes placing items in the inventory difficult
                        event.setCancelled(true);
                    }
                }
            }
        }
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		
		if (event.getClickedInventory() != null && (event.getClickedInventory().getType() == InventoryType.CHEST || event.getClickedInventory().getType() == InventoryType.PLAYER)) {
			if (event.getView().getTitle().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Request Inventory")) { //if its the right inventory
				if (event.getClickedInventory() != player.getInventory()) {
					ItemStack clickstack = event.getCurrentItem();
					if (clickstack != null && clickstack.getType() != Material.AIR) { //if the user doesnt try to pick up air with their cursor
						Request request = main.getRequest(player);
						ItemStack item49 = event.getInventory().getItem(49);
						main.configmanager.writedebug(player.getDisplayName() + " rq ID: " + request.getRequestID()); //DEBUG DEBUG DEBUG

						if (clickstack.getType() == Material.BARRIER) {
							if (clickstack.getItemMeta().hasDisplayName()) {
								if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Delete Exchange")) {
									if (player.hasPermission("minexchange.override") || player.getUniqueId() == request.getPlayerID()) {
										main.configmanager.writedebug(player.getDisplayName() + " deletes exchange"); //DEBUG DEBUG DEBUG
										event.setCancelled(true);
										int returnnum = request.getRewardLeft();
										while (returnnum > 0) {
											main.configmanager.writedebug(player.getDisplayName() + " gets back " + request.getRewardStack(1).getType() + " " + returnnum); //DEBUG DEBUG DEBUG
											main.addRewardsInventory(request.getPlayerID(), request.getRewardStack((int) MineXChange.clamp(returnnum, 1, 64)));
											returnnum -= MineXChange.clamp(returnnum, 1, 64);
										}
										request.setRequestLeft(0); //this makes the request unreachable by setting the remaining value to 0
										main.configmanager.savePlayerData(request.getPlayerID());
										player.openInventory(main.menu.createInventory(player));
									}
								}
							}
						} else if (clickstack.getType() == Material.EMERALD_BLOCK) {
							if (clickstack.getItemMeta().hasDisplayName()) {
								if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Confirm Exchange")) {
									main.configmanager.writedebug(player.getDisplayName() + " confirms exchange"); //DEBUG DEBUG DEBUG
									if (request.getRequestLeft() == 0) {
										main.configmanager.writedebug(player.getDisplayName() + " exchange was already completed"); //DEBUG DEBUG DEBUG
										player.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "This request has already been completed!");
										event.setCancelled(true);
										closeInv = true;
										return;
									}
									int itemcount = 0;
									for (int i = 0; i < 45; i++) {
										if (event.getInventory().getItem(i) != null) {
											ItemStack clone = event.getInventory().getItem(i).clone();
											clone.setAmount(1);
											if (clone.equals(request.getRawItemStack())) {
												itemcount += event.getInventory().getItem(i).getAmount(); //if the exchange is confirmed, this will loop through the request inventory, and sum all items given
											}
										}
									}

									confirmRequest(request, player, itemcount, item49);

									event.setCancelled(true);
									closeInv = true;
									player.closeInventory();
								}
							}
						} else if (clickstack.getType() == Material.REDSTONE_BLOCK) {
							if (clickstack.getItemMeta().hasDisplayName()) {
								if (clickstack.getItemMeta().getDisplayName().equals(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Cancel Exchange")) {
									main.configmanager.writedebug(player.getDisplayName() + " cancels exchange"); //DEBUG DEBUG DEBUG
									giveBackItems(player, item49, event.getInventory()); //if the exchange is cancelled, the player will get their items back

									event.setCancelled(true);
									closeInv = true;
									player.openInventory(main.menu.createInventory(player));
								}
							}
						} else if (event.getRawSlot() < 48 || event.getRawSlot() > 50) {
							return;
						}
					} else {
						if (player.getItemOnCursor().getType() != Material.AIR) {
							if (event.getRawSlot() < 45) {
								return;
							}
						}
					}
					event.setCancelled(true);
				} else {
					if (event.getCurrentItem() != null) {
						ItemStack clickstack = event.getCurrentItem().clone();
						if (main.getRequest(player) != null) {
							Request req = main.getRequest(player); //if the player is trying to give themselves items
							if (req.getPlayerID().equals(player.getUniqueId())) {
								event.setCancelled(true);
								player.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "You cannot give yourself items!");

								main.configmanager.writedebug(player.getDisplayName() + " tries to give self items"); //DEBUG DEBUG DEBUG
							} else {
								for (int i = 0; i < 45; i++) { //if the player isnt the requester, continue
									if (event.getView().getTopInventory().getItem(i) == null || event.getView().getTopInventory().getItem(i).getType() == Material.AIR) { //check if there's a spot in the inventory
										if (clickstack.getType() != Material.AIR) { //check if clickstack isnt nothing
											ItemStack teststack = clickstack.clone();
											teststack.setAmount(1);
											ItemStack reqstack = req.getRawItemStack();
											if (!teststack.equals(reqstack)) { //make sure the player doesnt have anything on their cursor, or if they do, make sure the two can stack
												event.setCancelled(true);
												main.configmanager.writedebug(player.getDisplayName() + " tries to give wrong items"); //DEBUG DEBUG DEBUG
												return;
											} else if (i == 44) {
												return;
											}
										} else {
											return;
										}
									}
								}
							}
						}
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public void giveBackItems(Player player, ItemStack item49, Inventory inv) {
		if (!closeInv) {
			if (item49 != null) {
				int itemcount = 0;
				for (int i = 0; i < 45; i++) {
					if (inv.getItem(i) != null) {
						if (inv.getItem(i).getType() == item49.getType()) { //if the exchange is cancelled, all items in the request inventory will return
							itemcount += inv.getItem(i).getAmount();								   //to the player
						}
					}
				}
				main.configmanager.writedebug(player.getDisplayName() + " give back items " + item49.getType() + " " + itemcount); //DEBUG DEBUG DEBUG
				giveItems(player, itemcount);
			}
		} else {
			closeInv = false;
		}
	}

	private void confirmRequest(Request request, Player player, int itemcount, ItemStack item49) {
		if (item49 != null) {
			if (request != null) {
				int leftover = 0;
				if (request.getReward(itemcount) > 0) {
					if (request.getRewardDivide() > 1) {
						if (Math.abs((((int)request.getRewardDivide() - (request.getRequestLeft() - itemcount)) % (int)request.getRewardDivide())) > 0) {
							leftover = (int)((request.getRewardDivide() - (request.getRequestLeft() - itemcount) % request.getRewardDivide()));
						}
					}
				} else {
					leftover = itemcount;
				}

				main.configmanager.writedebug(player.getDisplayName() + " gives " + item49.getType() + " " + itemcount); //DEBUG DEBUG DEBUG
				main.configmanager.writedebug(player.getDisplayName() + " confirms request with " + leftover + " leftover and " + itemcount + " itemcount"); //DEBUG DEBUG DEBUG

				int overflow = request.getRemoveAmount(itemcount); //if the amount of items given is larger than the amount needed, the items will return to the player

				main.configmanager.writedebug(player.getDisplayName() + " " + overflow + " overflow"); //DEBUG DEBUG DEBUG

				if (overflow == 0) {
					if (!confirmInv.containsKey(player.getUniqueId()) || confirmInv.get(player.getUniqueId()) == null) {
						itemcount -= leftover;
					}
				}

				if ((itemcount - overflow) > 0) {
					int[] ints = {0, itemcount - overflow};
					main.addStats(player.getUniqueId(), ints); //add the item stat for the player
					ArrayList<String> contributions = main.getExchangeContributions(player.getUniqueId()); //get the player's contributions

					boolean canadd = false;
					if (contributions.size() == 0) { //if the contribution list is empty, it can't loop, and mustn't have any contributions. Therefore, it hasn't done this request yet
						canadd = true;
					} else if (!contributions.contains(request.getRequestID())) {
						canadd = true;
					}
					if (canadd) {
						ints[0] = 1;
						ints[1] = 0;
						main.addStats(player.getUniqueId(), ints); //add contribution stat for the player
						main.addExchangeContributions(player.getUniqueId(), request.getRequestID());
					}
				}

				int amount = itemcount - overflow;
				int itemnum = itemcount - overflow;

				ItemStack giveitem = request.getRawItemStack(); //give the requester their items
				while (itemnum > 0) {
					main.configmanager.writedebug(request.getPlayerName() + " gets " + itemnum + " items from their request");
					giveitem.setAmount((int) MineXChange.clamp(itemnum, 1, 64));
					main.addRequestsInventory(request.getPlayerID(), giveitem.clone());
					itemnum -= MineXChange.clamp(itemnum, 1, 64);
				}

				if (overflow != 0) {
					giveItems(player, overflow);
				} else {
					if (leftover > 0) {
						if (!confirmInv.containsKey(player.getUniqueId()) || confirmInv.get(player.getUniqueId()) == null) {
							player.sendMessage(main.prefix + ChatColor.GRAY + "You're giving " + leftover + " extra item(s) for no reward. Type 'yes' to give the extra items, or 'no' to keep them");
							confirmInv.put(player.getUniqueId(), leftover);
						}
					}
				}

				int rewardnum = request.getReward(amount); //this gets the amount of reward items the player gets from giving that many items
				int rewnum = request.getReward(amount);
				while (rewardnum > 0) {
					main.configmanager.writedebug(player.getDisplayName() + " gets " + rewardnum + " reward of " + request.getRawItemStack().getType()); //DEBUG DEBUG DEBUG
					main.addRewardsInventory(player.getUniqueId(), request.getRewardStack((int) MineXChange.clamp(rewardnum, 1, 64)));
					rewardnum -= MineXChange.clamp(rewardnum, 1, 64);
				}
				main.configmanager.savePlayerData(request.getPlayerID());
				main.configmanager.savePlayerData(player.getUniqueId());
				main.configmanager.writedebug(player.getDisplayName() + " request removes " + itemcount + " request amount and " + rewnum + " reward amount");
				request.removeAmount(itemcount, rewnum); //the amount can't be removed earlier because it will change the data of the itemstack, making it non-retrievable
			}
		}
	}

	public void giveItems(Player player, int itemcount) {
		while (itemcount > 0) {
			ItemStack returnitem = main.getRequest(player).getRawItemStack(); //this gets the reward stack, gives it the default name, then returns the extras
			returnitem.setAmount((int)MineXChange.clamp(itemcount, 1, returnitem.getMaxStackSize())); //to the player, if they gave too much
			if (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(returnitem);
			} else {
				main.configmanager.writedebug(player.getDisplayName() + " give items drop at feet"); //DEBUG DEBUG DEBUG
				World world = player.getWorld();
				world.dropItem(player.getLocation(), returnitem);
			}
			itemcount -= returnitem.getAmount();
		}
	}

	@EventHandler
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (confirmInv.containsKey(player.getUniqueId())) {
			if (confirmInv.get(player.getUniqueId()) != null) {
				int leftover = confirmInv.get(player.getUniqueId());
				if (event.getMessage().toLowerCase().equals("yes")) {
					main.configmanager.writedebug(player.getDisplayName() + " gave their extras"); //DEBUG DEBUG DEBUG
					player.sendMessage(MineXChange.prefix + ChatColor.GRAY + "You gave your extras");
					confirmRequest(main.getRequest(player), player, leftover, main.getRequest(player).getRawItemStack());
				} else {
					main.configmanager.writedebug(player.getDisplayName() + " kept their extras"); //DEBUG DEBUG DEBUG
					player.sendMessage(MineXChange.prefix + ChatColor.GRAY + "You kept your extras");
					Inventory inv = Bukkit.createInventory(null, 54);
					ItemStack item49 = main.getRequest(player).getRawItemStack();
					item49.setAmount(leftover);
					inv.setItem(0, item49);
					giveBackItems(player, item49, inv);
					event.setCancelled(true);
				}
				event.setCancelled(true);
				confirmInv.put(player.getUniqueId(), null);
			}
		}
	}

	public void checkConfirm() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			@Override
			public void run() {
				for (Player player : openGUI.keySet()) {
					if (openGUI.get(player)) {
						player.openInventory(main.menu.createInventory(player));
						openGUI.put(player, false);
					}
				}
			}
		},1, 5);
	}
}
