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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

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

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Marry_Requests;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Databases.Database;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Economy.BaseEconomy;

public class OnCommand implements CommandExecutor 
{
	private MarriageMaster plugin;

	private MarryTp marryTp;
	private Priest priest;
	private Home home;
	
	public OnCommand(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
		
		home = new Home(plugin);
		marryTp = new MarryTp(plugin);
		priest = new Priest(plugin);
	}
	
	private void reload()
	{
		HandlerList.unregisterAll(plugin);
		plugin.config.Reload();
		plugin.lang.Reload();
		if(plugin.config.GetDatabaseType().toLowerCase() != plugin.DBType)
		{
			plugin.DBType = plugin.config.GetDatabaseType().toLowerCase();
			plugin.DB = Database.getDatabase(plugin.DBType, plugin);
		}
		plugin.economy = BaseEconomy.GetEconomy(plugin);
		if(plugin.perms == null && plugin.config.getUseVaultPermissions())
		{
			if(!plugin.setupPermissions())
			{
				plugin.log.info(plugin.lang.Get("Console.NoPermPL"));
			}
		}
		else if(plugin.perms != null && !plugin.config.getUseVaultPermissions())
		{
			plugin.perms = null;
		}
		if(plugin.minepacks == null && plugin.config.getUseMinepacks())
		{
			if(!plugin.setupMinePacks())
			{
				plugin.minepacks = null;
			}
		}
		else if(plugin.minepacks != null && !plugin.config.getUseMinepacks())
		{
			plugin.minepacks = null;
		}
		plugin.RegisterEvents();
		plugin.mr = new ArrayList<Marry_Requests>();
		plugin.dr = new HashMap<Player, Player>();
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
			        	NavigableMap<String, String> map = plugin.DB.GetAllMarriedPlayers();
		    			if(map.size() > 0)
		    			{
		    				sender.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ListHL"));
		    				
		    				for(Map.Entry<String, String> item : map.entrySet())
		    				{
		    					sender.sendMessage(ChatColor.GREEN + item.getKey() + " + " + item.getValue());
		    				}	
		    			}	
		    			else
		    			{
		    				sender.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoMarriedPlayers"));
		    			}
		    			break;
					case "update":
						if(plugin.config.UseUpdater())
						{
							Update(sender);
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
		
		if(args.length == 0 || args.length > 3)
		{
			ShowAvailableCmds(player);
			return true;
		}
		switch(args[0].toLowerCase())
		{
			case "reload":
				if(plugin.CheckPerm(player, "marry.reload", false))
		    	{
					reload();
					player.sendMessage(ChatColor.BLUE + "Reloaded");
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
				}
				return true;
			case "list":
				if(plugin.CheckPerm(player, "marry.list"))
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
							player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NaN"));
							return true;
						}
					}
	    			NavigableMap<String, String> map = plugin.DB.GetAllMarriedPlayers();

	    			if(map.size() > 0)
	    			{
	    				avpages = (int)Math.ceil(map.size() / 7.0);
	    				if(page >= avpages)
	    				{
	    					page = avpages - 1;
	    				}
	    				player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ListHL") + ((avpages > 1) ? 
	    				  (ChatColor.WHITE + " - " + ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.ListHLMP"), page + 1, avpages)) : ""));
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
	    				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoMarriedPlayers"));
	    			}
	    		}
	        	else
		    	{
		    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
		    	    		player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PvPOn"));
		    	    	}
		    	    	else
		    	    	{
		    	    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
		    	    	}
		        	}
		    		else
		    		{
		    			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
		    		}
		        }
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "pvpoff":
				if (plugin.config.GetAllowBlockPvP())
		        {
		        	if(plugin.HasPartner(player))
		        	{
		    	    	if(plugin.CheckPerm(player, "marry.pvpoff"))
		    	    	{
		    	    		plugin.DB.SetPvPEnabled(player, false);
		    	    		player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PvPOff"));
		    	    	}
		    	    	else
		    	    	{
		    	    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
		    	    	}
		        	}
		    		else
		    		{
		    			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
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
					if(plugin.HasPartner(player))
		        	{
						home.TP(player);
		        	}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
					}
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
    	    	}
				return true;
			case "sethome":
				if(plugin.CheckPerm(player, "marry.home"))
    	    	{
					if(plugin.HasPartner(player))
		        	{
						home.SetHome(player);
		        	}
					else
		    		{
		    			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
		    		}
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
    	    	}
				return true;
			case "tp":
				if(plugin.CheckPerm(player, "marry.tp"))
    			{
					marryTp.TP(player);
    			}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
	        					player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ChatLeft"));
	        					plugin.chat.Marry_ChatDirect.remove(player);
	        				}
	        				else
	        				{
	        					player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ChatJoined"));
	        					plugin.chat.Marry_ChatDirect.add(player);
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
		        			plugin.chat.Chat(player, otP, msg);
	        			}
	        			else
	        			{
	        				player.sendMessage("/marry chat <Message>");
	        			}
	        		}
	        		else
	        		{
	        			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
	        		}
	    		}
	        	else
	        	{
	        		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
	        	}
				return true;
			case "listenchat":
				if(plugin.CheckPerm(player, "marry.listenchat"))
	    		{
	        		if(!plugin.chat.pcl.contains(player))
	        		{
	        			plugin.chat.pcl.add(player);
	        			player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ListeningStarted"));
	        		}
	        		else
	        		{
	        			plugin.chat.pcl.remove(player);
	        			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.ListeningStoped"));
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
	        			if(!player.getGameMode().equals(GameMode.SURVIVAL))
	        			{
	        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.GiftsOnlyInSurvival"));
	        				return true;
	        			}
	        			Player partner = Bukkit.getServer().getPlayer(Partner);
	        			if(partner == null || !partner.isOnline())
	        			{
	        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerOffline"));
	        				return true;
	        			}
	        			if(!plugin.InRadiusAllWorlds(player, partner, plugin.config.GetRange("Gift")))
	        			{
	        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotInRange"));
	        				return true;
	        			}
	        			ItemStack its = player.getInventory().getItemInHand();
	        			if(its == null || its.getType() == Material.AIR)
	        			{
	        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoItemInHand"));
	        				return true;
	        			}
	        			if(partner.getInventory().firstEmpty() == -1)
	        			{
	        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerInvFull"));
	        				return true;
	        			}
	        			if(plugin.economy == null || plugin.economy.Gift(player))
	        			{
		        			partner.getInventory().addItem(its);
		        			player.getInventory().removeItem(its);
		        			player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.ItemSent"), its.getAmount(), its.getType().toString()));
		        			partner.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.ItemReceived"), its.getAmount(), its.getType().toString()));
	        			}
					}
	        		else
	        		{
	        			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
	        					player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.BackpackShareOn"));
	        				}
	        				else if(args[1].equalsIgnoreCase("off"))
	        				{
	        					plugin.DB.SetShareBackpack(player, false);
	        					player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.BackpackShareOff"));
	        				}
	        			}
	        			else
	        			{
	        				// Open backpack
	        				if(!player.getGameMode().equals(GameMode.SURVIVAL))
		        			{
		        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.BackpackOnlyInSurvival"));
		        				return true;
		        			}
		        			Player partner = Bukkit.getServer().getPlayer(Partner);
		        			if(partner == null || !partner.isOnline())
		        			{
		        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerOffline"));
		        				return true;
		        			}
	        				if(plugin.DB.GetPartnerShareBackpack(partner))
	        				{
	        					if(!plugin.InRadius(player, partner, plugin.config.GetRange("Backpack")))
	    	        			{
	    	        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotInRange"));
	    	        				return true;
	    	        			}
	        					plugin.minepacks.OpenBackpack(player, partner, true);
	        					partner.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.BackpackOpend"));
	        				}
	        				else
	        				{
	        					player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.BackpackShareDenied"));
	        				}
	        			}
					}
	        		else
	        		{
	        			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
		    	}
				return true;
			case "accept": priest.AcceptMarriage(player); return true;
			case "deny":
				Iterator<Entry<Player, Player>> d = plugin.dr.entrySet().iterator();
				Entry<Player, Player> e;
				while(d.hasNext())
				{
					e = d.next();
					if(player.equals(e.getKey()))
					{
						e.getValue().sendMessage(String.format(plugin.lang.Get("Priest.PlayerCanceled"), player.getDisplayName() + ChatColor.WHITE));
						d.remove();
						return true;
					}
				}
				for (Marry_Requests m : plugin.mr)
	    		{
	        		if(m.p1 == player || m.p2 == player)
	        		{
	        			plugin.mr.remove(m);
	        			if(plugin.config.UseConfirmation() && plugin.config.UseConfirmationAutoDialog() && m.priest!=null)
						{
	        				player.chat(plugin.lang.Get("Dialog.NoIDontWant"));
						}
	        			if(m.priest != null)
	        			{
	        				m.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
	        			}
	        			player.sendMessage(plugin.lang.Get("Ingame.YouCalledOff"));
	        			if(m.p1 == player)
	        			{
	        				m.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
	        			}
	        			else
	        			{
	        				m.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerCalledOff"), player.getDisplayName() + ChatColor.WHITE));
	        			}
	        			return true;
	        		}
	    		}
	        	player.sendMessage(plugin.lang.Get("Priest.NoRequest"));
	        	return true;
			case "update":
				if(plugin.config.UseUpdater())
				{
		        	if(plugin.CheckPerm(player, "marry.update", false))
		    		{
		        		Update(sender);
					}
			    	else
			    	{
			    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
			    	}
				}
				else
				{
					ShowAvailableCmds(player);
				}
				return true;
			case "kiss":
				if (plugin.config.GetKissEnabled())
		        {
		        	if(plugin.CheckPerm(player, "marry.kiss"))
		    		{
		        		if(plugin.HasPartner(player))
						{
		        			if(!plugin.kiss.CanKissAgain(player.getName()))
		            		{
		            			player.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.KissWait"),plugin.kiss.GetKissTimeOut(player.getName())));
		            			return true;
		            		}
		        			String Partner = plugin.DB.GetPartner(player);
		        			if(Partner == null || Partner.isEmpty())
		        			{
		        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerOffline"));
		        				return true;
		        			}
		        			Player partner = Bukkit.getServer().getPlayer(Partner);
		        			if(partner == null || !partner.isOnline())
		        			{
		        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerOffline"));
		        				return true;
		        			}
		        			if(plugin.InRadius(player, partner, plugin.config.GetRange("Kiss")))
		        			{
		        				plugin.kiss.kiss(player, partner);
		        			}
		        			else
		        			{
		        				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.TooFarToKiss"));
		        			}
						}
		        		else
		        		{
		        			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
		        		}
		    		}
		        	else
			    	{
			    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
			    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
		        	if(plugin.IsPriest(player))
		        	{
		        		priest.Divorce(player, args);
		        	}
		        	else
		        	{
		        		player.sendMessage(ChatColor.RED + plugin.lang.Get("Description.NotAPriest"));
		        	}
		        }
		        else if(plugin.config.AllowSelfMarry() && args.length == 1)
		        {
		        	if(plugin.CheckPerm(player, "marry.selfmarry"))
		        	{
		        		priest.SelfDivorce(player);
		        	}
		        	else
			    	{
			    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
			    	}
		        }
		        else
		        {
		        	ShowAvailableCmds(player);
		        }
				return true;
		}
		// Other commands
		if(plugin.config.UseAltChatToggleCommand() && args[0].equalsIgnoreCase(plugin.config.ChatToggleCommand()))
        {
        	if(plugin.CheckPerm(player, "marry.chat"))
    		{
        		if(plugin.HasPartner(player))
        		{
    				if(plugin.chat.Marry_ChatDirect.contains(player))
    				{
    					player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ChatLeft"));
    					plugin.chat.Marry_ChatDirect.remove(player);
    				}
    				else
    				{
    					player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.ChatJoined"));
    					plugin.chat.Marry_ChatDirect.add(player);
    				}
        		}
        		else
        		{
        			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
        		}
    		}
        	else
        	{
        		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
        		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
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
        				priest.SelfMarry(player, args[args.length-1]); break;
        			case 3:
        				priest.SelfMarry(player, args[1], args[2]);
        		}
        	}
        	else
	    	{
	    		player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
	    	}
        }
        else if (args.length == 2 || (args.length == 3 && plugin.config.getSurname()))
        {
        	if(plugin.IsPriest(player))
    		{
    			priest.Marry(player, args);
    		}
    		else
    		{
    			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotAPriest"));
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
		player.sendMessage(ChatColor.YELLOW + "Marriage Master - " + plugin.lang.Get("Description.Commands"));
		if(plugin.CheckPerm(player, "marry.list"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry list" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.ListAll"));
		}
		if(plugin.IsPriest(player))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + Surname + ChatColor.WHITE + " - " + String.format(plugin.lang.Get("Description.Marry"), plugin.config.GetRange("Marry")));
			player.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + String.format(plugin.lang.Get("Description.Divorce"), plugin.config.GetRange("Marry")));
		}
		if(plugin.CheckPerm(player, "marry.tp"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry tp" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.TP"));
		}
		if(plugin.CheckPerm(player, "marry.home"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry home" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.TPHome"));
			player.sendMessage(ChatColor.AQUA + "/marry sethome" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.SetHome"));
		}
		if(plugin.CheckPerm(player, "marry.chat"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry chat <Message>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Chat"));
			player.sendMessage(ChatColor.AQUA + "/marry " + (plugin.config.UseAltChatToggleCommand() ? plugin.config.ChatToggleCommand() : "chat toggle") + ChatColor.WHITE + " - " + plugin.lang.Get("Description.ChatToggle"));
		}
		if(plugin.CheckPerm(player, "marry.pvpon") && plugin.config.GetAllowBlockPvP())
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpon" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.PvPOn"));
		}
		if(plugin.CheckPerm(player, "marry.pvpoff") && plugin.config.GetAllowBlockPvP())
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpoff" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.PvPOff"));
		}
		if(plugin.CheckPerm(player, "marry.gift"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry gift" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Gift"));
		}
		if(plugin.minepacks != null && plugin.CheckPerm(player, "marry.backpack"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry backpack" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Backpack"));
			if(plugin.DB.GetPartnerShareBackpack(player))
			{
				player.sendMessage(ChatColor.AQUA + "/marry backpack off" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.BackpackOff"));
			}
			else
			{
				player.sendMessage(ChatColor.AQUA + "/marry backpack on" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.BackpackOn"));
			}
		}
		if(plugin.CheckPerm(player, "marry.kiss") && plugin.config.GetKissEnabled())
		{
			player.sendMessage(ChatColor.AQUA + "/marry kiss" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Kiss"));
		}
		if(plugin.config.AllowSelfMarry() && plugin.CheckPerm(player, "marry.selfmarry"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername>" + Surname + ChatColor.WHITE + " - " + plugin.lang.Get("Description.SelfMarry"));
			player.sendMessage(ChatColor.AQUA + "/marry divorce" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.SelfDivorce"));
		}
		if(plugin.config.getSurname() && plugin.CheckPerm(player, "marry.changesurname"))
		{
			if(plugin.IsPriest(player))
			{
				player.sendMessage(ChatColor.AQUA + "/marry Surname <Playername> <Surname>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Surname"));
			}
			else
			{
				if(plugin.config.AllowSelfMarry() && plugin.CheckPerm(player, "marry.selfmarry"))
				{
					player.sendMessage(ChatColor.AQUA + "/marry Surname <Surname>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Surname"));
				}
			}
		}
		if(plugin.CheckPerm(player, "marry.setpriest", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry " + plugin.config.GetPriestCMD() + " <Playername>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Priest"));
		}
		if(plugin.CheckPerm(player, "marry.listenchat", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry listenchat" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.ListenChat"));
		}
		if(plugin.config.UseUpdater() && plugin.CheckPerm(player, "marry.update", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry update" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Update"));
		}
		if(plugin.CheckPerm(player, "marry.reload", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Reload"));
		}
	}
	
	private void Update(CommandSender sender)
	{
		if(plugin.Update())
		{
			sender.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.Updated"));
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + plugin.lang.Get("Ingame.NoUpdate"));
		}
	}
	
	private void ShowAvailableCmds(CommandSender sender)
	{
		String Surname = " <Surname>";
		if(!plugin.config.getSurname())
		{
			Surname = "";
		}
		sender.sendMessage(ChatColor.YELLOW + "Marriage Master - " + plugin.lang.Get("Description.Commands"));
		sender.sendMessage(ChatColor.AQUA + "/marry list <Page>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.ListAll"));
		sender.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + Surname + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Marry"));
		sender.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Divorce"));
		sender.sendMessage(ChatColor.AQUA + "/marry priest <Playername>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Priest"));
		if(plugin.config.getSurname())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry Surname <Playername> <Surname>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Surname"));
		}
		if(plugin.config.UseUpdater())
		{
			sender.sendMessage(ChatColor.AQUA + "/marry update" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Update"));
		}
		sender.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Reload"));
	}
}
