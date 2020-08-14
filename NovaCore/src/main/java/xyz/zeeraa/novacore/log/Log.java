package xyz.zeeraa.novacore.log;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Used to log messages
 * <p>
 * Players and the console can change what logs to receive by using the command
 * <code>/novacore log set <LOG LEVEL><code>
 * 
 * @author Zeeraa
 */
public class Log {
	private static LogLevel consoleLogLevel = LogLevel.INFO;

	public static HashMap<UUID, LogLevel> subscribedPlayers = new HashMap<UUID, LogLevel>();

	public static void trace(String message) {
		Log.trace(null, message);
	}

	public static void debug(String message) {
		Log.debug(null, message);
	}

	public static void info(String message) {
		Log.info(null, message);
	}

	public static void warn(String message) {
		Log.warn(null, message);
	}

	public static void error(String message) {
		Log.error(null, message);
	}

	public static void fatal(String message) {
		Log.fatal(null, message);
	}

	public static void trace(String source, String message) {
		Log.log(source, message, LogLevel.TRACE);
	}

	public static void debug(String source, String message) {
		Log.log(source, message, LogLevel.DEBUG);
	}

	public static void info(String source, String message) {
		Log.log(source, message, LogLevel.INFO);
	}

	public static void warn(String source, String message) {
		Log.log(source, message, LogLevel.WARN);
	}

	public static void error(String source, String message) {
		Log.log(source, message, LogLevel.ERROR);
	}

	public static void fatal(String source, String message) {
		Log.log(source, message, LogLevel.FATAL);
	}

	public static void log(String source, String message, LogLevel logLevel) {
		String fullMessage = "[" + logLevel.getMessagePrefix() + ChatColor.RESET + "]" + (source == null ? "" : "[" + ChatColor.BOLD + source + ChatColor.RESET + "]") + ": " + message;
		if (logLevel.shouldLog(consoleLogLevel)) {
			Bukkit.getServer().getConsoleSender().sendMessage(fullMessage);
		}

		for (UUID uuid : subscribedPlayers.keySet()) {
			LogLevel userLogLevel = subscribedPlayers.get(uuid);
			if (logLevel.shouldLog(userLogLevel)) {
				Player player = Bukkit.getServer().getPlayer(uuid);
				if (player != null) {
					if (player.isOnline()) {
						player.sendMessage(fullMessage);
					}
				}
			}
		}
	}

	/**
	 * Get the log level the console is set to use. Default is {@link LogLevel#INFO}
	 * 
	 * @return The {@link LogLevel} the console is using
	 */
	public static LogLevel getConsoleLogLevel() {
		return consoleLogLevel;
	}

	/**
	 * Set the log level for the console to use. Default is {@link LogLevel#INFO}
	 * 
	 * @param consoleLogLevel The {@link LogLevel} to use
	 */
	public static void setConsoleLogLevel(LogLevel consoleLogLevel) {
		Log.consoleLogLevel = consoleLogLevel;
	}

	public static HashMap<UUID, LogLevel> getSubscribedPlayers() {
		return subscribedPlayers;
	}
}