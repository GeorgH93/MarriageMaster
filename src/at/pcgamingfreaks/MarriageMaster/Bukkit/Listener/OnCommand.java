/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.Priest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Databases.Database;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Marry_Requests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class OnCommand implements CommandExecutor
{
	private MarriageMaster plugin;

	private Priest priest;

	public OnCommand(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;

		priest = new Priest(plugin);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String arg, String[] args)
	{
		final Player player;
		if(sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			if(args.length == 0)
			{
				ShowAvailableCmds(sender);
			}
			else
			{
				switch(args[0].toLowerCase())
				{
					case "reload":
						plugin.reload();
						sender.sendMessage(ChatColor.BLUE + "Reloaded");
						break;
					case "list":
						plugin.DB.GetAllMarriedPlayers(new Database.Callback<TreeMap<String, String>>()
						{
							@Override
							public void onResult(TreeMap<String, String> map)
							{
								if(map.size() > 0)
								{
									sender.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ListHL"));
									for(Map.Entry<String, String> item : map.entrySet())
									{
										sender.sendMessage(ChatColor.GREEN + item.getKey() + " + " + item.getValue());
									}
								}
								else
								{
									sender.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoMarriedPlayers"));
								}
							}
						});
						break;
					case "update":
						if(plugin.config.UseUpdater())
						{
							plugin.update();
						}
						break;
					case "surname":
						if(plugin.config.getSurname())
						{
							if(args.length == 3)
							{
								priest.SetSurname(sender, args[1], args[2]);
							}
							else
							{
								sender.sendMessage("/marry surname <Playername> <Surname>");
							}
						}
						break;
					default:
						if(args.length == 2)
						{
							if(args[0].equalsIgnoreCase(plugin.config.GetPriestCMD()))
							{
								priest.setPriest(args, sender);
							}
							else if(args[0].equalsIgnoreCase("divorce"))
							{
								priest.Divorce(sender, args);
							}
							else
							{
								priest.Marry(sender, args);
							}
						}
						else if(args.length == 3 && plugin.config.getSurname())
						{
							priest.Marry(sender, args);
						}
						else
						{
							ShowAvailableCmds(sender);
						}
						break;
				}
			}
			return true;
		}

		if(args.length == 0 || (args.length > 3 && !(args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("chat"))))
		{
			ShowAvailableCmds(player);
			return true;
		}
		switch(args[0].toLowerCase())
		{
			case "reload":
				if(plugin.CheckPerm(player, "marry.reload", false))
				{
					plugin.reload();
					player.sendMessage(ChatColor.BLUE + "Reloaded");
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "list":
				if(plugin.CheckPerm(player, "marry.list"))
				{
					int page = 0;
					if(args.length == 2)
					{
						try
						{
							page = Integer.parseInt(args[1]) - 1;
							if(page < 0)
							{
								page = 0;
							}
						}
						catch(Exception e)
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NaN"));
							return true;
						}
					}
					final int showPage = page;
					plugin.DB.GetAllMarriedPlayers(new Database.Callback<TreeMap<String, String>>()
					{
						@Override
						public void onResult(TreeMap<String, String> map)
						{
							int page = showPage;
							if(map.size() > 0)
							{
								int avpages = (int) Math.ceil(map.size() / 7.0);
								if(page >= avpages)
								{
									page = avpages - 1;
								}
								player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ListHL") + ((avpages > 1) ? (ChatColor.WHITE + " - " + ChatColor.GREEN + String.format(plugin.lang.get("Ingame.ListHLMP"), page + 1, avpages)) : ""));
								int c = 7, c2 = page * 7;
								for(Map.Entry<String, String> item : map.entrySet())
								{
									if(c2 > 0)
									{
										c2--;
									}
									else
									{
										player.sendMessage(ChatColor.GREEN + item.getKey() + ChatColor.RED + " + " + ChatColor.GREEN + item.getValue());
										c--;
										if(c <= 0)
										{
											return;
										}
									}
								}
							}
							else
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoMarriedPlayers"));
							}
						}
					});
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "pvpon":
				if(plugin.config.GetAllowBlockPvP())
				{
					if(plugin.HasPartner(player))
					{
						if(plugin.CheckPerm(player, "marry.pvpon"))
						{
							plugin.DB.SetPvPEnabled(player, true);
							player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.PvPOn"));
						}
						else
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "pvpoff":
				if(plugin.config.GetAllowBlockPvP())
				{
					if(plugin.HasPartner(player))
					{
						if(plugin.CheckPerm(player, "marry.pvpoff"))
						{
							plugin.DB.SetPvPEnabled(player, false);
							player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.PvPOff"));
						}
						else
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "home":
				if(plugin.CheckPerm(player, "marry.home"))
				{
					if(args.length == 2 && plugin.CheckPerm(player, "marry.home.others", false))
					{
						plugin.home.TPAdmin(player, args[1]);
					}
					else if(plugin.HasPartner(player))
					{
						plugin.home.TP(player);
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "sethome":
				if(plugin.CheckPerm(player, "marry.home"))
				{
					if(plugin.HasPartner(player))
					{
						plugin.home.SetHome(player);
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "delhome":
				if(plugin.CheckPerm(player, "marry.home"))
				{
					if(args.length == 2 && plugin.CheckPerm(player, "marry.home.others", false))
					{
						plugin.DB.DelMarryHome(args[1]);
						player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.HomeDeleted"));
					}
					else if(plugin.HasPartner(player))
					{
						plugin.DB.DelMarryHome(player);
						player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.HomeDeleted"));
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "tp":
				if(plugin.CheckPerm(player, "marry.tp"))
				{
					plugin.tp.TP(player);
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "c":
			case "chat":
				if(plugin.CheckPerm(player, "marry.chat"))
				{
					String partner = plugin.DB.GetPartner(player);
					if(partner != null && !partner.isEmpty())
					{
						if(args.length == 2 && args[1].equalsIgnoreCase("toggle"))
						{
							if(plugin.chat.Marry_ChatDirect.contains(player))
							{
								player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ChatLeft"));
								plugin.chat.Marry_ChatDirect.remove(player);
							}
							else
							{
								player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ChatJoined"));
								plugin.chat.Marry_ChatDirect.add(player);
							}
							return true;
						}
						else if(args.length >= 2)
						{
							Player otP = Bukkit.getPlayerExact(partner);
							String msg = "";
							for(int i = 1; i < args.length; i++)
							{
								msg += args[i] + " ";
							}
							plugin.chat.Chat(player, otP, msg);
						}
						else
						{
							player.sendMessage("/marry chat <Message>");
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "listenchat":
				if(plugin.CheckPerm(player, "marry.listenchat"))
				{
					if(!plugin.chat.pcl.contains(player))
					{
						plugin.chat.pcl.add(player);
						player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ListeningStarted"));
					}
					else
					{
						plugin.chat.pcl.remove(player);
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.ListeningStoped"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "gift":
			case "give":
			case "send":
				if(plugin.CheckPerm(player, "marry.gift"))
				{
					String Partner = plugin.DB.GetPartner(player);
					if(Partner != null && !Partner.isEmpty())
					{
						if(!(player.getGameMode().equals(GameMode.SURVIVAL) || plugin.config.getAllowGiftsInCreative() || plugin.CheckPerm(player, "marry.bypassgiftgamemode", false)))
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.GiftsOnlyInSurvival"));
							return true;
						}
						Player partner = Bukkit.getServer().getPlayerExact(Partner);
						if(partner == null || !partner.isOnline())
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerOffline"));
							return true;
						}
						if(!plugin.InRadiusAllWorlds(player, partner, plugin.config.GetRange("Gift")))
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotInRange"));
							return true;
						}
						ItemStack its = player.getInventory().getItemInHand();
						if(its == null || its.getType() == Material.AIR)
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoItemInHand"));
							return true;
						}
						if(partner.getInventory().firstEmpty() == -1)
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerInvFull"));
							return true;
						}
						if(plugin.economy == null || plugin.economy.Gift(player))
						{
							partner.getInventory().addItem(its);
							player.getInventory().removeItem(its);
							player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.get("Ingame.ItemSent"), its.getAmount(), its.getType().toString()));
							partner.sendMessage(ChatColor.GREEN + String.format(plugin.lang.get("Ingame.ItemReceived"), its.getAmount(), its.getType().toString()));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "backpack":
				if(plugin.minepacks == null)
				{
					ShowAvailableCmds(player);
					return true;
				}
				if(plugin.CheckPerm(player, "marry.backpack"))
				{
					String Partner = plugin.DB.GetPartner(player);
					if(Partner != null && !Partner.isEmpty())
					{
						if(args.length == 2)
						{
							// Set backpack parameter
							if(args[1].equalsIgnoreCase("on"))
							{
								plugin.DB.SetShareBackpack(player, true);
								player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.BackpackShareOn"));
							}
							else if(args[1].equalsIgnoreCase("off"))
							{
								plugin.DB.SetShareBackpack(player, false);
								player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.BackpackShareOff"));
							}
						}
						else
						{
							// Open backpack
							if(!player.getGameMode().equals(GameMode.SURVIVAL))
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.BackpackOnlyInSurvival"));
								return true;
							}
							Player partner = Bukkit.getServer().getPlayerExact(Partner);
							if(partner == null || !partner.isOnline())
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerOffline"));
								return true;
							}
							if(plugin.DB.GetPartnerShareBackpack(partner))
							{
								if(!plugin.InRadius(player, partner, plugin.config.GetRange("Backpack")))
								{
									player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotInRange"));
									return true;
								}
								plugin.minepacks.OpenBackpack(player, partner, true);
								partner.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.BackpackOpend"));
							}
							else
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.BackpackShareDenied"));
							}
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
				}
				return true;
			case "accept":
				priest.AcceptMarriage(player);
				return true;
			case "deny":
				for(Marry_Requests m : plugin.bdr)
				{
					if(m.p1.equals(player) || m.p2.equals(player))
					{
						plugin.bdr.remove(m);
						if(m.p1.equals(player))
						{
							m.p2.sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						}
						else
						{
							m.p1.sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						}
						m.priest.sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						return true;
					}
				}
				Iterator<Entry<Player, Player>> d = plugin.dr.entrySet().iterator();
				Entry<Player, Player> e;
				while(d.hasNext())
				{
					e = d.next();
					if(player.equals(e.getKey()))
					{
						e.getValue().sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						d.remove();
						return true;
					}
				}
				for(Marry_Requests m : plugin.mr)
				{
					if(m.p1.equals(player) || m.p2.equals(player))
					{
						plugin.mr.remove(m);
						if(plugin.config.UseConfirmation() && plugin.config.UseConfirmationAutoDialog() && m.priest != null)
						{
							player.chat(plugin.lang.get("Dialog.NoIDontWant"));
						}
						if(m.priest != null)
						{
							m.priest.sendMessage(String.format(plugin.lang.get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
						}
						player.sendMessage(plugin.lang.get("Ingame.YouCalledOff"));
						if(m.p1.equals(player))
						{
							m.p2.sendMessage(String.format(plugin.lang.get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
						}
						else
						{
							m.p1.sendMessage(String.format(plugin.lang.get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
						}
						return true;
					}
				}
				player.sendMessage(plugin.lang.get("Priest.NoRequest"));
				return true;
			case "cancel":
			case "close":
			case "clear":
				for(Marry_Requests m : plugin.bdr)
				{
					if(m.p1.equals(player) || m.p2.equals(player))
					{
						plugin.bdr.remove(m);
						if(m.p1.equals(player))
						{
							m.p2.sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						}
						else
						{
							m.p1.sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						}
						m.priest.sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						return true;
					}
				}
				Iterator<Entry<Player, Player>> d2 = plugin.dr.entrySet().iterator();
				Entry<Player, Player> e2;
				while(d2.hasNext())
				{
					e2 = d2.next();
					if(player.equals(e2.getKey()))
					{
						e2.getValue().sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						d2.remove();
						return true;
					}
					else if(player.equals(e2.getValue()))
					{
						e2.getKey().sendMessage(String.format(plugin.lang.get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						d2.remove();
						return true;
					}
				}
				for(Marry_Requests m : plugin.mr)
				{
					if(m.p1.equals(player) || m.p2.equals(player))
					{
						plugin.mr.remove(m);
						if(m.priest != null)
						{
							m.priest.sendMessage(String.format(plugin.lang.get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
						}
						player.sendMessage(plugin.lang.get("Ingame.YouCalledOff"));
						if(m.p1.equals(player))
						{
							m.p2.sendMessage(String.format(plugin.lang.get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
						}
						else
						{
							m.p1.sendMessage(String.format(plugin.lang.get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
						}
						return true;
					}
				}
				player.sendMessage(plugin.lang.get("Priest.NoRequest"));
				return true;
			case "update":
				if(plugin.config.UseUpdater())
				{
					if(plugin.CheckPerm(player, "marry.update", false))
					{
						plugin.update(player);
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
					}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "kiss":
				if(plugin.config.GetKissEnabled())
				{
					if(plugin.CheckPerm(player, "marry.kiss"))
					{
						if(plugin.HasPartner(player))
						{
							if(!plugin.kiss.CanKissAgain(player.getName()))
							{
								player.sendMessage(ChatColor.RED + String.format(plugin.lang.get("Ingame.KissWait"), plugin.kiss.GetKissTimeOut(player.getName())));
								return true;
							}
							String Partner = plugin.DB.GetPartner(player);
							if(Partner == null || Partner.isEmpty())
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerOffline"));
								return true;
							}
							Player partner = Bukkit.getServer().getPlayerExact(Partner);
							if(partner == null || !partner.isOnline())
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerOffline"));
								return true;
							}
							if(plugin.InRadius(player, partner, plugin.config.GetRange("Kiss")))
							{
								plugin.kiss.kiss(player, partner);
							}
							else
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.TooFarToKiss"));
							}
						}
						else
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
					}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "surname":
				if(plugin.config.getSurname() && plugin.CheckPerm(player, "marry.changesurname"))
				{
					if((plugin.config.AllowSelfMarry() && plugin.CheckPerm(player, "marry.selfmarry")) || plugin.IsPriest(player))
					{
						if((args.length == 2 && plugin.config.AllowSelfMarry() && plugin.CheckPerm(player, "marry.selfmarry") || (args.length == 3 && plugin.IsPriest(player))))
						{
							priest.SetSurname(sender, args[args.length - 2], args[args.length - 1]);
						}
						else
						{
							if(plugin.IsPriest(player))
							{
								player.sendMessage("/marry surname <Playername> <Surname>");
							}
							else
							{
								player.sendMessage("/marry surname <Surname>");
							}
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
					}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "divorce":
				if((args.length == 2 && ! plugin.config.requireBothNamesOnPriestDivorce()) || args.length == 3)
				{
					if(plugin.IsPriest(player))
					{
						if(args.length == 3)
						{
							args = Arrays.copyOf(args, 2);
						}
						priest.Divorce(player, args);
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotAPriest"));
					}
				}
				else if(plugin.config.AllowSelfDivorce() && args.length == 1)
				{
					if(plugin.CheckPerm(player, "marry.selfmarry"))
					{
						priest.SelfDivorce(player);
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
					}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
		}
		// Other commands
		if(args[0].equalsIgnoreCase(plugin.config.getChatToggleCommand()))
		{
			if(plugin.CheckPerm(player, "marry.chat"))
			{
				if(plugin.HasPartner(player))
				{
					if(plugin.chat.Marry_ChatDirect.contains(player))
					{
						player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ChatLeft"));
						plugin.chat.Marry_ChatDirect.remove(player);
					}
					else
					{
						player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.ChatJoined"));
						plugin.chat.Marry_ChatDirect.add(player);
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
			}
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(plugin.config.GetPriestCMD()))
		{
			if(plugin.CheckPerm(player, "marry.setpriest", false))
			{
				priest.setPriest(args, sender);
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
			}
		}
		else if(plugin.config.AllowSelfMarry() && ((args[0].equalsIgnoreCase("me") && (args.length == 2 || args.length == 3)) || args.length == 1))
		{
			if(plugin.CheckPerm(player, "marry.selfmarry"))
			{
				switch(args.length)
				{
					case 1:
					case 2:
						priest.SelfMarry(player, args[args.length - 1]);
						break;
					case 3:
						priest.SelfMarry(player, args[1], args[2]);
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoPermission"));
			}
		}
		else if(args.length == 2 || (args.length == 3 && plugin.config.getSurname()))
		{
			if(plugin.IsPriest(player))
			{
				priest.Marry(player, args);
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotAPriest"));
			}
		}
		else
		{
			ShowAvailableCmds(player);
		}
		return true;
	}

	public void ShowAvailableCmds(Player player)
	{
		String Surname = " <Surname>";
		if(!plugin.config.getSurname())
		{
			Surname = "";
		}
		player.sendMessage(ChatColor.YELLOW + "Marriage Master - " + plugin.lang.get("Description.Commands"));
		if(plugin.CheckPerm(player, "marry.list"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry list" + ChatColor.WHITE + " - " + plugin.lang.get("Description.ListAll"));
		}
		if(plugin.IsPriest(player))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + Surname + ChatColor.WHITE + " - " + String.format(plugin.lang.get("Description.Marry"), plugin.config.GetRange("Marry")));
			if(plugin.config.requireBothNamesOnPriestDivorce())
			{
				player.sendMessage(ChatColor.AQUA + "/marry divorce <Playername 1> <Playername 2>" + ChatColor.WHITE + " - " + String.format(plugin.lang.get("Description.Divorce"), plugin.config.GetRange("Marry")));
			}
			else
			{
				player.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + String.format(plugin.lang.get("Description.Divorce"), plugin.config.GetRange("Marry")));
			}
		}
		if(plugin.CheckPerm(player, "marry.tp"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry tp" + ChatColor.WHITE + " - " + plugin.lang.get("Description.TP"));
		}
		if(plugin.CheckPerm(player, "marry.home"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry home" + ChatColor.WHITE + " - " + plugin.lang.get("Description.TPHome"));
			player.sendMessage(ChatColor.AQUA + "/marry sethome" + ChatColor.WHITE + " - " + plugin.lang.get("Description.SetHome"));
			player.sendMessage(ChatColor.AQUA + "/marry delhome" + ChatColor.WHITE + " - " + plugin.lang.get("Description.DelHome"));
			if(plugin.CheckPerm(player, "marry.home.others", false))
			{
				player.sendMessage(ChatColor.AQUA + "/marry home <player>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.TPHomeOther"));
				player.sendMessage(ChatColor.AQUA + "/marry delhome <player>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.DelHomeOther"));
			}
		}
		if(plugin.CheckPerm(player, "marry.chat"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry chat <Message>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Chat"));
			player.sendMessage(ChatColor.AQUA + "/marry chat toggle" + ChatColor.WHITE + " - " + plugin.lang.get("Description.ChatToggle"));
		}
		if(plugin.CheckPerm(player, "marry.pvpon") && plugin.config.GetAllowBlockPvP())
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpon" + ChatColor.WHITE + " - " + plugin.lang.get("Description.PvPOn"));
		}
		if(plugin.CheckPerm(player, "marry.pvpoff") && plugin.config.GetAllowBlockPvP())
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpoff" + ChatColor.WHITE + " - " + plugin.lang.get("Description.PvPOff"));
		}
		if(plugin.CheckPerm(player, "marry.gift"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry gift" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Gift"));
		}
		if(plugin.minepacks != null && plugin.CheckPerm(player, "marry.backpack"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry backpack" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Backpack"));
			if(plugin.DB.GetPartnerShareBackpack(player))
			{
				player.sendMessage(ChatColor.AQUA + "/marry backpack off" + ChatColor.WHITE + " - " + plugin.lang.get("Description.BackpackOff"));
			}
			else
			{
				player.sendMessage(ChatColor.AQUA + "/marry backpack on" + ChatColor.WHITE + " - " + plugin.lang.get("Description.BackpackOn"));
			}
		}
		if(plugin.CheckPerm(player, "marry.kiss") && plugin.config.GetKissEnabled())
		{
			player.sendMessage(ChatColor.AQUA + "/marry kiss" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Kiss"));
		}
		if(plugin.config.AllowSelfMarry() && plugin.CheckPerm(player, "marry.selfmarry"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername>" + Surname + ChatColor.WHITE + " - " + plugin.lang.get("Description.SelfMarry"));
			player.sendMessage(ChatColor.AQUA + "/marry divorce" + ChatColor.WHITE + " - " + plugin.lang.get("Description.SelfDivorce"));
		}
		if(plugin.config.getSurname() && plugin.CheckPerm(player, "marry.changesurname"))
		{
			if(plugin.IsPriest(player))
			{
				player.sendMessage(ChatColor.AQUA + "/marry Surname <Playername> <Surname>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Surname"));
			}
			else
			{
				if(plugin.config.AllowSelfMarry() && plugin.CheckPerm(player, "marry.selfmarry"))
				{
					player.sendMessage(ChatColor.AQUA + "/marry Surname <Surname>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Surname"));
				}
			}
		}
		if(plugin.CheckPerm(player, "marry.setpriest", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry " + plugin.config.GetPriestCMD() + " <Playername>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Priest"));
		}
		if(plugin.CheckPerm(player, "marry.listenchat", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry listenchat" + ChatColor.WHITE + " - " + plugin.lang.get("Description.ListenChat"));
		}
		if(plugin.config.UseUpdater() && plugin.CheckPerm(player, "marry.update", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry update" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Update"));
		}
		if(plugin.CheckPerm(player, "marry.reload", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + plugin.lang.get("Description.reload"));
		}
	}

	private void ShowAvailableCmds(CommandSender sender)
	{
		String Surname = " <Surname>";
		if(!plugin.config.getSurname())
		{
			Surname = "";
		}
		sender.sendMessage(ChatColor.YELLOW + "Marriage Master - " + plugin.lang.get("Description.Commands"));
		sender.sendMessage(ChatColor.AQUA + "/marry list <Page>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.ListAll"));
		sender.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + Surname + ChatColor.WHITE + " - " + plugin.lang.get("Description.Marry"));
		if(plugin.config.requireBothNamesOnPriestDivorce())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry divorce <Playername 1> <Playername 2>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Divorce"));
		}
		else
		{
			sender.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Divorce"));
		}
		sender.sendMessage(ChatColor.AQUA + "/marry priest <Playername>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Priest"));
		if(plugin.config.getSurname())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry Surname <Playername> <Surname>" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Surname"));
		}
		if(plugin.config.UseUpdater())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry update" + ChatColor.WHITE + " - " + plugin.lang.get("Description.Update"));
		}
		sender.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + plugin.lang.get("Description.reload"));
	}
}
