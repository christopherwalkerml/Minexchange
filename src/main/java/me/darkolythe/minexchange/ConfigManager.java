package me.darkolythe.minexchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class ConfigManager {

	public MineXChange main; //initialize the main variable as Type main class
	public ConfigManager(MineXChange plugin) {
		this.main = plugin; //set it equal to an instance of main
	}
	
	private MineXChange plugin = MineXChange.getPlugin(MineXChange.class);
	
	public FileConfiguration requestlistcfg;
	public File requestlist;
	public FileConfiguration playerdatacfg;
	public File playerdata;
	public Path debugdata = Paths.get("./plugins/MineXChange/mxcdebug.txt");
	
	public void setup() {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}
		
		requestlist = new File(plugin.getDataFolder(), "RequestList.yml");
		playerdata = new File(plugin.getDataFolder(), "PlayerData.yml");
		
		if (!requestlist.exists()) {
			try {
				requestlist.createNewFile();
				System.out.println(MineXChange.prefix + ChatColor.GREEN + "RequestList.yml has been created");
			} catch (IOException e) {
				System.out.println(MineXChange.prefix + ChatColor.RED + "Could not create RequestList.yml");
			}
		}
		requestlistcfg = YamlConfiguration.loadConfiguration(requestlist);
		
		if (!playerdata.exists()) {
			try {
				playerdata.createNewFile();
				System.out.println(MineXChange.prefix + ChatColor.GREEN + "PlayerData.yml has been created");
			} catch (IOException e) {
				System.out.println(MineXChange.prefix + ChatColor.RED + "Could not create PlayerData.yml");
			}
		}
		playerdatacfg = YamlConfiguration.loadConfiguration(playerdata);

		try {
			Files.write(debugdata, Arrays.asList(""), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println(main.prefix + "Cant clear MXC debug");
			System.out.println(debugdata);
		}
	}
	

	
	public void savePlayerData(UUID uuid) {
		try {
			if (!playerdatacfg.contains("player." + uuid)) {
				playerdatacfg.createSection("player." + uuid); //if the request doesnt exist in the config, create it
			}
			String section = "player." + uuid;
			
			playerdatacfg.set(section, null);
			
			int[] ints = main.getStats(uuid);
			playerdatacfg.set(section + ".reqs", ints[0]);
			playerdatacfg.set(section + ".items", ints[1]);
			playerdatacfg.set(section + ".cooldown", main.getRawCooldown(uuid));
			playerdatacfg.set(section + ".notify", main.getNotify(uuid));
			playerdatacfg.set(section + ".contributions", main.getExchangeContributions(uuid));

			int num = 0;
			for (ItemStack itemstack : main.getRequestsInventory(uuid)) { //save the player's mt inventory
				if (itemstack != null) {
					playerdatacfg.createSection(section + ".requestsinv" + "." + num++).set(".itemstack", itemstack);
				} else {
					ItemStack airstack = new ItemStack(Material.AIR, 0);
					playerdatacfg.createSection(section + ".requestsinv" + "." + num++).set(".itemstack" + num++, airstack);
				}
			}
			
			num = 0;
			for (ItemStack itemstack : main.getRewardsInventory(uuid)) { //save the player's mt inventory
				if (itemstack != null) {
					playerdatacfg.createSection(section + ".rewardsinv" + "." + num++).set(".itemstack", itemstack);
				} else {
					ItemStack airstack = new ItemStack(Material.AIR, 0);
					playerdatacfg.createSection(section + ".rewardsinv" + "." + num++).set(".itemstack", airstack);
				}
			}
			
			playerdatacfg.save(playerdata);
		} catch (IOException e) {
			System.out.println(MineXChange.prefix + ChatColor.RED + "Could not save PlayerData.yml");
		}
	}

	public void writedebug(String str) {
		try {
			Files.write(debugdata, Arrays.asList(str), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println(main.prefix + "Cant save MXC debug");
		}
	}

	public void saveRequestList() {
		try {
			if (main.requestlist != null) {
				for (int i = 0; i < main.requestlist.length; i++) {
					if (main.requestlist[i] != null) {
						if (!requestlistcfg.contains("requests." + main.requestlist[i].getRequestID())) {
							requestlistcfg.createSection("requests." + main.requestlist[i].getRequestID()); //if the request doesnt exist in the config, create it
						}
						
						String section = "requests." + main.requestlist[i].getRequestID();
						Request request = main.requestlist[i];
						
						if (request.getRequestLeft() == 0 || request.getRewardLeft() == 0) {
							requestlistcfg.set(section, null);
						} else {
							requestlistcfg.set(section + ".live", 1);
							requestlistcfg.set(section + ".requestitem", request.getRawItemStack());
							requestlistcfg.set(section + ".requestamount", request.getRequestAmount());
							requestlistcfg.set(section + ".requestleft", request.getRequestLeft());
							requestlistcfg.set(section + ".rewarditem", request.getRewardStack(1));
							requestlistcfg.set(section + ".rewardamount", request.getRewardAmount());
							requestlistcfg.set(section + ".rewardleft", request.getRewardLeft());
							requestlistcfg.set(section + ".playerid", request.getPlayerID().toString());
							requestlistcfg.set(section + ".playername", request.getPlayerName().toString());
							requestlistcfg.set(section + ".createtime", request.getCreateTime());
						}
					}
				}
				requestlistcfg.save(requestlist);
			}
		} catch (IOException e) {
			System.out.println(MineXChange.prefix + ChatColor.RED + "Could not save RequestList.yml");
		}
	}

	
	
	public void loadPlayerData(UUID uuid) {
		String section = "player." + uuid;
		if (playerdatacfg.contains(section)) {
			int[] ints = {playerdatacfg.getInt(section + ".reqs"), playerdatacfg.getInt(section + ".items")};
			main.setStats(uuid, ints);
			long num = Long.parseLong(playerdatacfg.getString((section + ".cooldown"))); //turn the player's cooldown start time into a remaining cooldown amount by clamping the subtracted current time
			int cd = (int) MineXChange.clamp(num - System.currentTimeMillis(), 0, -1);
			main.setCooldown(uuid, cd);
			
			if (playerdatacfg.contains(section + ".notify")) {
				main.setNotify(uuid, playerdatacfg.getBoolean(section + ".notify"));
			} else {
				main.setNotify(uuid, true);
			}
			
			main.clearExchangeContributions(uuid);
			for (String contributions : playerdatacfg.getStringList(section + ".contributions")) {
				main.addExchangeContributions(uuid, contributions);
			}
			
			if (playerdatacfg.contains(section + ".requestsinv")) {
				main.clearRequestsInventory(uuid);
				for (String item : playerdatacfg.getConfigurationSection(section + ".requestsinv").getKeys(false)) { //load all the itemstacks from config.yml
					if (item != null) {
						if (playerdatacfg.getConfigurationSection(section + ".requestsinv." + item) != null) {
							main.addRequestsInventory(uuid, new ItemStack((playerdatacfg.getConfigurationSection(section + ".requestsinv." + item).getItemStack("itemstack"))));
						}
					}
				}
			}
			if (playerdatacfg.contains(section + ".rewardsinv")) {
				main.clearRewardsInventory(uuid);
				for (String item : playerdatacfg.getConfigurationSection(section + ".rewardsinv").getKeys(false)) { //load all the itemstacks from config.yml
					if (playerdatacfg.getConfigurationSection(section + ".rewardsinv." + item) != null) {
						main.addRewardsInventory(uuid, new ItemStack((playerdatacfg.getConfigurationSection(section + ".rewardsinv." + item).getItemStack("itemstack"))));
					}
				}
			}
		} else {
			savePlayerData(uuid);
		}
	}
	
	public void loadRequestList() {
		String section = "requests";
		if (requestlistcfg.contains(section)) {
			for (String item : requestlistcfg.getConfigurationSection(section).getKeys(false)) { //load all the itemstacks from config.yml
				if (item != null) {
					if (requestlistcfg.getInt(section + "." + item + ".live") == 1) {
						for (int i = 0; i < main.requestlist.length; i++) {
							if (main.requestlist[i] == null) { //find the first empty spot in the inventory
								ItemStack requestitem = new ItemStack((requestlistcfg.getConfigurationSection(section + "." + item).getItemStack("requestitem"))); //get all the data needed to make a Request instance
								int requestamount = requestlistcfg.getInt(section + "." + item + ".requestamount");
								int requestleft = requestlistcfg.getInt(section + "." + item + ".requestleft");
								ItemStack rewarditem = new ItemStack((requestlistcfg.getConfigurationSection(section + "." + item).getItemStack("rewarditem")));
								int rewardamount = requestlistcfg.getInt(section + "." + item + ".rewardamount");
								int rewardleft = requestlistcfg.getInt(section + "." + item + ".rewardleft");
								String playername = requestlistcfg.getString(section + "." + item + ".playername");
								UUID playerid = UUID.fromString(requestlistcfg.getString(section + "." + item + ".playerid"));
								long createtime = requestlistcfg.getLong(section + "." + item + ".createtime");

								main.requestlist[i] = new Request(requestitem, requestamount, requestleft, rewarditem, rewardamount, rewardleft, playername, playerid, item, createtime);
								break;
							}
						}
					}
					MineXChange.requestids.add(item);
				}
			}
		}
	}



	public void reloadAll(Player player) {
		requestlistcfg = YamlConfiguration.loadConfiguration(requestlist);
		playerdatacfg = YamlConfiguration.loadConfiguration(playerdata);
		if (player != null) {
			player.sendMessage(MineXChange.prefix + ChatColor.GREEN + "Files have been reloaded");
		}
	}

}
