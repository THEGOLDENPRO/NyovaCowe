package net.zeeraa.novacore.spigot.abstraction.enums;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.zeeraa.novacore.spigot.abstraction.VersionIndependantUtils;

/**
 * This class contains some of the in game sounds that can be used instead of
 * the built in {@link Sound} enum since some of those sounds changed in later
 * versions of the game
 * 
 * @author Zeeraa
 *
 */
public enum VersionIndependantSound {
	NOTE_PLING, NOTE_HAT, WITHER_DEATH, WITHER_HURT, ITEM_BREAK, ORB_PICKUP, ANVIL_LAND, EXPLODE;

	public void playAtLocation(Location location) {
		this.playAtLocation(location, 1F, 1F);
	}

	public void playAtLocation(Location location, float volume, float pitch) {
		VersionIndependantUtils.get().playSound(location, this, volume, pitch);
	}
	
	public void play(Player player) {
		this.play(player, player.getLocation(), 1F, 1F);
	}
	
	public void play(Player player, float volume, float pitch) {
		this.play(player, player.getLocation(), volume, pitch);
	}

	public void play(Player player, Location location) {
		this.play(player, location, 1F, 1F);
	}

	public void play(Player player, Location location, float volume, float pitch) {
		VersionIndependantUtils.get().playSound(player, location, this, volume, pitch);
	}
}