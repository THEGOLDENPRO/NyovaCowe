package xyz.zeeraa.novacore.abstraction.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VersionIndependantPlayerAchievementAwardedEvent extends Event implements Cancellable{
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	private boolean cancel;
	
	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

}