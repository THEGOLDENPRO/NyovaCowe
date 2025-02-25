package net.zeeraa.novacore.spigot.version.v1_12_R1;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.plugin.Plugin;

/**
 * Based on this:
 * https://bukkit.org/threads/register-command-without-plugin-yml.112932/#post-1430463
 * 
 * @author Zeeraa
 */
public class CommandRegistrator implements net.zeeraa.novacore.spigot.abstraction.CommandRegistrator {
	private CommandMap cmap = null;

	public CommandRegistrator() {
		try {
			Field f = CraftServer.class.getDeclaredField("commandMap");
			f.setAccessible(true);
			cmap = (CommandMap) f.get(Bukkit.getServer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerCommand(Plugin plugin, Command command) {
		cmap.register(plugin.getName(), command);
	}

	@Override
	public CommandMap getCommandMap() {
		return cmap;
	}

	@Override
	public boolean syncCommands() {
		return false;
	}

	@Override
	public Map<String, Command> tryGetKnownCommandsFromSimpleCommandMap(SimpleCommandMap commandMap) {
		// Not supported
		return null;
	}
}