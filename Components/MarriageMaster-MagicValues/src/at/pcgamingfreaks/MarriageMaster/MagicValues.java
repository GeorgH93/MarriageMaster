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

package at.pcgamingfreaks.MarriageMaster;

import java.io.InputStream;
import java.util.Properties;

public class MagicValues
{
	private static final char COLOR_CHAR = '\u00A7';
	public static final int LANG_VERSION, LANG_PRE_V2_VERSIONS = 90;
	public static final int CONFIG_VERSION, BUNGEE_CONFIG_VERSION, CONFIG_PRE_V2_VERSIONS = 90;
	public static final String SYMBOL_HEART = "\u2764", SYMBOL_SMALL_HEART = "\u2665", HEART_AND_RESET = SYMBOL_HEART + COLOR_CHAR + 'f', RED_HEART = COLOR_CHAR + 'c' + HEART_AND_RESET;
	public static final String MIN_PCGF_PLUGIN_LIB_VERSION;

	static
	{
		String pcgfPluginLibVersion = "99999", langVersion = "0", configVersion = "0", configVersionBungee = "0";

		try(InputStream propertiesStream = MagicValues.class.getClassLoader().getResourceAsStream("MarriageMaster.properties"))
		{
			Properties properties = new Properties();
			properties.load(propertiesStream);

			pcgfPluginLibVersion = properties.getProperty("PCGFPluginLibVersion");
			langVersion = properties.getProperty("LanguageFileVersion");
			configVersion = properties.getProperty("ConfigFileVersion");
			configVersionBungee = properties.getProperty("ConfigFileVersionBungee");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		MIN_PCGF_PLUGIN_LIB_VERSION = pcgfPluginLibVersion;
		LANG_VERSION = Integer.parseInt(langVersion);
		CONFIG_VERSION = Integer.parseInt(configVersion);
		BUNGEE_CONFIG_VERSION = Integer.parseInt(configVersionBungee);
	}
}