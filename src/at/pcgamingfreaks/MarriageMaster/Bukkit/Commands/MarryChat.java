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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class MarryChat 
{
	private MarriageMaster plugin;
	public List<Player> pcl;
	public List<Player> Marry_ChatDirect;
	private String format;
	
	public MarryChat(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		pcl = new ArrayList<Player>();
		Marry_ChatDirect = new ArrayList<Player>();
		format = plugin.config.getChatPrivateFormat();
	}

	public void Chat(Player sender, Player reciver, String msg)
	{
		if(reciver != null && reciver.isOnline())
		{
			msg = msg.replace('§', '&');
			if(plugin.CheckPerm(sender, "marry.chat.color"))
			{
				msg = ChatColor.translateAlternateColorCodes('&', msg);
			}
			if(plugin.CheckPerm(sender, "marry.chat.format"))
			{
				msg = msg.replaceAll("&l", "§l").replaceAll("&m", "§m").replaceAll("&n", "§n").replaceAll("&o", "§o").replaceAll("&r", "§r");
			}
			else
			{
				msg = msg.replaceAll("§l", "&l").replaceAll("§m", "&m").replaceAll("§n", "&n").replaceAll("§o", "&o").replaceAll("§r", "&r");
			}
			if(plugin.CheckPerm(sender, "marry.chat.magic", false))
			{
				msg = msg.replaceAll("&k", "§k");
			}
			else
			{
				msg = msg.replaceAll("§k", "&k");
			}
			msg = String.format(format, sender.getDisplayName(), reciver.getDisplayName(), msg);
			reciver.sendMessage(msg);
			sender.sendMessage(msg);
			for (Player play : pcl)
			{
				play.sendMessage(msg);
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerOffline"));
		}
	}
}
