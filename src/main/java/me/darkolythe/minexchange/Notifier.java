package me.darkolythe.minexchange;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Notifier {
	
	/*
	 * This class is in charge of notifying the player when they have items in their exchange inventories. It can be disabled via command
	 */
	
	public MineXChange main; //initialize the main variable as Type main class
	public Notifier(MineXChange plugin) {
		this.main = plugin; //set it equal to an instance of main
	}

	public void broadcast() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(main, new Runnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					UUID uuid = player.getUniqueId();
					if (main.getNotify(uuid)) { //if the player has notify enabled, send messages
						if (!main.getRequestsInventory(uuid).isEmpty() || !main.getRewardsInventory(uuid).isEmpty()) {
							player.sendMessage(MineXChange.prefix + ChatColor.GREEN.toString() + "You have items to retrieve from your exchange inventory!");
						}
					}
				}
			}
		}, 0L, 36000L);
	}
	
}
