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
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HomeDelEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HomeSetEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HomeTPEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageHome;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class HomeCommand extends MarryCommand
{
	private MarryCommand setHomeCommand, delHomeCommand;
	private final Message messagePlayerNoHome, messageNoHome, messageTPed;
	private final long delayTime;

	public HomeCommand(MarriageMaster plugin)
	{
		super(plugin, "home", plugin.getLanguage().getTranslated("Commands.Description.Home"), "marry.home", false, true, plugin.getLanguage().getCommandAliases("Home"));

		messagePlayerNoHome = plugin.getLanguage().getMessage("Ingame.Home.PlayerNoHome");
		messageNoHome       = plugin.getLanguage().getMessage("Ingame.Home.NoHome");
		messageTPed         = plugin.getLanguage().getMessage("Ingame.Home.TPed");

		delayTime = plugin.getConfiguration().getTPDelayTime() * 20L;

		if(plugin.getPluginChannelCommunicator() != null)
		{
			plugin.getPluginChannelCommunicator().setHomeCommand(this);
		}
	}

	@Override
	public void registerSubCommands()
	{
		setHomeCommand = new SetHomeCommand(((MarriageMaster) getMarriagePlugin()), this);
		getMarriagePlugin().getCommandManager().registerMarryCommand(setHomeCommand);
		delHomeCommand = new DelHomeCommand(((MarriageMaster) getMarriagePlugin()), this);
		getMarriagePlugin().getCommandManager().registerMarryCommand(delHomeCommand);
	}

	@Override
	public void unRegisterSubCommands()
	{
		getMarriagePlugin().getCommandManager().unRegisterMarryCommand(setHomeCommand);
		setHomeCommand.close();
		getMarriagePlugin().getCommandManager().unRegisterMarryCommand(delHomeCommand);
		delHomeCommand.close();
	}

	@Override
	public void close()
	{
		if(setHomeCommand != null)
		{
			setHomeCommand.close();
			setHomeCommand = null;
		}
		if(delHomeCommand != null)
		{
			delHomeCommand.close();
			delHomeCommand = null;
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(player.isMarried() ||(sender.hasPermission("marry.home.others") &&
				((!getMarriagePlugin().isPolygamyAllowed() && args.length == 1) || (getMarriagePlugin().isPolygamyAllowed() && args.length == 2))))
		{
			Marriage marriage = getTargetedMarriage(sender, player, args);
			if(marriage != null)
			{
				if(!marriage.isHomeSet()) // no home set
				{
					if(sender.hasPermission("marry.home.others") && !marriage.getPartner1().equals(player) && !marriage.getPartner2().equals(player))
					{
						messagePlayerNoHome.send(sender);
					}
					else
					{
						messageNoHome.send(sender);
					}
				}
				else
				{
					HomeTPEvent event = new HomeTPEvent(player, marriage);
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled())
					{
						getMarriagePlugin().doDelayableTeleportAction(new TeleportHome(player, marriage));
					}
				}
			}
		}
		else
		{
			((MarriageMaster) getMarriagePlugin()).messageNotMarried.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		List<String> names = getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
		if(sender.hasPermission("marry.home.others"))
		{
			if(names == null)
			{
				names = new LinkedList<>();
			}
			String arg = args[args.length - 1].toLowerCase();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				if(!names.contains(player.getName()) && !sender.getName().equals(player.getName()) && player.getName().toLowerCase().startsWith(arg))
				{
					names.add(player.getName());
				}
			}
			if(names.isEmpty())
			{
				names = null;
			}
		}
		return names;
	}

	@Override
	public boolean canUse(@NotNull CommandSender sender)
	{
		return super.canUse(sender) && (sender.hasPermission("marry.home.others") || getMarriagePlugin().getPlayerData((Player) sender).isMarried());
	}

	private Marriage getTargetedMarriage(CommandSender sender, MarriagePlayer player, String[] args)
	{
		Marriage marriage;
		if(getMarriagePlugin().isPolygamyAllowed())
		{
			if(args.length == 2 && sender.hasPermission("marry.home.others"))
			{
				MarriagePlayer target1 = getMarriagePlugin().getPlayerData(args[0]), target2 = getMarriagePlugin().getPlayerData(args[1]);
				if(target1.isMarried() && target2.isMarried() && target1.isPartner(target2))
				{
					marriage = target1.getMarriageData(target2);
				}
				else
				{
					if(!target1.isMarried() || !target2.isMarried())
					{
						MarriagePlayer t = (!target1.isMarried()) ? target1 : target2;
						((MarriageMaster) getMarriagePlugin()).messagePlayerNotMarried.send(sender, t.getName());
					}
					else
					{
						((MarriageMaster) getMarriagePlugin()).messagePlayersNotMarried.send(sender);
					}
					return null;
				}
			}
			else if(args.length == 1)
			{
				MarriagePlayer partner = getMarriagePlugin().getPlayerData(args[0]);
				if(!player.isPartner(partner))
				{
					((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
					return null;
				}
				marriage = player.getMarriageData(partner);
			}
			else
			{
				marriage = player.getMarriageData();
			}
		}
		else
		{
			if(args.length == 1 && sender.hasPermission("marry.home.others"))
			{
				MarriagePlayer target = getMarriagePlugin().getPlayerData(args[0]);
				if(target.isMarried())
				{
					marriage = target.getMarriageData();
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messagePlayerNotMarried.send(sender, target.getPlayer().getName());
					return null;
				}
			}
			else
			{
				marriage = player.getMarriageData();
			}
		}
		return marriage;
	}

	public void doTheTP(MarriagePlayer player, Marriage marriage)
	{
		if(player.isOnline())
		{
			if(marriage.getHome() != null)
			{
				if(marriage.getHome().isOnThisServer())
				{
					player.getPlayer().getPlayer().teleport(marriage.getHome().getLocation());
					messageTPed.send(player.getPlayer().getPlayer());
				}
				else
				{
					//TODO tp player to other server
				}
			}
			else
			{
				messageNoHome.send(player.getPlayer().getPlayer());
			}
		}
	}

	private class TeleportHome implements DelayableTeleportAction
	{
		private MarriagePlayer player;
		private Marriage marriage;

		public TeleportHome(MarriagePlayer player, Marriage marriage)
		{
			this.player = player;
			this.marriage = marriage;
		}

		@Override
		public void run()
		{
			doTheTP(player, marriage);
		}

		@Override
		public @NotNull Player getPlayer()
		{
			return player.getPlayer().getPlayer();
		}

		@Override
		public long getDelay()
		{
			return delayTime;
		}
	}

	public class SetHomeCommand extends MarryCommand
	{
		private HomeCommand homeCommand;
		private Message messageSet;

		public SetHomeCommand(MarriageMaster plugin, HomeCommand homeCommand)
		{
			super(plugin, "sethome", plugin.getLanguage().getTranslated("Commands.Description.SetHome"), "marry.home", false, true, plugin.getLanguage().getCommandAliases("SetHome"));
			this.homeCommand = homeCommand;
			messageSet = plugin.getLanguage().getMessage("Ingame.Home.Set");
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
			if(player.isMarried() || (sender.hasPermission("marry.home.others") &&
					((!getMarriagePlugin().isPolygamyAllowed() && args.length == 1) || (getMarriagePlugin().isPolygamyAllowed() && args.length == 2))))
			{
				Marriage marriage = getTargetedMarriage(sender, player, args);
				if(marriage != null)
				{
					HomeSetEvent event = new HomeSetEvent(player, marriage, ((Player) sender).getLocation());
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled())
					{
						marriage.setHome(new MarriageHome(event.getNewHomeLocation()));
						messageSet.send(sender);
					}
				}
			}
			else
			{
				((MarriageMaster) getMarriagePlugin()).messageNotMarried.send(sender);
			}
		}

		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			return homeCommand.tabComplete(sender, mainCommandAlias, alias, args);
		}

		@Override
		public boolean canUse(@NotNull CommandSender sender)
		{
			return super.canUse(sender) && (sender.hasPermission("marry.home.others") || getMarriagePlugin().getPlayerData((Player) sender).isMarried());
		}
	}

	public class DelHomeCommand extends MarryCommand
	{
		private HomeCommand homeCommand;
		private Message messageHomeDeleted;

		public DelHomeCommand(MarriageMaster plugin, HomeCommand homeCommand)
		{
			super(plugin, "delhome", plugin.getLanguage().getTranslated("Commands.Description.DelHome"), "marry.home", false, true, plugin.getLanguage().getCommandAliases("DelHome"));
			this.homeCommand = homeCommand;
			messageHomeDeleted = plugin.getLanguage().getMessage("Ingame.Home.Deleted");
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			if(!(sender instanceof Player))
			{
				if((getMarriagePlugin().isPolygamyAllowed() && args.length == 2) || (!getMarriagePlugin().isPolygamyAllowed() && args.length == 1))
				{
					Marriage marriageData = homeCommand.getTargetedMarriage(sender, null, args);
					if(marriageData != null)
					{
						marriageData.setHome(null);
						messageHomeDeleted.send(sender);
					}
				}
				else
				{
					showHelp(sender, mainCommandAlias);
				}
			}
			else
			{
				MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
				if(player.isMarried() || (sender.hasPermission("marry.home.others") && ((!getMarriagePlugin().isPolygamyAllowed() && args.length == 1) || (getMarriagePlugin().isPolygamyAllowed() && args.length == 2))))
				{
					Marriage marriage = getTargetedMarriage(sender, player, args);
					if(marriage != null)
					{
						HomeDelEvent event = new HomeDelEvent(player, marriage);
						Bukkit.getPluginManager().callEvent(event);
						if(!event.isCancelled())
						{
							marriage.setHome(null);
							messageHomeDeleted.send(sender);
						}
					}
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messageNotMarried.send(sender);
				}
			}
		}

		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			return homeCommand.tabComplete(sender, mainCommandAlias, alias, args);
		}

		@Override
		public boolean canUse(@NotNull CommandSender sender)
		{
			return super.canUse(sender) && (sender.hasPermission("marry.home.others") || getMarriagePlugin().getPlayerData((Player) sender).isMarried());
		}
	}
}