package me.darkolythe.minexchange;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class Request {

	private ItemStack requestitem;
	private int requestamount;
	private int requestleft;
	private ItemStack rewarditem;
	private int rewardamount;
	private int rewardleft;
	private float rewarddivide;
	private UUID playerid;
	private String playername;
	private String id;
	private long createtime;
	
	public Request(ItemStack requestitem, int requestamount, int requestleft, ItemStack rewarditem, int rewardamount, int rewardleft, String playername, UUID playerid, String ID, long createtime) {
		this.requestitem = requestitem;
		this.requestamount = requestamount;
		this.requestleft = requestleft;
		this.rewarditem = rewarditem;
		this.rewardamount = rewardamount;
		this.rewardleft = rewardleft;
		this.rewarddivide = (float)requestamount / (float)rewardamount;
		this.playerid = playerid;
		this.playername = playername;

		if (ID == null) {
			if (MineXChange.broadcast) {
				String msg = MineXChange.message;
				msg = ChatColor.translateAlternateColorCodes('&', msg);
				msg = msg.replace("%prefix%", MineXChange.prefix);
				msg = msg.replace("%requestamount%", Integer.toString(requestamount));
				if (getRawItemStack().getItemMeta().hasDisplayName()) {
					msg = msg.replace("%requestitem%", getRawItemStack().getItemMeta().getDisplayName());
				} else {
					msg = msg.replace("%requestitem%", getRawItemStack().getType().toString());
				}
				msg = msg.replace("%rewardamount%", Integer.toString(rewardamount));
				if (getRewardStack(1).getItemMeta().hasDisplayName()) {
					msg = msg.replace("%rewarditem%", getRewardStack(1).getItemMeta().getDisplayName());
				} else {
					msg = msg.replace("%rewarditem%", getRewardStack(1).getType().toString());
				}
				Bukkit.getServer().broadcastMessage(msg);
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
				}
			}
			this.id = setRequestID();
		} else {
			this.id = ID;
		}
		if (createtime == 0) {
			this.createtime = System.currentTimeMillis();
		} else {
			this.createtime = createtime;
		}
	}
	
	
	
	public ItemStack getItemStack() {
		/*
		 * This method returns an itemstack of the request
		 */
		ItemStack returnstack = requestitem.clone();
		
		int num = getTimeLeft();

		String rewardname;
		if (rewarditem.getItemMeta().hasDisplayName()) {
			rewardname = rewarditem.getItemMeta().getDisplayName();
		} else {
			rewardname = WordUtils.capitalize(rewarditem.getType().toString().replace("_", " ").toLowerCase());
		}
		List<String> lore = Arrays.asList(MineXChange.toTitleCase(ChatColor.BLUE.toString() + "Reward: " + ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() +
																	   rewardleft + " " + ChatColor.RESET + rewardname),
										  MineXChange.toTitleCase(ChatColor.BLUE.toString() + "Player: " + playername.toLowerCase()),
										  MineXChange.toTitleCase(ChatColor.GRAY.toString() + "Time Left: " + MineXChange.intToDHM(num)));
		ItemMeta reqmeta = returnstack.getItemMeta();
		if (returnstack.getItemMeta().hasDisplayName()) {
			reqmeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + requestleft + " " + ChatColor.RESET + returnstack.getItemMeta().getDisplayName());
		} else {
			reqmeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + requestleft + " " + ChatColor.RESET + WordUtils.capitalize(returnstack.getType().toString().replace("_", " ").toLowerCase()));
		}
		reqmeta.setLore(lore);
		returnstack.setItemMeta(reqmeta);
		
		return returnstack;
	}
	
	public ItemStack getRawItemStack() {
		return requestitem.clone();
	}
	
	public int getRequestLeft() {
		return requestleft;
	}
	
	public void setRequestLeft(int num) {
		requestleft = num;
	}
	
	public UUID getPlayerID() {
		return playerid;
	}
	
	public String getPlayerName() {
		return playername;
	}
	
	public int getRequestAmount() {
		return requestamount;
	}
	
	public int getRewardAmount() {
		return rewardamount;
	}
	
	public int getRewardLeft() {
		return rewardleft;
	}
	
	public void setRewardLeft(int num) {
		rewardleft = num;
	}
	
	public float getRewardDivide() {
		return rewarddivide;
	}
	
	public int getReward(int amount) {
		int reward = 0;
		for (int i = 0; i < rewardleft; i++) {
			if (requestleft >= (i * rewarddivide) && requestleft - amount <= (i * rewarddivide)) {
				reward += 1;
			}
		}
		return reward;
	}
	
	public ItemStack getRewardStack(int amount) {
		ItemStack reward = rewarditem.clone();
		reward.setAmount(amount);
		return reward;
	}
	
	
	public String setRequestID() { //a request id is given to every unique request. this makes it so players have the correct amount of contributions
		String genstr;
		do {
			int min = 97; //char 'a'
			int max = 122; //char 'z'
			int strlen = 12; //the length of the item ID
			Random random = new Random();
			StringBuilder buffer = new StringBuilder(strlen);
		    for (int i = 0; i < strlen; i++) {
		        int randomLimitedInt = min + (int) 
		          (random.nextFloat() * (max - min + 1));
		        buffer.append((char) randomLimitedInt);
		    }
		    genstr = buffer.toString();
		} while (MineXChange.requestids.contains(genstr));
		return genstr;
	}
	
	public String getRequestID() {
		return id;
	}
	
	public long getCreateTime() {
		return createtime;
	}
	
	public void setCreateTime(long time) {
		createtime = time;
	}
	
	public int getTimeLeft() {
		return (int) (MineXChange.clamp(createtime + MineXChange.timeout - System.currentTimeMillis(), 0, -1));
	}
	
	
	public Inventory createInventory() {
		/*
		 * This method creates the inventory for the request
		 */
	
		Inventory reqinv = Bukkit.getServer().createInventory(null, 54, ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Request Inventory");
		
		ItemStack reqstack = new ItemStack(Material.EMERALD_BLOCK, 1);
		ItemMeta reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Confirm Exchange");
		reqmeta.setLore(Arrays.asList(ChatColor.RED.toString() + "Clicking this will give the player your items", ChatColor.RED.toString() + "The reward will appear in your "
									  + ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "Exchange Inventory", ChatColor.RED.toString() + "in the main menu"));
		reqstack.setItemMeta(reqmeta);
		reqinv.setItem(53, reqstack);
		
		reqstack = new ItemStack(Material.REDSTONE_BLOCK, 1);
		reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Cancel Exchange");
		reqmeta.setLore(Arrays.asList(ChatColor.RED.toString() + "Clicking this will give you your items back", ChatColor.RED.toString() + "If there isn't enough space in your inventory,",
									  ChatColor.RED.toString() + "The items will be dropped at your feet"));
		reqstack.setItemMeta(reqmeta);
		reqinv.setItem(45, reqstack);
		
		
		
		reqstack = new ItemStack(Material.BARRIER, 1);
		reqmeta = reqstack.getItemMeta();
		reqmeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Delete Exchange");
		reqmeta.setLore(Arrays.asList(ChatColor.RED.toString() + "Only staff or the requester", ChatColor.RED.toString() + "can delete this exchange", ChatColor.RED.toString()));
		reqstack.setItemMeta(reqmeta);
		reqinv.setItem(46, reqstack);

		ItemStack reqclone = requestitem.clone();
		ItemStack rewclone = rewarditem.clone();
		ItemMeta reqclonemeta = reqclone.getItemMeta();
		ItemMeta rewclonemeta = rewclone.getItemMeta();
		reqclonemeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		rewclonemeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		reqclone.setItemMeta(reqclonemeta);
		rewclone.setItemMeta(rewclonemeta);
		reqinv.setItem(48, reqclone);
		reqinv.setItem(49, getItemStack());
		reqinv.setItem(50, rewclone);
		
		return reqinv;
	}
	
	
	
	public void removeAmount(int requestamount, int rewardamount) {
		/*
		 * This method subtracts an amount from the remaining amount.
		 */
		requestleft = (int) MineXChange.clamp(requestleft - requestamount, 0, -1);
		rewardleft -= rewardamount;
	}
	
	public int getRemoveAmount(int amount) {
		if (requestleft - amount >= 0) {
			return 0; //return 0 if all can be removed
		} else {
			return amount - requestleft; //return the excess to return to player
		}
	}
		
}
