/*
 *   Copyright (C) 2022 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee;

import at.pcgamingfreaks.Bungee.ManagedUpdater;
import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.DelayableTeleportAction;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriageMasterPlugin;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.Commands.CommandManagerImplementation;
import at.pcgamingfreaks.MarriageMaster.Bungee.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bungee.Database.Database;
import at.pcgamingfreaks.MarriageMaster.Bungee.Database.Language;
import at.pcgamingfreaks.MarriageMaster.Bungee.Listener.JoinLeaveInfo;
import at.pcgamingfreaks.MarriageMaster.Bungee.Listener.PluginChannelCommunicator;
import at.pcgamingfreaks.MarriageMaster.Bungee.SpecialInfoWorker.NoDatabaseWorker;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;
import at.pcgamingfreaks.Plugin.IPlugin;
import at.pcgamingfreaks.Version;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarriageMaster extends Plugin implements MarriageMasterPlugin, IPlugin
{
	@Getter @Setter(AccessLevel.PRIVATE) private static MarriageMaster instance = null;

	@Getter private ManagedUpdater updater;
	private Config config = null;
	@Getter private Language language = null;
	private Database database = null;
	@Getter private PluginChannelCommunicator pluginChannelCommunicator = null;
	private final CommandManagerImplementation commandManager = new CommandManagerImplementation(this);

	// Global Settings
	private boolean multiMarriage = false;
	@Getter private boolean selfMarriageAllowed = false, surnamesEnabled = false, surnamesForced = false;


	// Global Translations
	public Message messageNoPermission, messageNotMarried, messagePartnerOffline, messageNotFromConsole, messageTargetPartnerNotFound, messagePlayerNotMarried, messagePlayersNotMarried;
	public String helpPartnerNameVariable;

	@Override
	public void onEnable()
	{
		updater = new ManagedUpdater(this);
		setInstance(this);
		config = new Config(this);
		language = new Language(this);
		if(!config.isLoaded())
		{
			getLogger().info(ConsoleColor.RED + "Failed to enable the plugin! " + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
			return;
		}
		updater.setChannel(config.getUpdateChannel());
		if(config.useUpdater()) updater.update(); // Check for updates
		if(load()) // Load Plugin
		{
			getLogger().info(ConsoleColor.GREEN + "Marriage Master has been enabled! " + ConsoleColor.YELLOW + " :) " + ConsoleColor.RESET);
		}
		else
		{
			getLogger().info(ConsoleColor.RED + "Failed to enable the plugin! " + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
		}
	}

	@Override
	public void onDisable()
	{
		if(config != null && config.isLoaded() && database != null)
		{
			if(config.useUpdater()) updater.update(); // Check for updates
			unload();
		}
		updater.waitForAsyncOperation(); // Wait for updates to finish
		getLogger().info(ConsoleColor.RED + "Marriage Master has been disabled. " + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
	}

	private boolean load()
	{
		// Loading base Data
		if(!config.isLoaded() || !language.load(config))
		{
			// If we ever reach this code there must be a serious problem, someone probably has put an outdated version of one of our libs into his problem.
			getLogger().warning(ConsoleColor.RED + "A critical error occurred! The plugin failed to load!" + ConsoleColor.RESET);
			return false;
		}
		updater.setChannel(config.getUpdateChannel());
		database = new Database(this);
		if(!database.available())
		{
			getLogger().warning(ConsoleColor.RED + "Failed to connect to database! Please adjust your settings and retry!" + ConsoleColor.RESET);
			new NoDatabaseWorker(this); // Starts the worker that informs everyone with reload permission that the database connection failed.
			return false;
		}
		// Load data
		surnamesEnabled     = config.isSurnamesEnabled();
		multiMarriage       = config.areMultiplePartnersAllowed();
		selfMarriageAllowed = config.isSelfMarriageAllowed();
		surnamesForced      = config.isSurnamesForced() && surnamesEnabled;

		// Load global translations
		helpPartnerNameVariable      = language.get("Commands.PartnerNameVariable");
		messageNotFromConsole        = language.getMessage("NotFromConsole");
		messageNotMarried            = language.getMessage("Ingame.NotMarried");
		messageNoPermission          = language.getMessage("Ingame.NoPermission");
		messagePartnerOffline        = language.getMessage("Ingame.PartnerOffline");
		messageTargetPartnerNotFound = language.getMessage("Ingame.TargetPartnerNotFound");
		messagePlayerNotMarried      = language.getMessage("Ingame.PlayerNotMarried").replaceAll("\\{PlayerName}", "%s");
		messagePlayersNotMarried     = language.getMessage("Ingame.PlayersNotMarried");

		// Register Events
		pluginChannelCommunicator = new PluginChannelCommunicator(this);
		if(config.isJoinLeaveInfoEnabled()) getProxy().getPluginManager().registerListener(this, new JoinLeaveInfo(this));

		commandManager.init();

		return true;
	}

	private void unload()
	{
		commandManager.close();
		getProxy().getPluginManager().unregisterCommands(this);
		getProxy().getPluginManager().unregisterListeners(this);
		database.close();
		pluginChannelCommunicator.close();
		pluginChannelCommunicator = null;
	}

	public void reload()
	{
		unload();
		config.reload();
		load();
		//getProxy().getPluginManager().callEvent(new MarriageMasterReloadEvent());
	}

	public Config getConfig()
	{
		return config;
	}

	public Database getDatabase()
	{
		return database;
	}

	//region API Stuff
	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull ProxiedPlayer proxiedPlayer)
	{
		return database.getPlayer(proxiedPlayer.getUniqueId());
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull UUID uuid)
	{
		return database.getPlayer(uuid);
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull String name)
	{
		ProxiedPlayer player = getProxy().getPlayer(name);
		if(player == null) throw new RuntimeException("BungeeCord does not provide player objects for offline players!"); //TODO return correct player if the player is in the cache
		return getPlayerData(player);
	}

	@Override
	public @NotNull Collection<? extends Marriage> getMarriages()
	{
		return database.getCache().getLoadedMarriages();
	}

	@Override
	public boolean areMultiplePartnersAllowed()
	{
		return multiMarriage;
	}

	@Override
	public boolean isSelfDivorceAllowed()
	{
		return false;
	}

	@Override
	public void doDelayableTeleportAction(@NotNull DelayableTeleportAction action)
	{
		//TODO
	}

	@Override
	public @NotNull CommandManagerImplementation getCommandManager()
	{
		return commandManager;
	}

	@Override
	public @NotNull Collection<? extends MarriagePlayer> getPriestsOnline()
	{
		ArrayList<MarriagePlayer> priests = new ArrayList<>();
		for(ProxiedPlayer player : getProxy().getPlayers())
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

	@Override
	public @NotNull Version getVersion()
	{
		return new Version(getDescription().getVersion());
	}

	@Override
	public @NotNull String getName()
	{
		return getDescription().getName();
	}

	//endregion
}