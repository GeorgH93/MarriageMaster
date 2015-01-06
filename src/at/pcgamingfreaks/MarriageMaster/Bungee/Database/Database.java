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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import java.util.TreeMap;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Database
{
	protected MarriageMaster plugin;
	
	public Database(MarriageMaster marriagemaster) { plugin = marriagemaster; }
	
	public void Recache() {}
	
	public void Disable() {}
	
	public void UpdatePlayer(ProxiedPlayer player) {}
	
	public void DivorcePlayer(ProxiedPlayer player) {}
	
	public void MarryPlayers(ProxiedPlayer player1, ProxiedPlayer player2, String priest, String surname) {}
	
	public void MarryPlayers(ProxiedPlayer player1, ProxiedPlayer player2, ProxiedPlayer priest, String surname) { MarryPlayers(player1, player2, priest.getName(), surname); }
	
	public void SetPriest(ProxiedPlayer player) {}
	
	public void DelPriest(ProxiedPlayer player) {}
	
	public boolean IsPriest(ProxiedPlayer player) { return false; }
	
	public String GetPartner(ProxiedPlayer player) { return null; }
	
	public UUID GetPartnerUUID(ProxiedPlayer player) { return null; }
	
	public ProxiedPlayer GetPlayerPartner(ProxiedPlayer player)
	{
		UUID partner = GetPartnerUUID(player);
		if(partner != null)
		{
			return plugin.getProxy().getPlayer(partner);
		}
		return null;
	}
	
	public String GetSurname(ProxiedPlayer player) { return null; }
	
	public void SetSurname(ProxiedPlayer player, String Surname) {}
	
	public void SetShareBackpack(ProxiedPlayer player, boolean allow) {}
	
	public boolean GetPartnerShareBackpack(ProxiedPlayer player) { return false; }
	
	public TreeMap<String, String> GetAllMarriedPlayers() { return null;}
}