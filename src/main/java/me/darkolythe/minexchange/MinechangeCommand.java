package me.darkolythe.minexchange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MinechangeCommand implements CommandExecutor {
	
	private MineXChange main = MineXChange.getInstance();
	public MainMenu menu = new MainMenu(main);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(MineXChange.prefix + ChatColor.RED + "The console cannot open the MineXChange inventory!");
			return true;
		}

		Player player = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("minexchange")) { //if the player has permission, and the command is right
			main.configmanager.writedebug((player.getName() + " uses command: mxc ")); //DEBUG DEBUG DEBUG
			for (String arg : args) {
				main.configmanager.writedebug((arg + " ")); //DEBUG DEBUG DEBUG
			}
			if (player.hasPermission("minexchange.command")) {
				if (args.length == 0) {
					player.openInventory(main.menu.createInventory(player));
				} else {


					if (args[0].equalsIgnoreCase("cooldown")) { //if there's more than one argument, and it's cooldown
						if (args.length == 4) {
							if (player.hasPermission("minexchange.setcooldown")) { //if the command executor has permission
								
								
								if (args[1].equalsIgnoreCase("set")) {
									for (Player plr : Bukkit.getServer().getOnlinePlayers()) { //check if the player is online
										if (plr.getName().equalsIgnoreCase(args[2])) {
											int setval = main.formatNum(args[3], player);
											if (setval >= 0) { //the remaining time is equal to the system's time minus the system's time minus an amount in milliseconds
												main.setCooldown(plr.getUniqueId(), setval);
												sender.sendMessage(MineXChange.prefix + ChatColor.GREEN + plr.getName() + "'s request cooldown set to " + MineXChange.intToDHM(setval));
											} else {
												sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid amount. Must be greater than 0.");
											}
											return true;
										}
									}
									sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid player. " + args[2] + " isn't online.");
								} else {
									sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange cooldown set <player> <time in minutes>");
								}
							} else {
								sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You don't have permission to do that!");
							}
							
							
						} else if (args.length == 3 || args.length == 2) {
							if (args[1].equalsIgnoreCase("get")) {
								if (args.length == 3) {
									for (Player plr : Bukkit.getServer().getOnlinePlayers()) { //check if the player is online
										if (plr.getName().equalsIgnoreCase(args[2])) {
											if (plr.getName().equals(sender.getName()) || (!plr.getName().equals(sender.getName()) && player.hasPermission("minexchange.override"))) {
												sender.sendMessage(MineXChange.prefix + ChatColor.GREEN + plr.getName() + "'s remaining cooldown: " + MineXChange.intToDHM(main.getCooldown(plr.getUniqueId())));
												return true;
											} else {
												sender.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "You do not have permission to check other players' cooldowns.");
												return true;
											}
										}
									}
									sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid player. " + args[2] + " isn't online.");
								} else {
									sender.sendMessage(MineXChange.prefix + ChatColor.GREEN + sender.getName() + "'s remaining cooldown: " + MineXChange.intToDHM(main.getCooldown(player.getUniqueId())));
								}
							} else {
								sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange cooldown get/set <player>");
							}
						} else {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange cooldown get/set <player>");
						}
						
						
						
					} else if (args[0].equalsIgnoreCase("request")) {
						if (player.hasPermission("minexchange.add")) {
							if (main.getCooldown(player.getUniqueId()) == 0) {
								if (args.length == 5) {
									if (stringCanMaterial(args[1])) {
										if (stringCanInteger(args[2])) {
											if (Integer.parseInt(args[2]) <= main.maxrequestamount) {
												if (stringCanMaterial(args[3])) {
													if (stringCanInteger(args[4])) {
														Material plrmat = Material.getMaterial(args[3].toUpperCase());
														ItemStack plritem = new ItemStack(plrmat, 1);
														if (main.createrequestmenu.removeFromInventory(player.getInventory(), plritem, Integer.parseInt(args[4]))) { //the arguments create a new Reward instance
															player.closeInventory();
															player.updateInventory();
															ItemStack reqstack = new ItemStack(Material.getMaterial(args[1].toUpperCase()), 1);
															Request req = new Request(reqstack, Integer.parseInt(args[2]), Integer.parseInt(args[2]), plritem, Integer.parseInt(args[4]), Integer.parseInt(args[4]), player.getName(), player.getUniqueId(), null, 0);
															for (int i = 0; i < main.requestlist.length; i++) {
																if (main.requestlist[i] == null) {
																	main.requestlist[i] = req;
																	sender.sendMessage(MineXChange.prefix + ChatColor.GREEN + "Request for " + req.getItemStack().getItemMeta().getDisplayName() + ChatColor.GREEN + " successfully made!");
																	main.setCooldown(player.getUniqueId(), main.cooldownamount);
																	return true;
																}
															}
															sender.sendMessage(MineXChange.prefix + ChatColor.RED + "There are currently too many active exchanges.");  //if there are no empty spots in requestlist
														} else {
															sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You do not have the specified amount of items to give as a reward in your inventory");
														}
													} else {
														sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Reward Item Amount");
													}
												} else {
													sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Reward Item Type");
												}
											} else {
												sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Maximum request amount is " + Integer.toString(main.maxrequestamount));
											}
										} else {
											sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Request Item Amount");
										}
									} else {
										sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Request Item Type");
									}
								} else {
									sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange request <request item> <amount> <reward item> <amount>");
								}
							} else {
								sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You must wait before making another request!\nTime left: " + MineXChange.intToDHM(main.getCooldown(player.getUniqueId())));
							}
						} else {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You don't have permission to do that!");
						}
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (player.hasPermission("minexchange.reload")) {
							main.getConfigs();
							player.sendMessage(MineXChange.prefix + ChatColor.GREEN + "Files have been reloaded");
						} else {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You don't have permission to do that!");
						}
					} else if (args[0].equalsIgnoreCase("togglenotify")) {
						main.setNotify(player.getUniqueId(), !main.getNotify(player.getUniqueId())); //this will switch the player's current notify state
						if (main.getNotify(player.getUniqueId())) {
							sender.sendMessage(MineXChange.prefix + ChatColor.GREEN.toString() + "MineXChange notifications will now appear every so often");
						} else {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED.toString() + "MineXChange notifications will no longer appear");
						}
					} else if (args[0].equalsIgnoreCase("token")) {
						if (player.hasPermission("minexchange.token")) {
							player.getWorld().dropItem(player.getLocation(), main.menu.getToken());
						} else {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You don't have permission to do that!");
						}
					} else if (args[0].equalsIgnoreCase("open")) {
						if (player.hasPermission("minexchange.openothers")) {
							if (args.length == 2) {
								for (Player plr : Bukkit.getServer().getOnlinePlayers()) { //check if the player is online
									if (plr.getName().equalsIgnoreCase(args[1])) {
										main.configmanager.writedebug(player.getDisplayName() + " opens " + plr.getName() + "'s requests inventory");
										player.openInventory(main.exchangeinventory.createRequestsInventory(plr, 1));
										return true;
									}
								}
								sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid player. " + args[1] + " isn't online.");
							} else {
								sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange open playername");
							}
						}
					} else {
						if (player.hasPermission("minexchange.override")) {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange request/togglenotify/cooldown/reload/token");
						} else {
							sender.sendMessage(MineXChange.prefix + ChatColor.RED + "Invalid Arguments: /minexchange request/togglenotify");
						}
					}
				}
			} else {
				sender.sendMessage(MineXChange.prefix + ChatColor.RED + "You don't have permission to do that!");
			}
		}
		return true;
	}
	
	public Boolean stringCanMaterial(String str) {
		str = str.toUpperCase();
		Material mat = Material.getMaterial(str);
		if (mat != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean stringCanInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
}