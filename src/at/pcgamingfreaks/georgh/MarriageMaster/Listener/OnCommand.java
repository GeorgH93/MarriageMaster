/*
 *   Copyright (C) 2014 GeorgH93
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

package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import at.pcgamingfreaks.georgh.MarriageMaster.Commands.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Databases.Files;
import at.pcgamingfreaks.georgh.MarriageMaster.Databases.MySQL;
import at.pcgamingfreaks.georgh.MarriageMaster.*;

public class OnCommand implements CommandExecutor 
{
	private MarriageMaster marriageMaster;

	private MarryTp marryTp;
	private Priest priest;
	private Home home;
	
	public OnCommand(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		
		home = new Home(marriageMaster);
		marryTp = new MarryTp(marriageMaster);
		priest = new Priest(marriageMaster);
	}
	
	private void reload()
	{
		HandlerList.unregisterAll(marriageMaster);
		marriageMaster.config.Reload();
		marriageMaster.lang.Reload();
		if(marriageMaster.config.GetDatabaseType().toLowerCase() != marriageMaster.DBType)
		{
			marriageMaster.DBType = marriageMaster.config.GetDatabaseType().toLowerCase();
			switch(marriageMaster.DBType)
			{
				case "mysql": marriageMaster.DB = new MySQL(marriageMaster); break;
				default: marriageMaster.DB = new Files(marriageMaster); break;
			}
		}
		marriageMaster.RegisterEconomy();
		
		if(marriageMaster.perms == null && marriageMaster.config.getUsePermissions())
		{
			if(!marriageMaster.setupPermissions())
			{
				marriageMaster.log.info(marriageMaster.lang.Get("Console.NoPermPL"));
			}
		}
		else if(marriageMaster.perms != null && !marriageMaster.config.getUsePermissions())
		{
			marriageMaster.perms = null;
		}
		if(marriageMaster.minepacks == null && marriageMaster.config.getUseMinepacks())
		{
			if(!marriageMaster.setupMinePacks())
			{
				marriageMaster.minepacks = null;
			}
		}
		else if(marriageMaster.minepacks != null && !marriageMaster.config.getUseMinepacks())
		{
			marriageMaster.minepacks = null;
		}
		marriageMaster.RegisterEvents();
		marriageMaster.mr = new ArrayList<Marry_Requests>();
		marriageMaster.dr = new HashMap<Player, Player>();
	}
		
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) 
	{
		Player player = null;
		if (sender instanceof Player) 
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
						reload();
						sender.sendMessage(ChatColor.BLUE + "Reloaded");
						break;
					case "list":
			        	NavigableMap<String, String> map = marriageMaster.DB.GetAllMarriedPlayers();
		    			if(map.size() > 0)
		    			{
		    				sender.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListHL"));
		    				
		    				for(Map.Entry<String, String> item : map.entrySet())
		    				{
		    					sender.sendMessage(ChatColor.GREEN + item.getKey() + " + " + item.getValue());
		    				}	
		    			}	
		    			else
		    			{
		    				sender.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoMarriedPlayers"));
		    			}
		    			break;
					case "update":
						if(marriageMaster.config.UseUpdater())
						{
							Update(sender);
						}
						break;
					case "surname":
						if(marriageMaster.config.getSurname())
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
							if(args[0].equalsIgnoreCase(marriageMaster.config.GetPriestCMD()))
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
						else if(args.length == 3 && marriageMaster.config.getSurname())
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
		
		if(args.length == 0 || args.length > 3)
		{
			ShowAvailableCmds(player);
			return true;
		}
		switch(args[0].toLowerCase())
		{
			case "reload":
				if(marriageMaster.config.CheckPerm(player, "marry.reload", false))
		    	{
					reload();
					player.sendMessage(ChatColor.BLUE + "Reloaded");
				}
				else
				{
					player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
				}
				return true;
			case "list":
				if(marriageMaster.config.CheckPerm(player, "marry.list"))
	    		{
					int page = 0, avpages = 0;
					if(args.length == 2)
					{
						try
						{
							page = Integer.parseInt(args[1])-1;
							if(page < 0)
							{
								page = 0;
							}
						}
						catch(Exception e)
						{
							player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NaN"));
							return true;
						}
					}
	    			NavigableMap<String, String> map = marriageMaster.DB.GetAllMarriedPlayers();

	    			if(map.size() > 0)
	    			{
	    				avpages = (int)Math.ceil(map.size() / 7.0);
	    				if(page >= avpages)
	    				{
	    					page = avpages - 1;
	    				}
	    				player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListHL") + ((avpages > 1) ? 
	    				  (ChatColor.WHITE + " - " + ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.ListHLMP"), page + 1, avpages)) : ""));
	    				int c = 7, c2 = page*7;
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
	    							return true;
	    						}
	    					}
	    				}	
	    			}	
	    			else
	    			{
	    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoMarriedPlayers"));
	    			}
	    		}
	        	else
		    	{
		    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
		    	}
				return true;
			case "pvpon":
				if(marriageMaster.config.GetAllowBlockPvP()) 
		        {
		        	if(marriageMaster.HasPartner(player))
		        	{
		    	    	if(marriageMaster.config.CheckPerm(player, "marry.pvpon"))
		    	    	{
		    	    		marriageMaster.DB.SetPvPEnabled(player, true);
		    	    		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PvPOn"));
		    	    	}
		    	    	else
		    	    	{
		    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
		    	    	}
		        	}
		    		else
		    		{
		    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
		    		}
		        }
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "pvpoff":
				if (marriageMaster.config.GetAllowBlockPvP())
		        {
		        	if(marriageMaster.HasPartner(player))
		        	{
		    	    	if(marriageMaster.config.CheckPerm(player, "marry.pvpoff"))
		    	    	{
		    	    		marriageMaster.DB.SetPvPEnabled(player, false);
		    	    		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PvPOff"));
		    	    	}
		    	    	else
		    	    	{
		    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
		    	    	}
		        	}
		    		else
		    		{
		    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
		    		}
		        }
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "home":
				if(marriageMaster.config.CheckPerm(player, "marry.home"))
    	    	{
					if(marriageMaster.HasPartner(player))
		        	{
						home.TP(player);
		        	}
					else
					{
						player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
					}
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
				return true;
			case "sethome":
				if(marriageMaster.config.CheckPerm(player, "marry.home"))
    	    	{
					if(marriageMaster.HasPartner(player))
		        	{
						home.SetHome(player);
		        	}
					else
		    		{
		    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
		    		}
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
				return true;
			case "tp":
				if(marriageMaster.config.CheckPerm(player, "marry.tp"))
    			{
					marryTp.TP(player);
    			}
				else
				{
					player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
				}
				return true;
			case "c":
			case "chat":
				if(marriageMaster.config.CheckPerm(player, "marry.chat"))
	    		{
					String partner = marriageMaster.DB.GetPartner(player);
	        		if(partner != null && !partner.isEmpty())
	        		{
	        			if(args.length == 2 && args[1].equalsIgnoreCase("toggle"))
	            		{
	        				if(marriageMaster.chat.Marry_ChatDirect.contains(player))
	        				{
	        					player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ChatLeft"));
	        					marriageMaster.chat.Marry_ChatDirect.remove(player);
	        				}
	        				else
	        				{
	        					player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ChatJoined"));
	        					marriageMaster.chat.Marry_ChatDirect.add(player);
	        				}
	            			return true;
	            		}
	        			else if(args.length == 2)
	        			{
		        			Player otP = Bukkit.getPlayer(partner);
		        			String msg = "";
		        			for(int i = 1; i < args.length; i++)
		    				{
		    					msg += args[i] + " ";
		    				}
		        			marriageMaster.chat.Chat(player, otP, msg);
	        			}
	        			else
	        			{
	        				player.sendMessage("/marry chat <Message>");
	        			}
	        		}
	        		else
	        		{
	        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
	        		}
	    		}
	        	else
	        	{
	        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
	        	}
				return true;
			case "listenchat":
				if(marriageMaster.config.CheckPerm(player, "marry.listenchat"))
	    		{
	        		if(!marriageMaster.chat.pcl.contains(player))
	        		{
	        			marriageMaster.chat.pcl.add(player);
	        			player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListeningStarted"));
	        		}
	        		else
	        		{
	        			marriageMaster.chat.pcl.remove(player);
	        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.ListeningStoped"));
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
		    	}
				return true;
			case "gift":
			case "give":
			case "send":
				if(marriageMaster.config.CheckPerm(player, "marry.gift"))
	    		{
					String Partner = marriageMaster.DB.GetPartner(player);
	        		if(Partner != null && !Partner.isEmpty())
					{
	        			if(!player.getGameMode().equals(GameMode.SURVIVAL))
	        			{
	        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.GiftsOnlyInSurvival"));
	        				return true;
	        			}
	        			Player partner = Bukkit.getServer().getPlayer(Partner);
	        			if(partner == null || !partner.isOnline())
	        			{
	        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
	        				return true;
	        			}
	        			if(!marriageMaster.InRadiusAllWorlds(player, partner, marriageMaster.config.GetRange("Gift")))
	        			{
	        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotInRange"));
	        				return true;
	        			}
	        			ItemStack its = player.getInventory().getItemInHand();
	        			if(its == null || its.getType() == Material.AIR)
	        			{
	        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoItemInHand"));
	        				return true;
	        			}
	        			if(partner.getInventory().firstEmpty() == -1)
	        			{
	        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerInvFull"));
	        				return true;
	        			}
	        			if(marriageMaster.economy == null || marriageMaster.economy.Gift(player, marriageMaster.config.GetEconomyGift()))
	        			{
		        			partner.getInventory().addItem(its);
		        			player.getInventory().removeItem(its);
		        			player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.ItemSent"), its.getAmount(), its.getType().toString()));
		        			partner.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.ItemReceived"), its.getAmount(), its.getType().toString()));
	        			}
					}
	        		else
	        		{
	        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
		    	}
				return true;
			case "backpack":
				if(marriageMaster.minepacks == null)
				{
					ShowAvailableCmds(player);
					return true;
				}
				if(marriageMaster.config.CheckPerm(player, "marry.backpack"))
	    		{
					String Partner = marriageMaster.DB.GetPartner(player);
	        		if(Partner != null && !Partner.isEmpty())
					{
	        			if(args.length == 2)
	        			{
	        				// Set backpack parameter
	        				if(args[1].equalsIgnoreCase("on"))
	        				{
	        					marriageMaster.DB.SetShareBackpack(player, true);
	        					player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.BackpackShareOn"));
	        				}
	        				else if(args[1].equalsIgnoreCase("off"))
	        				{
	        					marriageMaster.DB.SetShareBackpack(player, false);
	        					player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.BackpackShareOff"));
	        				}
	        			}
	        			else
	        			{
	        				// Open backpack
	        				if(!player.getGameMode().equals(GameMode.SURVIVAL))
		        			{
		        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.BackpackOnlyInSurvival"));
		        				return true;
		        			}
		        			Player partner = Bukkit.getServer().getPlayer(Partner);
		        			if(partner == null || !partner.isOnline())
		        			{
		        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
		        				return true;
		        			}
	        				if(marriageMaster.DB.GetPartnerShareBackpack(partner))
	        				{
	        					if(!marriageMaster.InRadius(player, partner, marriageMaster.config.GetRange("Backpack")))
	    	        			{
	    	        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotInRange"));
	    	        				return true;
	    	        			}
	        					marriageMaster.minepacks.OpenBackpack(player, partner, true);
	        					partner.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.BackpackOpend"));
	        				}
	        				else
	        				{
	        					player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.BackpackShareDenied"));
	        				}
	        			}
					}
	        		else
	        		{
	        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
		    	}
				return true;
			case "accept": priest.AcceptMarriage(player); return true;
			case "deny":
				Iterator<Entry<Player, Player>> d = marriageMaster.dr.entrySet().iterator();
				Entry<Player, Player> e;
				while(d.hasNext())
				{
					e = d.next();
					if(player.equals(e.getKey()))
					{
						e.getValue().sendMessage(String.format(marriageMaster.lang.Get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						d.remove();
						return true;
					}
				}
				for (Marry_Requests m : marriageMaster.mr)
	    		{
	        		if(m.p1 == player || m.p2 == player)
	        		{
	        			marriageMaster.mr.remove(m);
	        			if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog() && m.priest!=null)
						{
	        				player.chat(marriageMaster.lang.Get("Dialog.NoIDontWant"));
						}
	        			if(m.priest != null)
	        			{
	        				m.priest.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
	        			}
	        			player.sendMessage(marriageMaster.lang.Get("Ingame.YouCalledOff"));
	        			if(m.p1 == player)
	        			{
	        				m.p2.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
	        			}
	        			else
	        			{
	        				m.p1.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
	        			}
	        			return true;
	        		}
	    		}
	        	player.sendMessage(marriageMaster.lang.Get("Priest.NoRequest"));
	        	return true;
			case "update":
				if(marriageMaster.config.UseUpdater())
				{
		        	if(marriageMaster.config.CheckPerm(player, "marry.update", false))
		    		{
		        		Update(sender);
					}
			    	else
			    	{
			    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
			    	}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "kiss":
				if (marriageMaster.config.GetKissEnabled())
		        {
		        	if(marriageMaster.config.CheckPerm(player, "marry.kiss"))
		    		{
		        		if(marriageMaster.HasPartner(player))
						{
		        			if(!marriageMaster.kiss.CanKissAgain(player.getName()))
		            		{
		            			player.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.KissWait"),marriageMaster.kiss.GetKissTimeOut(player.getName())));
		            			return true;
		            		}
		        			String Partner = marriageMaster.DB.GetPartner(player);
		        			if(Partner == null || Partner.isEmpty())
		        			{
		        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
		        				return true;
		        			}
		        			Player partner = Bukkit.getServer().getPlayer(Partner);
		        			if(partner == null || !partner.isOnline())
		        			{
		        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
		        				return true;
		        			}
		        			if(marriageMaster.InRadius(player, partner, marriageMaster.config.GetRange("Kiss")))
		        			{
		        				marriageMaster.kiss.kiss(player, partner);
		        			}
		        			else
		        			{
		        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.TooFarToKiss"));
		        			}
						}
		        		else
		        		{
		        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
		        		}
		    		}
		        	else
			    	{
			    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
			    	}
		        }
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "surname":
				if(marriageMaster.config.getSurname() && marriageMaster.config.CheckPerm(player, "marry.changesurname"))
		        {
		        	if((marriageMaster.config.AllowSelfMarry() && marriageMaster.config.CheckPerm(player, "marry.selfmarry")) || marriageMaster.IsPriest(player))
		        	{
		        		if((args.length == 2 && marriageMaster.config.AllowSelfMarry() && marriageMaster.config.CheckPerm(player, "marry.selfmarry") || (args.length == 3 && marriageMaster.IsPriest(player))))
		        		{
			        		priest.SetSurname(sender, args[args.length - 2], args[args.length - 1]);
		        		}
		        		else
		        		{
		        			if(marriageMaster.IsPriest(player))
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
			    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
			    	}
		        }
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "divorce":
				if (args.length == 2)
		        {
		        	if(marriageMaster.IsPriest(player))
		        	{
		        		priest.Divorce(player, args);
		        	}
		        	else
		        	{
		        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Description.NotAPriest"));
		        	}
		        }
		        else if(marriageMaster.config.AllowSelfMarry() && args.length == 1)
		        {
		        	if(marriageMaster.config.CheckPerm(player, "marry.selfmarry"))
		        	{
		        		priest.SelfDivorce(player);
		        	}
		        	else
			    	{
			    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
			    	}
		        }
		        else
		        {
		        	ShowAvailableCmds(player);
		        }
				return true;
		}
		// Other commands
		if(marriageMaster.config.UseAltChatToggleCommand() && args[0].equalsIgnoreCase(marriageMaster.config.ChatToggleCommand()))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.chat"))
    		{
        		if(marriageMaster.HasPartner(player))
        		{
    				if(marriageMaster.chat.Marry_ChatDirect.contains(player))
    				{
    					player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ChatLeft"));
    					marriageMaster.chat.Marry_ChatDirect.remove(player);
    				}
    				else
    				{
    					player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ChatJoined"));
    					marriageMaster.chat.Marry_ChatDirect.add(player);
    				}
        		}
        		else
        		{
        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
        		}
    		}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
        	}
        }
        else if(args.length == 2 && args[0].equalsIgnoreCase(marriageMaster.config.GetPriestCMD()))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.setpriest", false))
        	{
        		priest.setPriest(args, sender);
        	}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
        	}
        }
        else if(marriageMaster.config.AllowSelfMarry() && ((args[0].equalsIgnoreCase("me") && (args.length == 2 || args.length == 3)) || args.length == 1))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.selfmarry"))
        	{
        		switch(args.length)
        		{
        			case 1:
        			case 2:
        				priest.SelfMarry(player, args[args.length-1]); break;
        			case 3:
        				priest.SelfMarry(player, args[1], args[2]);
        		}
        	}
        	else
	    	{
	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
	    	}
        }
        else if (args.length == 2 || (args.length == 3 && marriageMaster.config.getSurname()))
        {
        	if(marriageMaster.IsPriest(player))
    		{
    			priest.Marry(player, args);
    		}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotAPriest"));
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
		if(!marriageMaster.config.getSurname())
		{
			Surname = "";
		}
		player.sendMessage(ChatColor.YELLOW + "Marriage Master - " + marriageMaster.lang.Get("Description.Commands"));
		if(marriageMaster.config.CheckPerm(player, "marry.list"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry list" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListAll"));
		}
		if(marriageMaster.IsPriest(player))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + Surname + ChatColor.WHITE + " - " + String.format(marriageMaster.lang.Get("Description.Marry"), marriageMaster.config.GetRange("Marry")));
			player.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + String.format(marriageMaster.lang.Get("Description.Divorce"), marriageMaster.config.GetRange("Marry")));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.tp"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry tp" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.TP"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.home"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry home" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.TPHome"));
			player.sendMessage(ChatColor.AQUA + "/marry sethome" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.SetHome"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.chat"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry chat <Message>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Chat"));
			player.sendMessage(ChatColor.AQUA + "/marry " + (marriageMaster.config.UseAltChatToggleCommand() ? marriageMaster.config.ChatToggleCommand() : "chat toggle") + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ChatToggle"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.pvpon") && marriageMaster.config.GetAllowBlockPvP())
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpon" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.PvPOn"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.pvpoff") && marriageMaster.config.GetAllowBlockPvP())
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpoff" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.PvPOff"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.gift"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry gift" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Gift"));
		}
		if(marriageMaster.minepacks != null && marriageMaster.config.CheckPerm(player, "marry.backpack"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry backpack" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Backpack"));
			if(marriageMaster.DB.GetPartnerShareBackpack(player))
			{
				player.sendMessage(ChatColor.AQUA + "/marry backpack off" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.BackpackOff"));
			}
			else
			{
				player.sendMessage(ChatColor.AQUA + "/marry backpack on" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.BackpackOn"));
			}
		}
		if(marriageMaster.config.CheckPerm(player, "marry.kiss") && marriageMaster.config.GetKissEnabled())
		{
			player.sendMessage(ChatColor.AQUA + "/marry kiss" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Kiss"));
		}
		if(marriageMaster.config.AllowSelfMarry() && marriageMaster.config.CheckPerm(player, "marry.selfmarry"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername>" + Surname + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.SelfMarry"));
			player.sendMessage(ChatColor.AQUA + "/marry divorce" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.SelfDivorce"));
		}
		if(marriageMaster.config.getSurname() && marriageMaster.config.CheckPerm(player, "marry.changesurname"))
		{
			if(marriageMaster.IsPriest(player))
			{
				player.sendMessage(ChatColor.AQUA + "/marry Surname <Playername> <Surname>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Surname"));
			}
			else
			{
				if(marriageMaster.config.AllowSelfMarry() && marriageMaster.config.CheckPerm(player, "marry.selfmarry"))
				{
					player.sendMessage(ChatColor.AQUA + "/marry Surname <Surname>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Surname"));
				}
			}
		}
		if(marriageMaster.config.CheckPerm(player, "marry.setpriest", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry " + marriageMaster.config.GetPriestCMD() + " <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Priest"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.listenchat", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry listenchat" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListenChat"));
		}
		if(marriageMaster.config.UseUpdater() && marriageMaster.config.CheckPerm(player, "marry.update", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry update" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Update"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.reload", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Reload"));
		}
	}
	
	private void Update(CommandSender sender)
	{
		if(marriageMaster.Update())
		{
			sender.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.Updated"));
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + marriageMaster.lang.Get("Ingame.NoUpdate"));
		}
	}
	
	private void ShowAvailableCmds(CommandSender sender)
	{
		String Surname = " <Surname>";
		if(!marriageMaster.config.getSurname())
		{
			Surname = "";
		}
		sender.sendMessage(ChatColor.YELLOW + "Marriage Master - " + marriageMaster.lang.Get("Description.Commands"));
		sender.sendMessage(ChatColor.AQUA + "/marry list <Page>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListAll"));
		sender.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + Surname + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Marry"));
		sender.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Divorce"));
		sender.sendMessage(ChatColor.AQUA + "/marry priest <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Priest"));
		if(marriageMaster.config.getSurname())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry Surname <Playername> <Surname>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Surname"));
		}
		if(marriageMaster.config.UseUpdater())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry update" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Update"));
		}
		sender.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Reload"));
	}
}
