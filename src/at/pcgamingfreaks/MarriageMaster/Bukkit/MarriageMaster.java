/*
 *   Copyright (C) 2019 GeorgH93
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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriageMasterReloadEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration.BackpackIntegrationManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration.IBackpackIntegration;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.CommandManagerImplementation;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Database;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Language;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.NoDatabaseWorker;
import at.pcgamingfreaks.MarriageMaster.IUpdater;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Updater.UpdateProviders.UpdateProvider;
import at.pcgamingfreaks.Updater.Updater;
import at.pcgamingfreaks.Version;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.UUID;

public class MarriageMaster extends JavaPlugin implements MarriageMasterPlugin, IUpdater
{
	private static final String MIN_PCGF_PLUGIN_LIB_VERSION = "1.0.17-SNAPSHOT";
	private static final String RANGE_LIMIT_PERM = "marry.bypass.rangelimit";
	@Setter(AccessLevel.PRIVATE) private static Version version = null;
	@Getter @Setter(AccessLevel.PRIVATE) private static MarriageMaster instance;

	// Global Objects
	private Config config = null;
	@Getter private Language language = null;
	@Getter private Database database = null;
	@Getter private IBackpackIntegration backpacksIntegration = null;
	@Getter private PluginChannelCommunicator pluginChannelCommunicator = null;
	@Getter private PrefixSuffixFormatter prefixSuffixFormatter = null;
	private CommandManagerImplementation commandManager = null;
	private MarriageManager marriageManager = null;
	private PlaceholderManager placeholderManager = null;

	// Global Settings
	private boolean multiMarriage = false, selfMarriage = false, selfDivorce = false, surnamesEnabled = false, surnamesForced = false;

	// Global Translations
	public String helpPartnerNameVariable, helpPlayerNameVariable;
	public Message messageNotANumber, messageNoPermission, messageNotFromConsole, messageNotMarried, messagePartnerOffline, messagePartnerNotInRange, messageTargetPartnerNotFound,
					messagePlayerNotFound, messagePlayerNotMarried, messagePlayerNotOnline, messagePlayersNotMarried, messageMoved, messageDontMove, messageMarriageNotExact;

	/*if[STANDALONE]
	@Override
	public boolean isRunningInStandaloneMode()
	{
		return true;
	}
	end[STANDALONE]*/

	//region Loading and Unloading the plugin
	@Override
	public void onEnable()
	{
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
		if(at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getVersion().olderThan(new Version(MIN_PCGF_PLUGIN_LIB_VERSION)))
		{
			getLogger().warning("You are using an outdated version of the PCGF PluginLib! Please update it!");
			failedToEnablePlugin();
			return;
		}
		/*end[STANDALONE]*/

		config = new Config(this);
		if(!config.isLoaded())
		{
			failedToEnablePlugin();
			return;
		}
		if(config.useUpdater()) update(null); // Check for updates
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

	private void failedToEnablePlugin()
	{
		getLogger().info(ConsoleColor.RED + "Failed to enable plugin!" + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
		this.setEnabled(false);
		instance = null;
	}

	@Override
	public void onDisable()
	{
		Updater updater = null;
		if(config != null && config.isLoaded() && database != null)
		{
			if(config.useUpdater()) updater = update(null); // Check for updates
			unload();
		}
		if(placeholderManager != null) placeholderManager.close();
		setInstance(null);
		if(updater != null) updater.waitForAsyncOperation();
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
			getLogger().warning(ConsoleColor.RED + "Configuration or language file not loaded correct! Disable plugin." + ConsoleColor.RESET);
			setEnabled(false);
			return false;
		}

		// Loading data
		surnamesEnabled = config.isSurnamesEnabled();
		multiMarriage   = config.areMultiplePartnersAllowed();
		selfMarriage    = config.isSelfMarriageAllowed();
		selfDivorce     = config.isSelfDivorceAllowed();
		surnamesForced  = config.isSurnamesForced() && surnamesEnabled;

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

		// Loading global translations
		helpPlayerNameVariable       = language.get("Commands.PlayerNameVariable");
		helpPartnerNameVariable      = language.get("Commands.PartnerNameVariable");
		messageNotFromConsole        = language.getMessage("NotFromConsole");
		messageNotANumber            = language.getMessage("Ingame.NaN");
		messageNoPermission          = language.getMessage("Ingame.NoPermission");
		messageNotMarried            = language.getMessage("Ingame.NotMarried");
		messagePartnerOffline        = language.getMessage("Ingame.PartnerOffline");
		messagePartnerNotInRange     = language.getMessage("Ingame.PartnerNotInRange");
		messagePlayerNotFound        = language.getMessage("Ingame.PlayerNotFound").replaceAll("\\{PlayerName}", "%s");
		messagePlayerNotMarried      = language.getMessage("Ingame.PlayerNotMarried").replaceAll("\\{PlayerName}", "%s");
		messagePlayerNotOnline       = language.getMessage("Ingame.PlayerNotOnline").replaceAll("\\{PlayerName}", "%s");
		messagePlayersNotMarried     = language.getMessage("Ingame.PlayersNotMarried");
		messageMoved                 = language.getMessage("Ingame.TP.Moved");
		messageDontMove              = language.getMessage("Ingame.TP.DontMove").replaceAll("\\{Time}", "%d");
		messageMarriageNotExact      = language.getMessage("Ingame.MarriageNotExact");
		messageTargetPartnerNotFound = language.getMessage("Ingame.TargetPartnerNotFound");

		commandManager = new CommandManagerImplementation(this);
		commandManager.init();
		marriageManager = new MarriageManager(this);
		prefixSuffixFormatter = new ChatPrefixSuffix(this);

		// Register Events
		getServer().getPluginManager().registerEvents(new JoinLeaveWorker(this), this);
		getServer().getPluginManager().registerEvents(new OpenRequestCloser(), this);
		if(config.isBonusXPEnabled())
		{
			if(config.getBonusXpMultiplier() > 1) getServer().getPluginManager().registerEvents(new BonusXP(this), this);
			if(config.isBonusXPSplitOnPickupEnabled()) getServer().getPluginManager().registerEvents(new BonusXpSplitOnPickup(this), this);
		}
		if(config.isHPRegainEnabled()) getServer().getPluginManager().registerEvents(new RegainHealth(this), this);
		if(config.isJoinLeaveInfoEnabled()) getServer().getPluginManager().registerEvents(new JoinLeaveInfo(this), this);
		if(config.isEconomyEnabled()) new EconomyHandler(this);
		if(config.isCommandExecutorEnabled()) getServer().getPluginManager().registerEvents(new CommandExecutor(this), this);

		placeholderManager = new PlaceholderManager(this);
		return true;
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


	@Override
	public boolean isRelease()
	{
		return getDescription().getVersion().contains("Release");
	}

	@Override
	public Updater createUpdater(final @NotNull UpdateProvider updateProvider)
	{
		return new at.pcgamingfreaks.Bukkit.Updater(this, this.getFile(), true, updateProvider);
	}

	public Config getConfiguration()
	{
		return config;
	}

	// API Stuff
	public static Version version()
	{
		return version;
	}

	public Version getVersion()
	{
		return version;
	}

	@Override
	public boolean areMultiplePartnersAllowed()
	{
		return multiMarriage;
	}

	@Override
	public boolean isSelfMarriageAllowed()
	{
		return selfMarriage;
	}

	@Override
	public boolean isSelfDivorceAllowed()
	{
		return selfDivorce;
	}

	@Override
	public boolean isSurnamesEnabled()
	{
		return surnamesEnabled;
	}

	@Override
	public boolean isSurnamesForced()
	{
		return surnamesForced;
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull UUID uuid)
	{
		return database.getPlayer(uuid);
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull MarriagePlayer getPlayerData(@NotNull String name)
	{
		return getPlayerData(getServer().getOfflinePlayer(name));
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
		return Utils.inRange(player1, player2, range, RANGE_LIMIT_PERM);
	}

	@Override
	public boolean isInRangeSquared(@NotNull Player player1, @NotNull Player player2, double rangeSquared)
	{
		return Utils.inRange(player1, player2, rangeSquared, RANGE_LIMIT_PERM);
	}

	@Override
	public void doDelayableTeleportAction(@NotNull final DelayableTeleportAction action)
	{
		//noinspection ConstantConditions
		if(action == null) return;
		if(action.getDelay() == 0 || action.getPlayer().hasPermission("marry.bypass.delay"))
		{
			action.run();
		}
		else
		{
			if(action.getPlayer().isOnline())
			{
				final Location p_loc = action.getPlayer().getLocation();
				final double p_hea = action.getPlayer().getHealth();
				messageDontMove.send(action.getPlayer(), action.getDelay()/20L);
				getServer().getScheduler().runTaskLater(this, () -> {
					if(action.getPlayer().isOnline())
					{
						//noinspection ConstantConditions
						if(p_hea <= action.getPlayer().getHealth() && p_loc.getX() == action.getPlayer().getLocation().getX() && p_loc.getY() == action.getPlayer().getLocation().getY() &&
								p_loc.getZ() == action.getPlayer().getLocation().getZ() && p_loc.getWorld().getName().equalsIgnoreCase(action.getPlayer().getLocation().getWorld().getName()))
						{
							action.run();
						}
						else
						{
							messageMoved.send(action.getPlayer());
						}
					}
				}, action.getDelay());
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
}