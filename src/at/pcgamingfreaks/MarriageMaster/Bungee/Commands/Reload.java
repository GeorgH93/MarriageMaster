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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Reload extends BaseCommand
{
	public Reload(MarriageMaster MM)
	{
		super(MM);
	}
	
	public boolean execute(ProxiedPlayer sender, String cmd, String[] args)
	{
		if(sender.hasPermission("marry.reload"))
		{
			plugin.Disable();
			plugin.PluginLoad();
			plugin.broadcastPluginMessage("reload"); // Send reload throu plugin channel to all servers
			sender.sendMessage(new TextComponent(ChatColor.BLUE + "Reloaded!"));
		}
		else
		{
			sender.sendMessage(plugin.Message_NoPermission);
		}
		return true;
	}
}