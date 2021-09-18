/*
 *   Copyright (C) 2020 GeorgH93
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

import at.pcgamingfreaks.Bukkit.ManagedUpdater;
import at.pcgamingfreaks.Bukkit.Message.Message;
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
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.NoDatabaseWorker;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Version;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarriageMaster extends JavaPlugin implements MarriageMasterPlugin
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
	private MarriageManager marriageManager = null;
	@Getter private PlaceholderManager placeholderManager = null;

	// Global Settings
	@Getter private boolean selfMarriageAllowed = false, selfDivorceAllowed = false, surnamesEnabled = false, surnamesForced = false;
	private boolean multiplePartnersAllowed = false;

	// Global Translations
	public String helpPartnerNameVariable, helpPlayerNameVariable;
	public Message messageNotANumber, messageNoPermission, messageNotFromConsole, messageNotMarried, messagePartnerOffline, messagePartnerNotInRange, messageTargetPartnerNotFound,
					messagePlayerNotFound, messagePlayerNotMarried, messagePlayerNotOnline, messagePlayersNotMarried, messageMoved, messageDontMove, messageMarriageNotExact;

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

	private void failedToEnablePlugin()
	{
		getLogger().info(ConsoleColor.RED + "Failed to enable the plugin!" + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
		this.setEnabled(false);
		instance = null;
	}

	@Override
	public void onDisable()
	{
		if(config != null && config.isLoaded() && database != null)
		{
			if(config.useUpdater()) updater.update(); // Check for updates
			unload();
		}
		if(placeholderManager != null) placeholderManager.close();
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

		// Loading data
		surnamesEnabled = config.isSurnamesEnabled();
		multiplePartnersAllowed = config.areMultiplePartnersAllowed();
		selfMarriageAllowed = config.isSelfMarriageAllowed();
		selfDivorceAllowed = config.isSelfDivorceAllowed();
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
		prefixSuffixFormatter = new PrefixSuffixFormatterImpl(this);

		// Register Events
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
		return multiplePartnersAllowed;
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
		//noinspection ConstantConditions
		if(action == null) return;
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
				messageDontMove.send(player, action.getDelay()/20L);
				if(playerData.getDelayedTpTask() != null) playerData.getDelayedTpTask().cancel();
				playerData.setDelayedTpTask(getServer().getScheduler().runTaskLater(this, () -> {
					playerData.setDelayedTpTask(null);
					if(action.getPlayer().isOnline())
					{
						//noinspection ConstantConditions
						if(oldHealth <= player.getHealth() && oldLocation.getX() == player.getLocation().getX() && oldLocation.getY() == player.getLocation().getY() &&
								oldLocation.getZ() == player.getLocation().getZ() && oldLocation.getWorld().getName().equalsIgnoreCase(player.getLocation().getWorld().getName()))
						{
							action.run();
						}
						else
						{
							messageMoved.send(player);
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