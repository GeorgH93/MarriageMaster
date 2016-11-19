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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Database
{
	protected MarriageMaster plugin;
	
	public Database(MarriageMaster marriagemaster) { plugin = marriagemaster; }
	
	public void disable() {}
	
	public void updatePlayer(ProxiedPlayer player) {}
	
	public String getPartner(ProxiedPlayer player) { return null; }
	
	public UUID getPartnerUUID(ProxiedPlayer player) { return null; }
	
	public ProxiedPlayer getPartnerPlayer(ProxiedPlayer player)
	{
		UUID partner = getPartnerUUID(player);
		if(partner != null)
		{
			return plugin.getProxy().getPlayer(partner);
		}
		return null;
	}
	
	public String getHomeServer(ProxiedPlayer player) { return null; }
	
	public String limitText(String text, int len)
	{
		if(text != null && text.length() > len)
		{
			return text.substring(0, len);
		}
		return text;
	}
}