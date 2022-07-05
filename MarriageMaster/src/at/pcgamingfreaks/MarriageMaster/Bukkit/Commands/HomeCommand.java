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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.DelayableTeleportAction;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HomeDelEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HomeSetEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HomeTPEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageHome;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class HomeCommand extends MarryCommand
{
	private MarryCommand setHomeCommand, delHomeCommand;
	private final Message messagePlayerNoHome, messageNoHome, messageTPed;
	private final long delayTime;

	public HomeCommand(MarriageMaster plugin)
	{
		super(plugin, "home", plugin.getLanguage().getTranslated("Commands.Description.Home"), Permissions.HOME, false, true, plugin.getLanguage().getCommandAliases("Home"));

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
	public void afterRegister()
	{
		MarriageMaster plugin = (MarriageMaster) getMarriagePlugin();
		setHomeCommand = new SetHomeCommand(plugin, this);
		plugin.getCommandManager().registerSubCommand(setHomeCommand);
		delHomeCommand = new DelHomeCommand(plugin, this);
		plugin.getCommandManager().registerSubCommand(delHomeCommand);
	}

	@Override
	public void beforeUnregister()
	{
		MarriageMaster plugin = (MarriageMaster) getMarriagePlugin();
		plugin.getCommandManager().unRegisterSubCommand(setHomeCommand);
		setHomeCommand.close();
		plugin.getCommandManager().unRegisterSubCommand(delHomeCommand);
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
		if(player.isMarried() ||(sender.hasPermission(Permissions.HOME_OTHERS) &&
				((!getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1) || (getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2))))
		{
			Marriage marriage = getTargetedMarriage(sender, player, args);
			if(marriage != null)
			{
				if(!marriage.isHomeSet()) // no home set
				{
					if(sender.hasPermission(Permissions.HOME_OTHERS) && !marriage.getPartner1().equals(player) && !marriage.getPartner2().equals(player))
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
		if(sender.hasPermission(Permissions.HOME_OTHERS))
		{
			if(names == null)
			{
				names = new LinkedList<>();
			}
			String arg = args[args.length - 1].toLowerCase(Locale.ENGLISH);
			for(Player player : Bukkit.getOnlinePlayers())
			{
				if(!names.contains(player.getName()) && !sender.getName().equals(player.getName()) && player.getName().toLowerCase(Locale.ENGLISH).startsWith(arg))
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
		return super.canUse(sender) && (sender.hasPermission(Permissions.HOME_OTHERS) || getMarriagePlugin().getPlayerData((Player) sender).isMarried());
	}

	private @Nullable Marriage getTargetedMarriage(@NotNull CommandSender sender, @Nullable MarriagePlayer player, @NotNull String[] args)
	{
		Marriage marriage;
		if(getMarriagePlugin().areMultiplePartnersAllowed())
		{
			if(args.length == 2 && sender.hasPermission(Permissions.HOME_OTHERS))
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
			else if (player == null) return null;
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
			if(args.length == 1 && sender.hasPermission(Permissions.HOME_OTHERS))
			{
				MarriagePlayer target = getMarriagePlugin().getPlayerData(args[0]);
				if(target.isMarried())
				{
					marriage = target.getMarriageData();
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messagePlayerNotMarried.send(sender, target.getName());
					return null;
				}
			}
			else if (player != null)
			{
				marriage = player.getMarriageData();
			}
			else
			{
				return null;
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
					if(marriage.getHome().getLocation().getWorld() == null)
					{
						plugin.getLogger().info("Home world " + marriage.getHome().getWorldName() + " does not exist. Deleting home.");
						marriage.setHome(null);
						player.send(messageNoHome);
						return;
					}
					player.getPlayerOnline().teleport(marriage.getHome().getLocation());
					player.send(messageTPed);
				}
				else
				{
					//TODO tp player to other server
				}
			}
			else
			{
				player.send(messageNoHome);
			}
		}
	}

	@AllArgsConstructor
	private class TeleportHome implements DelayableTeleportAction
	{
		@Getter private final MarriagePlayer player;
		private final Marriage marriage;

		@Override
		public void run()
		{
			doTheTP(player, marriage);
		}

		@Override
		public long getDelay()
		{
			return delayTime;
		}
	}

	public class SetHomeCommand extends MarryCommand
	{
		private final @NotNull HomeCommand homeCommand;
		private final @NotNull Message messageSet;

		public SetHomeCommand(final @NotNull MarriageMaster plugin, final @NotNull HomeCommand homeCommand)
		{
			super(plugin, "sethome", plugin.getLanguage().getTranslated("Commands.Description.SetHome"), Permissions.HOME_SET, false, true, plugin.getLanguage().getCommandAliases("SetHome"));
			this.homeCommand = homeCommand;
			messageSet = plugin.getLanguage().getMessage("Ingame.Home.Set");
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
			if(player.isMarried() || (sender.hasPermission(Permissions.HOME_OTHERS) &&
					((!getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1) || (getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2))))
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
			return super.canUse(sender) && (sender.hasPermission(Permissions.HOME_OTHERS) || getMarriagePlugin().getPlayerData((Player) sender).isMarried());
		}
	}

	public class DelHomeCommand extends MarryCommand
	{
		private final @NotNull HomeCommand homeCommand;
		private final @NotNull Message messageHomeDeleted;

		public DelHomeCommand(final @NotNull MarriageMaster plugin, final @NotNull HomeCommand homeCommand)
		{
			super(plugin, "delhome", plugin.getLanguage().getTranslated("Commands.Description.DelHome"), Permissions.HOME_DEL, false, true, plugin.getLanguage().getCommandAliases("DelHome"));
			this.homeCommand = homeCommand;
			messageHomeDeleted = plugin.getLanguage().getMessage("Ingame.Home.Deleted");
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			if(!(sender instanceof Player))
			{
				if((getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2) || (!getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1))
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
				if(player.isMarried() || (sender.hasPermission(Permissions.HOME_OTHERS) && ((!getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1) || (getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2))))
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
			return super.canUse(sender) && (sender.hasPermission(Permissions.HOME_OTHERS) || getMarriagePlugin().getPlayerData((Player) sender).isMarried());
		}
	}
}