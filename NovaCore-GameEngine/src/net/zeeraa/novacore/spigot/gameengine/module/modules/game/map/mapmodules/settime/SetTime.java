package net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodules.settime;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;

public class SetTime extends MapModule {
	private int time;
	private int delay;

	public SetTime(JSONObject json) {
		super(json);

		this.delay = 0;
		this.time = 6000;

		if (json.has("time")) {
			this.time = json.getInt("time");
		}

		if (json.has("delay")) {
			this.delay = json.getInt("delay");
		}
	}

	@Override
	public void onGameStart(Game game) {
		if (game.hasWorld()) {
			if (delay > 0) {
				new BukkitRunnable() {
					@Override
					public void run() {
						game.getWorld().setTime(time);
					}
				}.runTaskLater(game.getPlugin(), delay);
			} else {
				game.getWorld().setTime(time);
			}
		}
	}

	@Override
	public String getSummaryString() {
		return ChatColor.GOLD + "Time: " + ChatColor.AQUA + time + " " + ChatColor.GOLD + "Delay: " + ChatColor.AQUA + delay;
	}
}