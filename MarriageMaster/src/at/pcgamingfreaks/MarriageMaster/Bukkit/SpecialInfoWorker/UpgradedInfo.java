/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UpgradedInfo extends SpecialInfoBase
{
	private final Message messagePluginUpgraded;

	public UpgradedInfo(MarriageMaster plugin)
	{
		super(plugin, Permissions.RELOAD);
	    Bukkit.getPluginManager().registerEvents(this, plugin);
		// I don't load this message from the config. He just upgraded, the messages in the config would probably be exactly the same.
		messagePluginUpgraded = new MessageBuilder("Marriage Master", MessageColor.GOLD).append(" has been upgraded to ", MessageColor.AQUA)
				.append(MarriageMaster.getInstance().getDescription().getVersion(), MessageColor.BLUE, MessageFormat.BOLD).append(".", MessageColor.AQUA).appendNewLine()
				.append("Your config and language files have been backuped, since they are not compatible with this version of the plugin!", MessageColor.AQUA).appendNewLine()
				.append("You can try to use v2.5 to convert them to the new format or apply your changes manually.", MessageColor.AQUA).getMessage();
	}

	@Override
	protected void sendMessage(Player player)
	{
		messagePluginUpgraded.send(player);
	}
}