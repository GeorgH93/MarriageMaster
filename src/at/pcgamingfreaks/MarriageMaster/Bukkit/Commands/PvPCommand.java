/*
 *   Copyright (C) 2016 GeorgH93
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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.API.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PvPCommand extends MarryCommand
{
	private final Message messagePvPOn, messagePvPOff, messagePvPIsOff;
	private final String helpMulti, helpOn, helpOff;
	private MarryCommand onCommand, offCommand;

	public PvPCommand(MarriageMaster plugin)
	{
		super(plugin, "pvp", plugin.getLanguage().getTranslated("Commands.Description.PvP"), "marry.pvp", true, true, plugin.getLanguage().getCommandAliases("PvP"));

		messagePvPOn    = plugin.getLanguage().getMessage("Ingame.PvP.On");
		messagePvPOff   = plugin.getLanguage().getMessage("Ingame.PvP.Off");
		messagePvPIsOff = plugin.getLanguage().getMessage("Ingame.PvP.IsDisabled");

		helpMulti = "<" + plugin.helpPartnerNameVariable + "> <" + plugin.getCommandManager().getOnSwitchTranslation() + " / " +
				plugin.getCommandManager().getOffSwitchTranslation() + " / " + plugin.getCommandManager().getToggleSwitchTranslation() + ">";
		helpOn    = plugin.getCommandManager().getOnSwitchTranslation();
		helpOff   = plugin.getCommandManager().getOffSwitchTranslation();
	}

	@Override
	public void registerSubCommands()
	{
		onCommand = new PvPOnCommand(((MarriageMaster) getMarriagePlugin()));
		getMarriagePlugin().getCommandManager().registerMarryCommand(onCommand);
		offCommand = new PvPOffCommand(((MarriageMaster) getMarriagePlugin()));
		getMarriagePlugin().getCommandManager().registerMarryCommand(offCommand);
	}

	@Override
	public void unRegisterSubCommands()
	{
		getMarriagePlugin().getCommandManager().unRegisterMarryCommand(onCommand);
		onCommand.close();
		getMarriagePlugin().getCommandManager().unRegisterMarryCommand(offCommand);
		offCommand.close();
	}

	@Override
	public void close()
	{
		if(onCommand != null)
		{
			onCommand.close();
			onCommand = null;
		}
		if(offCommand != null)
		{
			offCommand.close();
			offCommand = null;
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(args.length < 1)
		{
			showHelp(sender, mainCommandAlias);
			return;
		}
		Marriage marriage;
		if(getMarriagePlugin().isPolygamyAllowed() && args.length == 2)
		{
			marriage = player.getMarriageData(getMarriagePlugin().getPlayerData(args[0]));
			if(marriage == null)
			{
				((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
				return;
			}
		}
		else
		{
			marriage = player.getNearestPartnerMarriageData();
		}
		if(getMarriagePlugin().getCommandManager().isOnSwitch(args[args.length - 1]))
		{
			//noinspection ConstantConditions
			marriage.setPVPEnabled(true);
			messagePvPOn.send(sender);
		}
		else if(getMarriagePlugin().getCommandManager().isOffSwitch(args[args.length - 1]))
		{
			//noinspection ConstantConditions
			marriage.setPVPEnabled(false);
			messagePvPOff.send(sender);
		}
		else
		{
			if(getMarriagePlugin().getCommandManager().isToggleSwitch(args[args.length - 1]))
			{
				//noinspection ConstantConditions
				if(marriage.isPVPEnabled())
				{
					marriage.setPVPEnabled(false);
					messagePvPOff.send(sender);
				}
				else
				{
					marriage.setPVPEnabled(true);
					messagePvPOn.send(sender);
				}
			}
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(getMarriagePlugin().isPolygamyAllowed() && args.length == 1)
		{
			return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
		}
		else
		{
			return null;
		}
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) requester);
		if(player.isMarried())
		{
			List<HelpData> help = new LinkedList<>();
			if(player.getPartners().size() > 1)
			{
				help.add(new HelpData(getTranslatedName(), helpMulti, getDescription()));
			}
			else
			{
				//noinspection ConstantConditions
				help.add(new HelpData(getTranslatedName() + (player.getMarriageData().isPVPEnabled() ? helpOff : helpOn) , "", getDescription()));
			}
			return help;
		}
		return null;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event)
	{
		if(event.getEntity() instanceof Player && (event.getDamager() instanceof Player || (event.getDamager() instanceof Arrow && ((Arrow)event.getDamager()).getShooter() instanceof Player)))
		{
			Player bPlayer = (Player)((event.getDamager() instanceof Player) ? event.getDamager() : ((Arrow)event.getDamager()).getShooter());
			MarriagePlayer player = getMarriagePlugin().getPlayerData(bPlayer);
			Marriage marriage = player.getMarriageData(getMarriagePlugin().getPlayerData((Player) event.getEntity()));
			if(marriage != null && !marriage.isPVPEnabled())
			{
				messagePvPIsOff.send(bPlayer);
				event.setCancelled(true);
			}
		}
	}

	private abstract class PvPSubCommand extends MarryCommand
	{
		public PvPSubCommand(JavaPlugin plugin, String name, String description, String permission, boolean mustBeMarried, boolean partnerSelectorInHelpForMoreThanOnePartner, String... aliases)
		{
			super(plugin, name, description, permission, mustBeMarried, partnerSelectorInHelpForMoreThanOnePartner, aliases);
		}

		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
		}

		@Override
		public List<HelpData> getHelp(@NotNull CommandSender requester)
		{
			return null;
		}

		protected Marriage getMarriage(Player sender, String[] args)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData(sender);
			Marriage marriage;
			if(getMarriagePlugin().isPolygamyAllowed() && args.length == 1)
			{
				marriage = player.getMarriageData(getMarriagePlugin().getPlayerData(args[0]));
				if(marriage == null)
				{
					((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
					return null;
				}
			}
			else
			{
				marriage = player.getNearestPartnerMarriageData();
			}
			return marriage;
		}
	}

	private class PvPOnCommand extends PvPSubCommand
	{
		public PvPOnCommand(MarriageMaster plugin)
		{
			super(plugin, "pvpon", plugin.getLanguage().getTranslated("Commands.Description.PvPOn"), "marry.pvp", true, true, plugin.getLanguage().getCommandAliases("PvPOn"));
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			Marriage marriage = getMarriage((Player) sender, args);
			if(marriage != null)
			{
				marriage.setPVPEnabled(true);
				messagePvPOn.send(sender);
			}
		}
	}

	private class PvPOffCommand extends PvPSubCommand
	{
		public PvPOffCommand(MarriageMaster plugin)
		{
			super(plugin, "pvpoff", plugin.getLanguage().getTranslated("Commands.Description.PvPOff"), "marry.pvp", true, true, plugin.getLanguage().getCommandAliases("PvPOff"));
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			Marriage marriage = getMarriage((Player) sender, args);
			if(marriage != null)
			{
				marriage.setPVPEnabled(false);
				messagePvPOff.send(sender);
			}
		}
	}
}