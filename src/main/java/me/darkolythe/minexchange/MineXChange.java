package me.darkolythe.minexchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MineXChange extends JavaPlugin implements Listener {
	
	/*
	 * This is the main class. It holds some useful functions, and controls the other classes
	 */

	public static String prefix = new String(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "[" + ChatColor.BLUE.toString() + "MXC" + ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "] ");
	public int cooldownamount = 30*60*1000; //(30 minutes in milliseconds)
	public static int timeout = 86400000 * 7; //(1 day * 7)
	public static Boolean broadcast = false;
	public static String message = "";
	public static Map<UUID, Request> openrequests = new HashMap<>();
	
	public static MineXChange plugin;
	public MainMenu menu;
	public CreateRequestMenu createrequestmenu;
	public Request request;
	public RequestInventory requestinventory;
	public ExchangeInventory exchangeinventory;
	public ConfigManager configmanager;
	public Notifier notifier;
	
	public Request[] requestlist = new Request[45];
	public HashMap<UUID, ArrayList<ItemStack>> requestsinventory = new HashMap<>();
	public HashMap<UUID, ArrayList<ItemStack>> rewardsinventory = new HashMap<>();
	public HashMap<UUID, int[]> playerstats = new HashMap<>();
	public HashMap<UUID, Long> cooldown = new HashMap<>();
	public HashMap<UUID, ArrayList<String>> exchangecontributions = new HashMap<>();
	public static ArrayList<String> requestids = new ArrayList<>();
	public HashMap<UUID, Boolean> notifylist = new HashMap<>();
	
	public int maxrequestamount;
	
	
	
	@Override
	public void onEnable() {
		menu = new MainMenu(this); //get other classes
		plugin = this;
		
		saveDefaultConfig();
		
		getConfigs();
		
		menu = new MainMenu(plugin); //get the Menu class
		createrequestmenu = new CreateRequestMenu(plugin);
		requestinventory = new RequestInventory(plugin);
		exchangeinventory = new ExchangeInventory(plugin);
		notifier = new Notifier(plugin);
		
		configmanager = new ConfigManager(plugin);
		configmanager.setup();
		configmanager.loadRequestList();
		for (Player plr : Bukkit.getServer().getOnlinePlayers()) {
			configmanager.loadPlayerData(plr.getUniqueId());
		}
		configmanager.reloadAll(null);
		
		notifier.broadcast();
		requestinventory.checkConfirm();

		Metrics metrics = new Metrics(plugin);
		
		getServer().getPluginManager().registerEvents(menu, this); //register events from other classes
		getServer().getPluginManager().registerEvents(createrequestmenu, this);
		getServer().getPluginManager().registerEvents(requestinventory, this);
		getServer().getPluginManager().registerEvents(exchangeinventory, this);
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("minexchange").setExecutor(new MinechangeCommand());
		
		System.out.println(prefix + ChatColor.GREEN + "MineXChange Enabled!");
	}
	
	@Override
	public void onDisable() {
		configmanager.saveRequestList();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			configmanager.savePlayerData(player.getUniqueId());
		}
		configmanager.reloadAll(null);
		plugin = null;
		System.out.println(prefix + ChatColor.RED + "MineXChange Disabled!");
	}

	public void getConfigs() {
		reloadConfig();
		cooldownamount = numStringToMilliseconds(getConfig().getString("cooldown"));
		timeout = numStringToMilliseconds(getConfig().getString("timeout"));
		maxrequestamount = getConfig().getInt("maxrequestamount");
		broadcast = getConfig().getBoolean("broadcastrequest");
		message = getConfig().getString("broadcastmessage");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) { //when the player joins, set their cooldown
		configmanager.loadPlayerData(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		configmanager.savePlayerData(event.getPlayer().getUniqueId());
	}
	
	
	
	public int getCooldown(UUID uuid) {
		if (!cooldown.containsKey(uuid)) {
			cooldown.put(uuid, System.currentTimeMillis() + cooldownamount); //return the remaining cooldown in minutes
		}
		int num = (int) clamp(cooldown.get(uuid) - System.currentTimeMillis(), 0, -1);
		return num;
	}
	
	public void setCooldown(UUID uuid, int time) {
		cooldown.put(uuid, clamp((System.currentTimeMillis() + time), 0, -1));
	}
	
	public long getRawCooldown(UUID uuid) {
		if (!cooldown.containsKey(uuid)) {
			cooldown.put(uuid, System.currentTimeMillis() + cooldownamount); //return the remaining cooldown in minutes
		}
		return cooldown.get(uuid);
	}
	
	public int[] getStats(UUID uuid) {
		//return the player's exchange stats. {exchanges aided, items given};
		if (!playerstats.containsKey(uuid)) {
			int[] ints = {0, 0};
			playerstats.put(uuid, ints);
		}
		return playerstats.get(uuid);
	}
	
	public void addStats(UUID uuid, int[] vals) {
		//add values to the player's stats
		if (playerstats.containsKey(uuid)) {
			int[] stats = playerstats.get(uuid);
			stats[0] += vals[0];
			stats[1] += vals[1];
		} else {
			playerstats.put(uuid, vals);
		}
	}
	
	public void setStats(UUID uuid, int[] vals) {
		playerstats.put(uuid, vals);
	}
	
	
	
	
	public Boolean getNotify(UUID uuid) {
		if (!notifylist.containsKey(uuid)) {
			notifylist.put(uuid, true);
		}
		return notifylist.get(uuid);
	}
	
	public void setNotify(UUID uuid, Boolean bool) {
		notifylist.put(uuid, bool);
	}
	
	
	
	
	public ArrayList<ItemStack> getRequestsInventory(UUID uuid) {
		if (!requestsinventory.containsKey(uuid)) {
			ArrayList<ItemStack> itemlist = new ArrayList<ItemStack>();
			requestsinventory.put(uuid, itemlist);
		}
		return requestsinventory.get(uuid);
	}
	
	public void addRequestsInventory(UUID uuid, ItemStack stack) {
		ArrayList<ItemStack> items = getRequestsInventory(uuid);
		items.add(stack);
	}
	
	public void clearRequestsInventory(UUID uuid) {
		ArrayList<ItemStack> items = getRequestsInventory(uuid);
		items.clear();
	}
	
	
	
	public ArrayList<ItemStack> getRewardsInventory(UUID uuid) {
		if (!rewardsinventory.containsKey(uuid)) {
			ArrayList<ItemStack> itemlist = new ArrayList<ItemStack>();
			rewardsinventory.put(uuid, itemlist);
		}
		return rewardsinventory.get(uuid);
	}
	
	public void addRewardsInventory(UUID uuid, ItemStack stack) {
		ArrayList<ItemStack> items = getRewardsInventory(uuid);
		items.add(stack);
	}
	
	public void clearRewardsInventory(UUID uuid) {
		ArrayList<ItemStack> items = getRewardsInventory(uuid);
		items.clear();
	}
	
	
	
	public ArrayList<String> getExchangeContributions(UUID uuid) {
		if (!exchangecontributions.containsKey(uuid)) {
			ArrayList<String> itemid = new ArrayList<String>();
			exchangecontributions.put(uuid, itemid);
		}
		return exchangecontributions.get(uuid);
	}
	
	public void addExchangeContributions(UUID uuid, String str) {
		ArrayList<String> items = getExchangeContributions(uuid);
		items.add(str);
	}
	
	public void clearExchangeContributions(UUID uuid) {
		ArrayList<String> items = getExchangeContributions(uuid);
		items.clear();
	}

	
	
	public Request getRequest(Player player) {
		if (openrequests.containsKey(player.getUniqueId())) {
			return openrequests.get(player.getUniqueId());
		} else {
			openrequests.put(player.getUniqueId(), null);
			return null;
		}
	}
	
	public static void setRequest(Player player, Request request) {
		openrequests.put(player.getUniqueId(), request);
	}
	
	public void clearRequests() {
		for (int i = 0; i < requestlist.length; i++) {
			requestlist[i] = null;
		}
	}
	
	
	
	
	
	public static long clamp(long num, int min, int max) {
		if (num >= max && max != -1) { return max; }
		else if (num <= min ) { return min; }
		else { return num; }
	}
	
	public static String toTitleCase(String str) {
		/*
		 * This method capitalizes all the first characters after spaces
		 */
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : str.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }
	        titleCase.append(c);
	    }
	    return titleCase.toString();
	}
	
	
	
	
	
	public int numStringToMilliseconds(String numstr) {
		/*
		 * This method is for getting config values. for turning user entered values, the function after this one is used
		 */
		String str = numstr.replaceAll("\\d", "");
		int num = Integer.parseInt(numstr.replaceAll("[^\\d]", ""));
		if (str.equals("d")) {
			return num * 24 * 60 * 60 * 1000;
		} else if (str.equals("h")) {
			return num * 60 * 60 * 1000;
		} else if (str.equals("m")) {
			return num * 60 * 1000;
		} else if (str.equals("s")) {
			return num * 1000;
		} else {
			System.out.println(prefix + ChatColor.RED + "Invalid value entered. options: d, h, m, s. ex: 2h");
			return 30*60*1000;
		}
	}
	
	public int formatNum(String numstr, Player player) {
		String str = numstr.replaceAll("\\d", "");
		int num = Integer.parseInt(numstr.replaceAll("[^\\d]", ""));
		if (str.equals("d")) {
			return num * 24 * 60 * 60 * 1000;
		} else if (str.equals("h")) {
			return num * 60 * 60 * 1000;
		} else if (str.equals("m")) {
			return num * 60 * 1000;
		} else if (str.equals("s")) {
			return num * 1000;
		} else {
			player.sendMessage(prefix + ChatColor.RED + "Invalid value entered. options: d, h, m, s. ex: 2h");
			return -1;
		}
	}
	
	public static String intToDHM(int num) {
		/*
		 * This method turns a number into Days, Hours, Minutse format
		 */
		StringBuilder str = new StringBuilder(32);
		str.append(num / 86400000);
		str.append(" Days, ");
		num %= 86400000;
		str.append(num / 3600000);
		str.append(" Hours, ");
		num %= 3600000;
		str.append(num / 60000);
		str.append(" Minutes ");
		return str.toString();
	}

	
	public static MineXChange getInstance() {
		return plugin;
	}
}
