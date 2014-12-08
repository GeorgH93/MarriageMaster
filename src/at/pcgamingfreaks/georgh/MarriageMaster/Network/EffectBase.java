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

package at.pcgamingfreaks.georgh.MarriageMaster.Network;

import org.bukkit.Location;

public class EffectBase
{
	protected static Class<?>	PacketPlayOutParticle = NMS.getNMSClass("PacketPlayOutWorldParticles");
	
	public void SpawnParticle(Location loc, Effects type, double visrange, int count, float offsetX, float offsetY, float offsetZ, float speed) throws Exception {}
}