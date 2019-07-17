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

package at.pcgamingfreaks.MarriageMaster.Bungee;

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
import at.pcgamingfreaks.MarriageMaster.IUpdater;
import at.pcgamingfreaks.Updater.UpdateProviders.UpdateProvider;
import at.pcgamingfreaks.Updater.Updater;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.UUID;

public class MarriageMaster extends Plugin implements MarriageMasterPlugin, IUpdater
{
	@Getter @Setter(AccessLevel.PRIVATE) private static MarriageMaster instance = null;

	private Config config = null;
	@Getter private Language language = null;
	private Database DB = null;
	@Getter private PluginChannelCommunicator pluginChannelCommunicator = null;
	private CommandManagerImplementation commandManager = new CommandManagerImplementation(this);

	// Global Settings
	private boolean multiMarriage = false, selfMarriage = false, selfDivorce = false, surnamesEnabled = false, surnamesForced = false;


	// Global Translations
	public Message messageNoPermission, messageNotMarried, messagePartnerOffline, messageNotFromConsole, messageTargetPartnerNotFound, messagePlayerNotMarried, messagePlayersNotMarried;
	public String helpPartnerNameVariable;

	@Override
	public void onEnable()
	{
		setInstance(this);
		config = new Config(this);
		language = new Language(this);
		if(!config.isLoaded())
		{
			getLogger().info(ConsoleColor.RED + "Failed to enable plugin! " + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
			return;
		}
		if(config.useUpdater()) update(null); // Check for updates
		if(load()) // Load Plugin
		{
			getLogger().info(ConsoleColor.GREEN + "Marriage Master has been enabled! " + ConsoleColor.YELLOW + " :) " + ConsoleColor.RESET);
		}
		else
		{
			getLogger().info(ConsoleColor.RED + "Failed to enable plugin! " + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
		}
	}

	@Override
	public void onDisable()
	{
		if(config != null && config.isLoaded() && DB != null)
		{
			if(config.useUpdater()) update(null); // Check for updates
			unload();
		}
		getLogger().info(ConsoleColor.RED + "Marriage Master has been disabled. " + ConsoleColor.YELLOW + " :( " + ConsoleColor.RESET);
	}

	private boolean load()
	{
		// Loading base Data
		if(!config.isLoaded() || !language.load(config.getLanguage(), config.getLanguageUpdateMode()))
		{
			// If we ever reach this code there must be a serious problem, someone probably has put an outdated version of one of our libs into his problem.
			getLogger().warning(ConsoleColor.RED + "A critical error occurred! The plugin failed to load!" + ConsoleColor.RESET);
			return false;
		}
		DB = new Database(this);
		if(!DB.available())
		{
			getLogger().warning(ConsoleColor.RED + "Failed to connect to database! Please adjust your settings and retry!" + ConsoleColor.RESET);
			new NoDatabaseWorker(this); // Starts the worker that informs everyone with reload permission that the database connection failed.
			return false;
		}
		// Load data
		surnamesEnabled = config.isSurnamesEnabled();
		multiMarriage   = config.areMultiplePartnersAllowed();
		selfMarriage    = config.isSelfMarriageAllowed();
		surnamesForced  = config.isSurnamesForced() && surnamesEnabled;

		// Load global translations
		helpPartnerNameVariable      = language.get("Commands.PartnerNameVariable");
		messageNotFromConsole        = language.getMessage("NotFromConsole");
		messageNotMarried            = language.getMessage("Ingame.NotMarried");
		messageNoPermission          = language.getMessage("Ingame.NoPermission");
		messagePartnerOffline        = language.getMessage("Ingame.PartnerOffline");
		messageTargetPartnerNotFound = language.getMessage("Ingame.TargetPartnerNotFound");
		messagePlayerNotMarried      = language.getMessage("Ingame.PlayerNotMarried").replaceAll("\\{PlayerName\\}", "%s");
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
		DB.close();
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
		return DB;
	}

	@Override
	public Updater createUpdater(UpdateProvider updateProvider)
	{
		return new at.pcgamingfreaks.Bungee.Updater(this, true, updateProvider);
	}

	@Override
	public boolean isRelease()
	{
		return getDescription().getVersion().contains("Release");
	}

	//region API Stuff
	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull ProxiedPlayer proxiedPlayer)
	{
		return DB.getPlayer(proxiedPlayer.getUniqueId());
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull UUID uuid)
	{
		return DB.getPlayer(uuid);
	}

	@Override
	public @NotNull MarriagePlayer getPlayerData(@NotNull String name)
	{
		return null;
	}

	@Override
	public @NotNull Collection<? extends Marriage> getMarriages()
	{
		return DB.getCache().getLoadedMarriages();
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
	public void doDelayableTeleportAction(@NotNull DelayableTeleportAction action)
	{

	}

	@Override
	public @NotNull CommandManagerImplementation getCommandManager()
	{
		return commandManager;
	}
	//endregion
}