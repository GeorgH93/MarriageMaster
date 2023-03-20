/*
 *   Copyright (C) 2023 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.Bukkit.ManagedUpdater;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriageMasterReloadEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration.BackpackIntegrationManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration.IBackpackIntegration;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.CommandManagerImplementation;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Database;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Language;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Formatter.PrefixSuffixFormatterImpl;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.BonusXP.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManagerImpl;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.NoDatabaseWorker;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Plugin.IPlugin;
import at.pcgamingfreaks.UUIDConverter;
import at.pcgamingfreaks.Util.StringUtils;
import at.pcgamingfreaks.Version;
import at.pcgamingfreaks.yaml.YAML;
import at.pcgamingfreaks.yaml.YamlKeyNotFoundException;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class MarriageMaster extends JavaPlugin implements MarriageMasterPlugin, IPlugin
{
	@Setter(AccessLevel.PRIVATE) private static Version version = null;
	@Getter @Setter(AccessLevel.PRIVATE) private static MarriageMaster instance;

	// Global Objects
	@Getter private ManagedUpdater updater;
	private Config config = null;
	@Getter private Language language = null;
	@Getter private Database database = null;
	@Getter private IBackpackIntegration backpacksIntegration = null;
	@Getter private PluginChannelCommunicator pluginChannelCommunicator = null;
	@Getter private PrefixSuffixFormatter prefixSuffixFormatter = null;
	private CommandManagerImplementation commandManager = null;
	private MarriageManagerImpl marriageManager = null;
	@Getter private PlaceholderManager placeholderManager = null;

	// Global Settings
	@Getter private boolean selfMarriageAllowed = false, selfDivorceAllowed = false, surnamesEnabled = false, surnamesForced = false;
	private boolean multiplePartnersAllowed = false;

	//region Loading and Unloading the plugin
	@Override
	public void onEnable()
	{
		updater = new ManagedUpdater(this);
		setInstance(this);
		setVersion(new Version(this.getDescription().getVersion()));
		Utils.warnIfPerWorldPluginsIsInstalled(getLogger()); // Check if PerWorldPlugins is installed and show info

		// Check if running as standalone edition
		/*if[STANDALONE]
		getLogger().info("Starting Marriage Master in standalone mode!");
		if(getServer().getPluginManager().isPluginEnabled("PCGF_PluginLib"))
		{
			getLogger().info("You do have the PCGF_PluginLib installed. You may consider switching to the default version of the plugin to reduce memory load and unlock additional features.");
		}
		else[STANDALONE]*/
		// Not standalone so we should check the version of the PluginLib
		if(at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getVersion().olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
		{
			getLogger().warning("You are using an outdated version of the PCGF PluginLib! Please update it!");
			failedToEnablePlugin();
			return;
		}
		/*end[STANDALONE]*/

		loadPermissions();

		config = new Config(this);
		if(!config.isLoaded())
		{
			failedToEnablePlugin();
			return;
		}
		updater.setChannel(config.getUpdateChannel());
		if(config.useUpdater()) updater.update(); // Check for updates
		language = new Language(this);
		BackpackIntegrationManager.initIntegration();
		backpacksIntegration = BackpackIntegrationManager.getIntegration();

		if(!load()) // Load Plugin
		{
			failedToEnablePlugin();
			return;
		}
		getLogger().info(StringUtils.getPluginEnabledMessage("Marriage Master"));
	}

	private void loadPermissions()
	{
		try
		{
			YAML permsYaml = new YAML(this.getClassLoader().getResourceAsStream("permissions.yml"));

			for(String perm : permsYaml.getNodeKeys())
			{
				if (!perm.contains(".description")) continue;
				perm = perm.substring(0, perm.length() - ".description".length());
				String description = permsYaml.getString(perm + ".description", "");
				Map<String, Boolean> children = null;
				try
				{
					YAML childPerms = permsYaml.getSection(perm + ".children");
					children = new HashMap<>();
					for(String child : childPerms.getKeys())
					{
						children.put(child, childPerms.getBoolean(child, true));
					}
				}
				catch(YamlKeyNotFoundException ignored){}
				PermissionDefault permDefault = PermissionDefault.getByName(permsYaml.getString(perm + ".default", "op"));
				if (permDefault == null) permDefault = PermissionDefault.OP;
				this.getServer().getPluginManager().addPermission(new Permission(
						perm, description, permDefault, children
				));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void failedToEnablePlugin()
	{
		getLogger().info(ConsoleColor.RED + "Failed to enable the plugin!" + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
		this.setEnabled(false);
		setInstance(null);
	}

	@Override
	public void onDisable()
	{
		if(config != null && config.isLoaded() && database != null)
		{
			if(config.useUpdater()) updater.update(); // Check for updates
			unload();
		}
		setInstance(null);
		updater.waitForAsyncOperation();
		getLogger().info(StringUtils.getPluginDisabledMessage("Marriage Master"));
	}

	public void reload()
	{
		unload();
		config.reload();
		load();
		getServer().getPluginManager().callEvent(new MarriageMasterReloadEvent());
	}

	private boolean load()
	{
		// Loading base Data
		if(!config.isLoaded() || !language.load(config))
		{
			getLogger().warning(ConsoleColor.RED + "Configuration or language file not loaded correct! Disabling plugin." + ConsoleColor.RESET);
			setEnabled(false);
			return false;
		}
		updater.setChannel(config.getUpdateChannel());

		loadGlobalSettings();

		database = new Database(this);
		if(!database.available())
		{
			getLogger().warning(ConsoleColor.RED + "Failed to connect to database! Please adjust your settings and retry!" + ConsoleColor.RESET);
			new NoDatabaseWorker(this); // Starts the worker that informs everyone with reload permission that the database connection failed.
			return true;
		}
		if(database.useBungee())
		{
			pluginChannelCommunicator = new PluginChannelCommunicator(this);
		}

		CommonMessages.loadCommonMessages(language);

		commandManager = new CommandManagerImplementation(this);
		commandManager.init();
		marriageManager = new MarriageManagerImpl(this);
		prefixSuffixFormatter = new PrefixSuffixFormatterImpl(this);

		registerEvents();

		placeholderManager = new PlaceholderManager(this);

		return true;
	}

	void loadGlobalSettings()
	{
		surnamesEnabled = config.isSurnamesEnabled();
		multiplePartnersAllowed = config.areMultiplePartnersAllowed();
		selfMarriageAllowed = config.isSelfMarriageAllowed();
		selfDivorceAllowed = config.isSelfDivorceAllowed();
		surnamesForced  = config.isSurnamesForced() && surnamesEnabled;
	}

	void registerEvents()
	{
		getServer().getPluginManager().registerEvents(new JoinLeaveWorker(this), this);
		getServer().getPluginManager().registerEvents(new OpenRequestCloser(), this);
		if(config.isBonusXPEnabled())
		{
			if(config.getBonusXpMultiplier() > 1) getServer().getPluginManager().registerEvents(new BonusXpListener(this), this);
			if(config.isBonusXPSplitOnPickupEnabled()) getServer().getPluginManager().registerEvents(new BonusXpSplitOnPickupListener(this), this);
		}
		if(config.isSkillApiBonusXPEnabled() && getServer().getPluginManager().isPluginEnabled("SkillAPI")) getServer().getPluginManager().registerEvents(new SkillApiBonusXpListener(this), this);
		if(config.isMcMMOBonusXPEnabled() && getServer().getPluginManager().isPluginEnabled("mcMMO"))
		{
			Plugin mcMMO = getServer().getPluginManager().getPlugin("mcMMO");
			if(mcMMO != null)
			{
				if(new Version(mcMMO.getDescription().getVersion()).olderThan(new Version("2"))) getServer().getPluginManager().registerEvents(new McMMOClassicBonusXpListener(this), this);
				else getServer().getPluginManager().registerEvents(new McMMOBonusXpListener(this), this);
			}

		}
		if(config.isHPRegainEnabled()) getServer().getPluginManager().registerEvents(new RegainHealth(this), this);
		if(config.isJoinLeaveInfoEnabled()) getServer().getPluginManager().registerEvents(new JoinLeaveInfo(this), this);
		if(config.isEconomyEnabled()) new EconomyHandler(this);
		if(config.isCommandExecutorEnabled()) getServer().getPluginManager().registerEvents(new CommandExecutor(this), this);
		if(getConfiguration().isPrefixEnabled() || getConfiguration().isSuffixEnabled()) getServer().getPluginManager().registerEvents(new ChatPrefixSuffix(this), this);
	}

	private void unload()
	{
		placeholderManager.close();
		getServer().getMessenger().unregisterIncomingPluginChannel(this);
		getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		if(pluginChannelCommunicator != null)
		{
			pluginChannelCommunicator.close();
			pluginChannelCommunicator = null;
		}
		HandlerList.unregisterAll(this);
		getServer().getMessenger().unregisterIncomingPluginChannel(this);
		getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		database.close();
		database = null;
		commandManager.close();
		marriageManager.close();
	}
	//endregion

	public Config getConfiguration()
	{
		return config;
	}

	// API Stuff
	public static Version version()
	{
		return version;
	}

	@Override
	public @NotNull Version getVersion()
	{
		return version;
	}

	@Override
	public boolean areMultiplePartnersAllowed()
	{
		return multiplePartnersAllowed;
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull UUID uuid)
	{
		return database.getPlayer(uuid);
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull String name)
	{
		Player player = getServer().getPlayer(name);
		if(player != null) return getPlayerData(player);
		MarriagePlayerData data = (MarriagePlayerData) getPlayerData(UUIDConverter.getUUIDCacheOnly(name, true));
		data.setName(name);
		return data;
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull OfflinePlayer player)
	{
		return database.getPlayer(player);
	}

	@Override
	public @NotNull Collection<? extends Marriage> getMarriages()
	{
		return database.getCache().getLoadedMarriages();
	}

	@Override
	public boolean isInRange(@NotNull Player player1, @NotNull Player player2, double range)
	{
		return Utils.inRange(player1, player2, range, Permissions.BYPASS_RANGELIMIT);
	}

	@Override
	public boolean isInRangeSquared(@NotNull Player player1, @NotNull Player player2, double rangeSquared)
	{
		return Utils.inRangeSquared(player1, player2, rangeSquared, Permissions.BYPASS_RANGELIMIT);
	}

	@Override
	public void doDelayableTeleportAction(@NotNull final DelayableTeleportAction action)
	{
		if(action.getDelay() == 0 || action.getPlayer().hasPermission(Permissions.BYPASS_DELAY))
		{
			action.run();
		}
		else
		{
			final Player player = action.getPlayer().getPlayerOnline();
			final MarriagePlayerData playerData = (MarriagePlayerData) action.getPlayer();
			if(action.getPlayer().isOnline() && player != null)
			{
				final Location oldLocation = player.getLocation();
				final double oldHealth = player.getHealth();
				CommonMessages.getMessageDontMove().send(player, action.getDelay()/20L);
				if(playerData.getDelayedTpTask() != null) playerData.getDelayedTpTask().cancel();
				playerData.setDelayedTpTask(getServer().getScheduler().runTaskLater(this, () -> {
					playerData.setDelayedTpTask(null);
					if(action.getPlayer().isOnline())
					{
						//noinspection ConstantConditions
						if(oldHealth <= player.getHealth() && Utils.equalsIgnoreOrientation(oldLocation, player.getLocation()))
						{
							action.run();
						}
						else
						{
							CommonMessages.getMessageMoved().send(player);
						}
					}
				}, action.getDelay()));
			}
		}
	}

	@Override
	public @NotNull CommandManager getCommandManager()
	{
		return commandManager;
	}

	@Override
	public @NotNull at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageManager getMarriageManager()
	{
		return marriageManager;
	}

	@Override
	public @NotNull Collection<? extends MarriagePlayer> getPriestsOnline()
	{
		ArrayList<MarriagePlayer> priests = new ArrayList<>();
		for(Player player : getServer().getOnlinePlayers())
		{
			MarriagePlayer marriagePlayer = getPlayerData(player);
			if(marriagePlayer.isPriest()) priests.add(marriagePlayer);
		}
		return priests;
	}

	@Override
	public @NotNull Collection<? extends MarriagePlayer> getPriests()
	{
		return database.getCache().getLoadedPlayers().stream().filter(MarriagePlayerDataBase::isPriest).collect(Collectors.toList());
	}
}