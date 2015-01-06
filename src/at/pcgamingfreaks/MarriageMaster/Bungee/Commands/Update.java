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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Update extends BaseCommand
{
	private BaseComponent[] Message_Updated, Message_NoUpdate, Message_CheckingForUpdates;
	
	public Update(MarriageMaster MM)
	{
		super(MM);
		
		// Load Messages
		Message_CheckingForUpdates	= plugin.lang.getReady("Ingame.CheckingForUpdates");
		Message_Updated				= plugin.lang.getReady("Ingame.Updated");
		Message_NoUpdate			= plugin.lang.getReady("Ingame.NoUpdate");
	}
	
	public boolean execute(final ProxiedPlayer sender, String cmd, String[] args)
	{
		if(sender.hasPermission("marry.update"))
		{
			sender.sendMessage(Message_CheckingForUpdates);
			plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() { @Override
				public void run()
				{
					if(plugin.Update())
					{
						sender.sendMessage(Message_Updated);
						plugin.broadcastPluginMessage("update"); // Send update through plugin channel to all servers
					}
					else
					{
						sender.sendMessage(Message_NoUpdate);
					}
				}});
		}
		else
		{
			sender.sendMessage(plugin.Message_NoPermission);
		}
		return true;
	}
}