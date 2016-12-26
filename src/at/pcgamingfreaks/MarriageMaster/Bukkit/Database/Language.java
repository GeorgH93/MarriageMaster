/*
 *   Copyright (C) 2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper.OldFileUpdater;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

public class Language extends at.pcgamingfreaks.Bukkit.Language
{
	private static final int LANG_VERSION = 90, UPGRADE_THRESHOLD = 90, PRE_V2_VERSIONS = 90;

	public Language(JavaPlugin plugin)
	{
		super(plugin, LANG_VERSION, UPGRADE_THRESHOLD);
	}

	@Override
	protected void doUpdate() {}

	@Override
	protected void doUpgrade(at.pcgamingfreaks.Language oldLang)
	{
		if(oldLang.getVersion() < PRE_V2_VERSIONS)
		{
			OldFileUpdater.updateLanguage(oldLang.getLang(), getLang());
		}
		else
		{
			super.doUpgrade(oldLang);
		}
	}

	@Override
	public String getTranslated(String key)
	{
		return super.getTranslated(key).replaceAll("<heart>", ChatColor.RED + "\u2764").replaceAll("<smallheart>", ChatColor.RED + "\u2665");
	}

	public String getDialog(String key)
	{
		return lang.getString("Dialog." + key, "").replaceAll("<heart>", ChatColor.RED + "\u2764").replaceAll("<smallheart>", ChatColor.RED + "\u2665");
	}

	public String[] getCommandAliases(String command)
	{
		List<String> aliases = lang.getStringList("Command." + command, new LinkedList<String>());
		return (aliases.size() > 0) ? aliases.toArray(new String[aliases.size()]) : new String[0];
	}

	public String[] getSwitch(String key, String defaultSwitch)
	{
		List<String> switches = lang.getStringList("Command.Switches." + key, new LinkedList<String>());
		if(!switches.contains(defaultSwitch)) switches.add(defaultSwitch);
		return switches.toArray(new String[switches.size()]);
	}
}