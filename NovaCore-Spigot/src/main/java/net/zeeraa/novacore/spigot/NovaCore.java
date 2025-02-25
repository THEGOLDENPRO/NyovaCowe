package net.zeeraa.novacore.spigot;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;

import net.zeeraa.novacore.spigot.abstraction.packet.MinecraftChannelDuplexHandler;
import net.zeeraa.novacore.spigot.spectators.SpectatorListener;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;

import net.novauniverse.novacore.bstats.Metrics;
import net.zeeraa.novacore.commons.NovaCommons;
import net.zeeraa.novacore.commons.ServerType;
import net.zeeraa.novacore.commons.api.novauniverse.NovaUniverseAPI;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.log.LogLevel;
import net.zeeraa.novacore.commons.utils.DelayedRunner;
import net.zeeraa.novacore.commons.utils.Hastebin;
import net.zeeraa.novacore.commons.utils.JSONFileType;
import net.zeeraa.novacore.commons.utils.JSONFileUtils;
import net.zeeraa.novacore.spigot.abstraction.CommandRegistrator;
import net.zeeraa.novacore.spigot.abstraction.NovaCoreAbstraction;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependantLoader;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.abstraction.commons.AbstractBukkitConsoleSender;
import net.zeeraa.novacore.spigot.abstraction.commons.AbstractBukkitPlayerMessageSender;
import net.zeeraa.novacore.spigot.abstraction.commons.BukkitAsyncManager;
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentMaterial;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentSound;
import net.zeeraa.novacore.spigot.abstraction.log.AbstractionLogger;
import net.zeeraa.novacore.spigot.abstraction.particle.NovaParticleProvider;
import net.zeeraa.novacore.spigot.command.CommandRegistry;
import net.zeeraa.novacore.spigot.command.commands.dumplanguagenodes.DumpLanguageNodesCommand;
import net.zeeraa.novacore.spigot.command.commands.novacore.NovaCoreCommand;
import net.zeeraa.novacore.spigot.command.fallback.ReflectionBasedCommandRegistrator;
import net.zeeraa.novacore.spigot.customcrafting.CustomCraftingManager;
import net.zeeraa.novacore.spigot.debug.DebugCommandRegistrator;
import net.zeeraa.novacore.spigot.debug.builtin.BuiltinDebugTriggers;
import net.zeeraa.novacore.spigot.delayedrunner.DelayedRunnerImplementationSpigot;
import net.zeeraa.novacore.spigot.language.LanguageReader;
import net.zeeraa.novacore.spigot.logger.SpigotAbstractionLogger;
import net.zeeraa.novacore.spigot.loottable.LootTableManager;
import net.zeeraa.novacore.spigot.loottable.loottables.V1.LootTableLoaderV1;
import net.zeeraa.novacore.spigot.loottable.loottables.V1.LootTableLoaderV1Legacy;
import net.zeeraa.novacore.spigot.loottable.loottables.randomiser.RandomizerLootTableLoader;
import net.zeeraa.novacore.spigot.mapdisplay.MapDisplayManager;
import net.zeeraa.novacore.spigot.mapdisplay.command.MapDisplayCommand;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.event.ModuleDisabledEvent;
import net.zeeraa.novacore.spigot.module.event.ModuleEnableEvent;
import net.zeeraa.novacore.spigot.module.modules.chestloot.ChestLootManager;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTracker;
import net.zeeraa.novacore.spigot.module.modules.cooldown.CooldownManager;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.deltatime.DeltaTime;
import net.zeeraa.novacore.spigot.module.modules.glowmanager.GlowManager;
import net.zeeraa.novacore.spigot.module.modules.gui.GUIManager;
import net.zeeraa.novacore.spigot.module.modules.jumppad.JumpPadManager;
import net.zeeraa.novacore.spigot.module.modules.lootdrop.LootDropManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import net.zeeraa.novacore.spigot.particles.DefaultNovaParticleProvider;
import net.zeeraa.novacore.spigot.permission.PermissionRegistrator;
import net.zeeraa.novacore.spigot.platformindependent.SpigotPlatformIndependentBungeecordAPI;
import net.zeeraa.novacore.spigot.platformindependent.SpigotPlatformIndependentPlayerAPI;
import net.zeeraa.novacore.spigot.tasks.abstraction.BukkitSimpleTaskCreator;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.CitizensUtils;

public class NovaCore extends JavaPlugin implements Listener {
	private static NovaCore instance;

	private CommandRegistrator bukkitCommandRegistrator;

	private LootTableManager lootTableManager;

	private File jumpPadFile;

	private File logSeverityConfigFile;
	private FileConfiguration logSeverityConfig;

	private TeamManager teamManager;

	private VersionIndependentUtils versionIndependentUtils;

	private CustomCraftingManager customCraftingManager;

	private CitizensUtils citizensUtils;

	private boolean hologramsSupport;

	private boolean loadingDone;

	private boolean noNMSMode;

	private boolean packetManagerEnabled;

	private boolean disableAdvancedGUISupport;
	private int advancedGUIMultiverseReloadDelay;

	private ReflectionBasedCommandRegistrator reflectionBasedCommandRegistrator;

	private LogLevel defaultOpLogLevel = LogLevel.ERROR;

	private NovaParticleProvider novaParticleProvider;

	private boolean disableUnregisteringCommands;

	/**
	 * Check if the NovaCoreGameEngine plugin is enabled
	 * 
	 * @return <code>true</code> if the game engine is enabled
	 */
	public static boolean isNovaGameEngineEnabled() {
		return Bukkit.getServer().getPluginManager().getPlugin("NovaCoreGameEngine") != null;
	}

	/**
	 * Get instance of the {@link NovaCore} plugin
	 * 
	 * @return {@link NovaCore} instance
	 */
	public static NovaCore getInstance() {
		return instance;
	}

	/**
	 * Get the {@link CommandRegistrator} for this version
	 * 
	 * @return {@link CommandRegistrator}
	 */
	public CommandRegistrator getCommandRegistrator() {
		return bukkitCommandRegistrator;
	}

	/**
	 * Get the instance of {@link LootTableManager}
	 * 
	 * @return {@link LootTableManager} instance
	 */
	public LootTableManager getLootTableManager() {
		return lootTableManager;
	}

	public TeamManager getTeamManager() {
		return teamManager;
	}

	/**
	 * Set the {@link TeamManager} to use
	 * 
	 * @param teamManager The {@link TeamManager} to use
	 */
	public void setTeamManager(TeamManager teamManager) {
		this.teamManager = teamManager;
	}

	/**
	 * Check if a {@link TeamManager} has been defined
	 * 
	 * @return <code>true</code> if a {@link TeamManager} has been defined
	 */
	public boolean hasTeamManager() {
		return teamManager != null;
	}

	/**
	 * Get the instance of {@link CustomCraftingManager}
	 * 
	 * @return {@link CustomCraftingManager} instance
	 */
	public CustomCraftingManager getCustomCraftingManager() {

		return customCraftingManager;
	}

	/**
	 * Get the instance of {@link VersionIndependentUtils} for this version
	 * 
	 * @return {@link VersionIndependentUtils} instance
	 */
	public VersionIndependentUtils getVersionIndependentUtils() {
		return versionIndependentUtils;
	}

	/**
	 * Get the instance of {@link VersionIndependentUtils} for this version
	 * 
	 * @return {@link VersionIndependentUtils} instance
	 */
	public static VersionIndependentUtils versionIndependantUtils() {
		return NovaCore.getInstance().getVersionIndependentUtils();
	}

	/**
	 * Check in holographic displays is installed
	 * 
	 * @return <code>true</code> if the holographic displays plugin is installed
	 */
	public boolean hasHologramsSupport() {
		return hologramsSupport;
	}

	/**
	 * Set the console log level
	 * 
	 * @param logLevel Log level for the console
	 */
	public void setLogLevel(LogLevel logLevel) {
		try {
			Log.info("NovaCore", "Setting console log level to " + logLevel.name());
			Log.setConsoleLogLevel(logLevel);
			logSeverityConfig.set("severity", logLevel.name());
			logSeverityConfig.save(logSeverityConfigFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the instance of {@link CitizensUtils}
	 * 
	 * @return {@link CitizensUtils} instance
	 */
	public CitizensUtils getCitizensUtils() {
		return citizensUtils;
	}

	/**
	 * Check if citizens utils is available
	 * 
	 * @return <code>true</code> if citizens is installed and citizens utils is
	 *         available
	 */
	public boolean hasCitizensUtils() {
		return citizensUtils != null;
	}

	/**
	 * Check if the plugin is running in no nms mode. If true
	 * {@link VersionIndependentUtils} wont be avaliabe
	 * 
	 * @return <code>true</code> if no nms mode is enabled
	 */
	public boolean isNoNMSMode() {
		return noNMSMode;
	}

	public ReflectionBasedCommandRegistrator getReflectionBasedCommandRegistrator() {
		return reflectionBasedCommandRegistrator;
	}

	public Hastebin getHastebinInstance() {
		return NovaCommons.getDefaultHastebinInstance();
	}

	public boolean isAdvancedGUISupportDisabled() {
		return disableAdvancedGUISupport;
	}

	public int getAdvancedGUIMultiverseReloadDelay() {
		return advancedGUIMultiverseReloadDelay;
	}

	public NovaParticleProvider getNovaParticleProvider() {
		return novaParticleProvider;
	}

	public void setNovaParticleProvider(NovaParticleProvider novaParticleProvider) {
		this.novaParticleProvider = novaParticleProvider;
	}

	public boolean runVersionIndependentLayerSelftest() {
		if (noNMSMode) {
			Log.error("NovaCore", "Cant run selftest in no nms mode");
			return false;
		}
		
		DelayedRunner.setImplementation(new DelayedRunnerImplementationSpigot());

		try {
			boolean ok = true;
			VersionIndependentUtils.get().resetLastError();
			Log.info("NovaCore", "Running version independent layer selftest");

			Log.debug("NovaCore", "SelfTest: Looping thru VersionIndependentMaterial");
			for (VersionIndependentMaterial m : VersionIndependentMaterial.values()) {
				Log.trace("NovaCore", "SelfTest: getMaterial(): " + m.name());
				VersionIndependentUtils.get().getMaterial(m);
			}

			if (VersionIndependentUtils.get().getLastError().isProblem()) {
				Log.error("NovaCore", "Errors detected while running selftest: VersionIndependentUtils last error is " + VersionIndependentUtils.get().getLastError().name() + " after get material test");
				VersionIndependentUtils.get().resetLastError();
				ok = false;
			}

			// Sound test
			Log.debug("NovaCore", "SelfTest: Looping thru VersionIndependentSound");
			for (VersionIndependentSound s : VersionIndependentSound.values()) {
				Log.trace("NovaCore", "SelfTest: getSound(): " + s.name());
				VersionIndependentUtils.get().getSound(s);
			}

			if (VersionIndependentUtils.get().getLastError().isProblem()) {
				Log.error("NovaCore", "Errors detected while running selftest: VersionIndependentUtils last error is " + VersionIndependentUtils.get().getLastError().name() + " after get sound test");
				VersionIndependentUtils.get().resetLastError();
				ok = false;
			}

			for (ColoredBlockType type : ColoredBlockType.values()) {
				for (DyeColor color : DyeColor.values()) {
					ItemStack item = VersionIndependentUtils.get().getColoredItem(color, type);
					if (item.getType() == Material.AIR) {
						Log.error("NovaCore", "Errors detected while running selftest: VersionIndependentUtils getColoredItem with color " + color.name() + " and type " + type.name() + " returned material type AIR");
						ok = false;
					}
				}
			}

			VersionIndependentUtils.get().resetLastError();
			return ok;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("NovaCore", "An exception occured while running selftest. " + e.getClass().getName() + " " + e.getMessage());
			return false;
		}
	}

	/**
	 * Check if the packet manager is enabled
	 * 
	 * @return <code>true</code> if packet manager is enabled
	 */
	public boolean isPacketManagerEnabled() {
		return packetManagerEnabled;
	}

	/**
	 * Enable the packet manager. If the packet manager is already enabled this wont
	 * do anything
	 * 
	 * @return <code>false</code> if the packet manager could not be started
	 */
	public boolean enablePacketManager() {
		if (noNMSMode) {
			Log.error("NovaCore", "Could not enable packet manager since we are running in NoNMS mode");
			return false;
		}

		if (packetManagerEnabled) {
			return true;
		}
		Bukkit.getServer().getPluginManager().registerEvents(VersionIndependentUtils.get().getPacketManager(), this);
		Log.info("NovaCore", "Packet manager enabled");
		packetManagerEnabled = true;
		return true;
	}

	public boolean enablePacketDebugging() {
		if (noNMSMode) {
			Log.error("NovaCore", "Could not enable packet debugging since we are running in NoNMS mode");
			return false;
		}
		if (!packetManagerEnabled) {
			Log.warn("NovaCore", "Could not enable packet debugging since the packet manager is not enabled");
			return false;
		}
		if (MinecraftChannelDuplexHandler.isDebug()) {
			Log.warn("NovaCore", "Packet Debugging is already disabled.");
			return false;
		}
		Log.info("NovaCore", "Packet debugging enabled");
		MinecraftChannelDuplexHandler.setDebug(true);
		return true;
	}

	public boolean disablePacketDebugging() {
		if (noNMSMode) {
			Log.error("NovaCore", "Could not disable packet debugging since we are running in NoNMS mode");
			return false;
		}
		if (!packetManagerEnabled) {
			Log.warn("NovaCore", "Could not disable packet debugging since the packet manager is not enabled");
			return false;
		}
		if (!MinecraftChannelDuplexHandler.isDebug()) {
			Log.warn("NovaCore", "Packet Debugging is already disabled.");
			return false;
		}
		Log.info("NovaCore", "Packet debugging disabled");
		MinecraftChannelDuplexHandler.setDebug(false);
		return true;
	}

	@Override
	public void onEnable() {
		NovaCore.instance = this;
		this.teamManager = null;
		this.citizensUtils = null;
		this.noNMSMode = false;
		this.reflectionBasedCommandRegistrator = new ReflectionBasedCommandRegistrator();

		this.disableUnregisteringCommands = false;

		AbstractionLogger.setLogger(new SpigotAbstractionLogger());

		saveDefaultConfig();

		ConfigurationSection commandRegistratorOptions = getConfig().getConfigurationSection("CommandRegistrator");

		NovaCommons.setAbstractConsoleSender(new AbstractBukkitConsoleSender());
		NovaCommons.setAbstractPlayerMessageSender(new AbstractBukkitPlayerMessageSender());
		NovaCommons.setAbstractSimpleTaskCreator(new BukkitSimpleTaskCreator());
		NovaCommons.setAbstractAsyncManager(new BukkitAsyncManager(this));
		NovaCommons.setPlatformIndependentPlayerAPI(new SpigotPlatformIndependentPlayerAPI());
		NovaCommons.setServerType(ServerType.SPIGOT);
		NovaCommons.setExtendedDebugging(getConfig().getBoolean("ExtendedDebugging"));

		Log.setConsoleLogLevel(LogLevel.INFO);

		Hastebin defaultHastebinInstance;
		try {
			String defaultHastebinURL = getConfig().getString("HastebinURL");
			defaultHastebinInstance = new Hastebin(defaultHastebinURL);
			Log.info("NovaCore", "Configured hastebin url is " + defaultHastebinInstance.getBaseUrl());
		} catch (IllegalArgumentException e) {
			Log.error("NovaCore", "The HastebinURL in config.yml is not valid. Using https://hastebin.novauniverse.net instead");
			defaultHastebinInstance = new Hastebin("https://hastebin.novauniverse.net");
		}
		NovaCommons.setDefaultHastebinInstance(defaultHastebinInstance);

		novaParticleProvider = new DefaultNovaParticleProvider();

		jumpPadFile = new File(this.getDataFolder().getPath() + File.separator + "jump_pads.json");

		File lootTableFolder = new File(this.getDataFolder().getPath() + File.separator + "LootTables");
		logSeverityConfigFile = new File(this.getDataFolder(), "log_severity.yml");

		this.disableUnregisteringCommands = commandRegistratorOptions.getBoolean("DisableUnregistation");
		if (disableUnregisteringCommands) {
			Log.warn("NovaCore", "Commands will not be unregistered since DisableUnregistation is set to true in config.yml");
		}

		try {
			FileUtils.forceMkdir(this.getDataFolder());
			FileUtils.forceMkdir(lootTableFolder);

			if (!jumpPadFile.exists()) {
				JSONFileUtils.createEmpty(jumpPadFile, JSONFileType.JSONArray);
			}

			if (!logSeverityConfigFile.exists()) {
				Log.info("NovaCore", "Creating log_severity.yml");
				FileUtils.touch(logSeverityConfigFile);
			}
			logSeverityConfig = YamlConfiguration.loadConfiguration(logSeverityConfigFile);

			if (!logSeverityConfig.contains("severity")) {
				logSeverityConfig.set("severity", LogLevel.INFO.name());
				logSeverityConfig.save(logSeverityConfigFile);
			}

			String logLevelName = logSeverityConfig.getString("severity");

			try {
				LogLevel logLevel = LogLevel.valueOf(logLevelName);
				Log.setConsoleLogLevel(logLevel);
			} catch (Exception e) {
				Log.warn("NovaCore", "The value " + logLevelName + " is not a valid LogLevel. Resetting it to " + LogLevel.INFO.name());
				logSeverityConfig.set("severity", LogLevel.INFO.name());
				logSeverityConfig.save(logSeverityConfigFile);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.fatal("NovaCore", "Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		String version = NovaCoreAbstraction.getNMSVersion();

		Log.info("NovaCore", "Server version: " + version);

		boolean forceReflectionCommandRegistrator = commandRegistratorOptions.getBoolean("ForceUseReflectionBasedRegistrator");

		try {
			Class<?> clazz = Class.forName("net.zeeraa.novacore.spigot.version." + version + ".VersionIndependentLoader");
			if (VersionIndependantLoader.class.isAssignableFrom(clazz)) {
				VersionIndependantLoader versionIndependantLoader = (VersionIndependantLoader) clazz.getConstructor().newInstance();

				if (!forceReflectionCommandRegistrator) {
					bukkitCommandRegistrator = versionIndependantLoader.getCommandRegistrator();
					if (bukkitCommandRegistrator == null) {
						Log.warn("NovaCore", "CommandRegistrator is not supported for this version");
					}
				}

				versionIndependentUtils = versionIndependantLoader.getVersionIndependentUtils();
				if (versionIndependentUtils == null) {
					Log.warn("NovaCore", "VersionIndependentUtils is not supported for this version");
					Log.warn("NovaCore", "Could not register events from ChunkLoader and PacketManager");
				} else {
					VersionIndependentUtils.setInstance(versionIndependentUtils);
					Bukkit.getServer().getPluginManager().registerEvents(VersionIndependentUtils.get().getChunkLoader(), this);
				}

				Bukkit.getServer().getPluginManager().registerEvents(versionIndependantLoader.getListeners(), this);

				NovaParticleProvider versionSpecificParticleProvider = versionIndependantLoader.getVersionSpecificParticleProvider();
				if (versionSpecificParticleProvider == null) {
					Log.info("NovaCore", "No version specific particle provider found. Using default implementation");
				} else {
					Log.info("NovaCore", "Using particle provider " + versionIndependantLoader.getClass().getName());
					this.novaParticleProvider = versionSpecificParticleProvider;
				}

			} else {
				throw new InvalidClassException(clazz.getName() + " is not assignable from " + VersionIndependantLoader.class.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.fatal("NovaCore", "Could not find support for this CraftBukkit version.");
			if (this.getConfig().getBoolean("IgnoreMissingNMS")) {
				noNMSMode = true;
				Log.warn("NovaCore", "Ignoring missing NMS support due to IgnoreMissingNMS being set to true. The error above can be ignored but some parts of this plugin wont work");
			} else {
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}

		if (forceReflectionCommandRegistrator) {
			Log.info("NovaCore", "Using reflection based command registrator since ForceUseReflectionBasedRegistrator is set to true in config.yml");
			bukkitCommandRegistrator = reflectionBasedCommandRegistrator;
		}

		if (bukkitCommandRegistrator.getCommandMap() == null) {
			Log.error("NovaCore", "The implementation of CommandRegistrator returned null when attempting to fetch command map. Using reflection based fallback instead");
			bukkitCommandRegistrator = reflectionBasedCommandRegistrator;
		}

		if (bukkitCommandRegistrator == null) {
			Log.warn("NovaCore", "No command registrator defined. Using the reflection based fallback");
			bukkitCommandRegistrator = reflectionBasedCommandRegistrator;
		}

		if (Bukkit.getServer().getPluginManager().getPlugin("HolographicDisplays") != null) {
			this.hologramsSupport = true;
			Log.info("NovaCore", "Hologram support enabled");
		} else {
			this.hologramsSupport = false;
			Log.warn("NovaCore", "Hologram support disabled due to HolographicDisplays not being installed");
		}

		if (getConfig().getBoolean("EnablePacketManager")) {
			this.enablePacketManager();
		}

		// Register permissions for log levels
		for (LogLevel ll : LogLevel.values()) {
			PermissionRegistrator.registerPermission("novacore.loglevel.auto." + ll.name().toLowerCase(), "Sets the players log level to " + ll.name().toLowerCase() + " whan they join", PermissionDefault.FALSE);
		}

		// Set op log level
		String defaultOpLogLevelString = getConfig().getString("DefaultOpLogLevel");
		boolean cfgOpLogLevelFound = false;
		if (defaultOpLogLevelString != null) {
			for (LogLevel logLevel : LogLevel.values()) {
				if (logLevel.name().equalsIgnoreCase(defaultOpLogLevelString)) {
					defaultOpLogLevel = logLevel;
					Log.info("NovaCore", "Set default log level for op players to " + defaultOpLogLevel.name());
					cfgOpLogLevelFound = true;
					break;
				}
			}
		}

		if (!cfgOpLogLevelFound) {
			Log.warn("NovaCore", "Unknown DefaultOpLogLevel: " + defaultOpLogLevelString + " in config.yml");
		}

		Log.info("NovaCore", "Loading language files...");
		try {
			LanguageReader.readFromJar(this.getClass(), "/lang/en-us.json");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			NovaUniverseAPI.setMojangAPIProxyBaseURL(getConfig().getString("MojangAPIProxyURL"));
		} catch (IllegalArgumentException e) {
			Log.error("NovaCore", "The MojangAPIProxyURL in config.yml is not valid. Using https://mojangapi.novauniverse.net as the default one instead");
		}

		lootTableManager = new LootTableManager();

		lootTableManager.addLoader(new LootTableLoaderV1());
		lootTableManager.addLoader(new LootTableLoaderV1Legacy());
		lootTableManager.addLoader(new RandomizerLootTableLoader());

		Log.info("NovaCore", "Loading loot tables from: " + lootTableFolder.getPath());
		lootTableManager.loadAll(lootTableFolder);

		customCraftingManager = new CustomCraftingManager();

		// Register plugin channels
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		// Platform independent Bungeecord API
		NovaCommons.setPlatformIndependentBungeecordAPI(new SpigotPlatformIndependentBungeecordAPI());

		// Register events
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(customCraftingManager, this);
		new SpectatorListener();

		// Load modules
		ModuleManager.loadModule(this, DeltaTime.class);
		ModuleManager.loadModule(this, GUIManager.class);
		ModuleManager.loadModule(this, LootDropManager.class);
		ModuleManager.loadModule(this, ChestLootManager.class);
		ModuleManager.loadModule(this, MultiverseManager.class);
		ModuleManager.loadModule(this, CompassTracker.class);
		ModuleManager.loadModule(this, NetherBoardScoreboard.class);
		ModuleManager.loadModule(this, JumpPadManager.class);
		ModuleManager.loadModule(this, GlowManager.class);
		ModuleManager.loadModule(this, CooldownManager.class);

		// Modules that might be enabled depending on the configuration
		ModuleManager.loadModule(this, CustomItemManager.class);
		ModuleManager.loadModule(this, MapDisplayManager.class);

		// Module configuration
		ConfigurationSection mapDisplayManagerConfig = getConfig().getConfigurationSection("MapDisplayManager");
		MapDisplayManager.getInstance().setWorldDataLoadingEnabled(mapDisplayManagerConfig.getBoolean("EnableWorldDataLoading"));
		MapDisplayManager.getInstance().setWorldDataSavingDisabled(mapDisplayManagerConfig.getBoolean("DisableWorldDataSaving"));

		// Enable modules
		ConfigurationSection enableModules = getConfig().getConfigurationSection("EnableModules");

		if (enableModules.getBoolean("CustomItemManager")) {
			ModuleManager.enable(CustomItemManager.class);
		}

		if (enableModules.getBoolean("MapDisplayManager")) {
			ModuleManager.enable(MapDisplayManager.class);
		}

		// Check if Citizens is enabled
		if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") != null) {
			citizensUtils = new CitizensUtils();
		}

		disableAdvancedGUISupport = getConfig().getBoolean("DisableAdvancedGUIAupport");
		advancedGUIMultiverseReloadDelay = getConfig().getInt("AdvancedGUIMultiverseReloadDelay");

		CommandRegistry.registerCommand(new NovaCoreCommand());
		CommandRegistry.registerCommand(new MapDisplayCommand());
		CommandRegistry.registerCommand(new DumpLanguageNodesCommand());

		CommandRegistry.syncCommands();

		new DebugCommandRegistrator();
		new BuiltinDebugTriggers();

		new BukkitRunnable() {
			@Override
			public void run() {
				loadingDone = true;

				boolean selfTestResult = runVersionIndependentLayerSelftest();
				if (selfTestResult) {
					Log.info("NovaCore", "Self test did not detect any errors");
				} else {
					Log.error("NovaCore", "Errors where detected while running the selftest. Check console for more details. Some features of this plugin might not work as expected");
				}
			}
		}.runTaskLater(this, 1L);

		if (NovaCommons.isExtendedDebugging()) {
			Log.info("NovaCore", "Extended debugging enabled. You can disable this in plugins/NovaCore/config.yml");
		}

		if (getConfig().getBoolean("DisableMetrics")) {
			Log.info("NovaCore", "Metrics disabled");
		} else {
			Log.info("NovaCore", "Starting metrics provided by bStats. This can be disabled in config.yml");
			new Metrics(this, 15987);
		}
	}

	@Override
	public void onDisable() {
		// VersionIndependentUtils.get().getPacketManager().removeOnlinePlayers();
		// Cancel scheduler tasks
		Bukkit.getScheduler().cancelTasks(this);

		if (ModuleManager.moduleExists(JumpPadManager.class)) {
			try {
				if (JumpPadManager.getInstance().hasBeenEnabledBefore()) {
					JumpPadManager.getInstance().saveJumpPads(jumpPadFile, this);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Disable modules
		ModuleManager.disableAll();

		// Unregister listeners
		HandlerList.unregisterAll((Plugin) this);

		// Unregister plugin channels
		Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
	}

	/**
	 * Read all jump pads from the jump pads file
	 */
	private final void loadNovaCoreJumpPads() {
		try {
			JumpPadManager.getInstance().loadJumpPads(jumpPadFile, this);
		} catch (JSONException | IOException e1) {
			e1.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent e) {
		Plugin plugin = e.getPlugin();
		ModuleManager.removePluginModules(plugin);
		if (!disableUnregisteringCommands) {
			CommandRegistry.removePluginCommands(plugin);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onModuleEnable(ModuleEnableEvent e) {
		if (e.isModule(JumpPadManager.class)) {
			if (loadingDone) {
				Log.info("NovaCore", "Loading jump pads");
				loadNovaCoreJumpPads();
			} else {
				new BukkitRunnable() {
					@Override
					public void run() {
						Log.info("NovaCore", "Loading jump pads (Delayed)");
						loadNovaCoreJumpPads();
					}
				}.runTaskLater(this, 1L);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onModuleDisable(ModuleDisabledEvent e) {
		if (e.isModule(JumpPadManager.class)) {
			try {
				if (JumpPadManager.getInstance().hasBeenEnabledBefore()) {
					JumpPadManager.getInstance().saveJumpPads(jumpPadFile, this);
				}
			} catch (JSONException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		if (Log.getSubscribedPlayers().containsKey(player.getUniqueId())) {
			// Player has set their log level, do not modify
			return;
		}

		if (player.isOp()) {
			if (defaultOpLogLevel != LogLevel.NONE && defaultOpLogLevel != null) {
				Log.getSubscribedPlayers().put(player.getUniqueId(), defaultOpLogLevel);
				Log.trace("NovaCore", "Set the log level of " + player.getName() + " to " + defaultOpLogLevel.name() + " since they have op. This can be changed in config.yml");
			}
		}

		// Used to check if the player have 2 or more permission levels
		int permissionLevelCount = 0;

		// Used to list permissions on error
		StringBuilder perms = new StringBuilder();

		// Register permissions for log levels
		for (LogLevel ll : LogLevel.values()) {
			if (player.hasPermission("novacore.loglevel.auto." + ll.name().toLowerCase())) {
				perms.append("novacore.loglevel.auto.").append(ll.name().toLowerCase()).append(" ");

				Log.debug("NovaCore", "Set " + player.getName() + "s log level to " + ll.name() + " because they have the novacore.loglevel.auto." + ll.name().toLowerCase() + " permission");
				Log.getSubscribedPlayers().put(player.getUniqueId(), ll);
				permissionLevelCount++;
			}
		}

		if (permissionLevelCount > 1) {
			Log.warn("NovaCore", player.getName() + " has multiple log level set permissions. Please remove permissions until they only have one of the following: " + perms);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlaceBlock(BlockPlaceEvent e) {

	}

}

// UwU