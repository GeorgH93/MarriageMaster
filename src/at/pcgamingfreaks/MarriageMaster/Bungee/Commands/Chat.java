/*
 *   Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import java.util.ArrayList;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Chat extends BaseCommand
{
	private String tcc, format;
	
	private BaseComponent[] Message_ChatJoined, Message_ChatLeft, Message_ChatListeningStarted, Message_ChatListeningStoped;
	
	public ArrayList<ProxiedPlayer> DirectChat = new ArrayList<ProxiedPlayer>();
	public ArrayList<ProxiedPlayer> ChatListener = new ArrayList<ProxiedPlayer>();
	
	public Chat(MarriageMaster MM)
	{
		super(MM);
		
		tcc = plugin.config.getChatToggleCommand();
		format = plugin.config.getChatFormat();
		
		// Load Messages
		Message_ChatJoined			 = plugin.lang.getReady("Ingame.ChatJoined");
		Message_ChatLeft			 = plugin.lang.getReady("Ingame.ChatLeft");
		Message_ChatListeningStarted = plugin.lang.getReady("Ingame.ChatListeningStarted");
		Message_ChatListeningStoped  = plugin.lang.getReady("Ingame.ChatListeningStoped");
	}
	
	public boolean execute(ProxiedPlayer player, String cmd, String[] args)
	{
		if(player.hasPermission("marry.chat"))
		{
			if(cmd.equals("ctoggle") || cmd.equals("chattoggle") || cmd.equals(tcc) || (args.length > 0 && args[0].equalsIgnoreCase("toggle")))
			{
				UUID partner = plugin.DB.GetPartnerUUID(player);
				if(partner != null)
	    		{
					ProxiedPlayer partnerPlayer = plugin.getProxy().getPlayer(partner);
					if(partnerPlayer != null)
					{
						if(DirectChat.contains(player))
						{
							player.sendMessage(Message_ChatLeft);
	    					plugin.chat.DirectChat.remove(player);
						}
						else
						{
							player.sendMessage(Message_ChatJoined);
	    					plugin.chat.DirectChat.add(player);
						}
					}
					else
					{
						player.sendMessage(plugin.Message_PartnerOffline);
					}
	    		}
				else
	    		{
	    			player.sendMessage(plugin.Message_NotMarried);
	    		}
			}
			else if(cmd.equals("listenchat"))
			{
				if(player.hasPermission("marry.listenchat"))
	    		{
	        		if(!ChatListener.contains(player))
	        		{
	        			ChatListener.add(player);
	        			player.sendMessage(Message_ChatListeningStarted);
	        		}
	        		else
	        		{
	        			ChatListener.remove(player);
	        			player.sendMessage(Message_ChatListeningStoped);
	        		}
				}
		    	else
		    	{
		    		player.sendMessage(plugin.Message_NoPermission);
		    	}
			}
			else if(args.length >= 1)
			{
				UUID partner = plugin.DB.GetPartnerUUID(player);
				if(partner != null)
	    		{
					ProxiedPlayer partnerPlayer = plugin.getProxy().getPlayer(partner);
					String msg = "";
        			for(int i = 0; i < args.length; i++)
    				{
    					msg += args[i] + " ";
    				}
        			SendChat(player, partnerPlayer, msg);
				}
				else
	    		{
	    			player.sendMessage(plugin.Message_NotMarried);
	    		}
    		}
			else
			{
				player.sendMessage(new TextComponent("/marry chat <Message>"));
			}
		}
		else
		{
			player.sendMessage(plugin.Message_NoPermission);
		}
		return true;
	}
	
	public void SendChat(ProxiedPlayer sender, ProxiedPlayer reciver, String msg)
	{
		if(reciver != null)
		{
			msg = msg.replace('§', '&');
			if(sender.hasPermission("marry.chat.color"))
			{
				msg = ChatColor.translateAlternateColorCodes('&', msg);
			}
			if(sender.hasPermission("marry.chat.format"))
			{
				msg = msg.replaceAll("&l", "§l").replaceAll("&m", "§m").replaceAll("&n", "§n").replaceAll("&o", "§o").replaceAll("&r", "§r");
			}
			else
			{
				msg = msg.replaceAll("§l", "&l").replaceAll("§m", "&m").replaceAll("§n", "&n").replaceAll("§o", "&o").replaceAll("§r", "&r");
			}
			if(sender.hasPermission("marry.chat.magic"))
			{
				msg = msg.replaceAll("&k", "§k");
			}
			else
			{
				msg = msg.replaceAll("§k", "&k");
			}
			BaseComponent[] sendmsg = TextComponent.fromLegacyText(String.format(format, sender.getDisplayName(), reciver.getDisplayName(), msg));
			reciver.sendMessage(sendmsg);
			sender.sendMessage(sendmsg);
			for (ProxiedPlayer player : ChatListener)
			{
				player.sendMessage(sendmsg);
			}
		}
		else
		{
			sender.sendMessage(plugin.Message_PartnerOffline);
		}
	}
	
	public void PlayerLeave(ProxiedPlayer player)
	{
		DirectChat.remove(player);
		ChatListener.remove(player);
	}
	
	public boolean CheckDirectChat(ProxiedPlayer player, String text)
	{
		if(DirectChat.contains(player))
		{
			ProxiedPlayer partner = plugin.DB.GetPartnerPlayer(player);
			if(partner != null)
			{
				SendChat(player, partner, text);
				return true;
			}
		}
		return false;
	}
}