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

package at.pcgamingfreaks.georgh.MarriageMaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.gravitydevelopment.ModUpdater.Updater;
import net.gravitydevelopment.ModUpdater.Updater.UpdateResult;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import at.pcgamingfreaks.georgh.MarriageMaster.Commands.Kiss;
import at.pcgamingfreaks.georgh.MarriageMaster.Commands.MarryChat;
import at.pcgamingfreaks.georgh.MarriageMaster.Databases.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Economy.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Listener.*;

public class MarriageMaster extends JavaPlugin
{
	public Logger log;
    public BaseEconomy economy = null;
    public Permission perms = null;
    public Kiss kiss = null;
    public MarryChat chat = null;
    public Config config;
    public Language lang;
    public Database DB;
    public String DBType = "";
    public List<Marry_Requests> mr;
    public HashMap<Player, Player> dr;
    
    public boolean setupPermissions()
    {
    	if(getServer().getPluginManager().getPlugin("Vault") == null)
    	{
    		return false;
    	}
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
        {
        	perms = permissionProvider.getProvider();
        }
        return (perms != null);
    }

	public void onEnable()
	{
		log = getLogger();
		config = new Config(this);
		lang = new Language(this);
		DBType = config.GetDatabaseType().toLowerCase();
		switch(DBType)
		{
			case "mysql": DB = new MySQL(this); break;
			default: DB = new Files(this); break;
		}
		kiss = new Kiss(this);
		chat = new MarryChat(this);
		mr = new ArrayList<Marry_Requests>();
		dr = new HashMap<Player, Player>();
		
		if(config.UseMetrics())
		{
			try
			{
			    Metrics metrics = new Metrics(this);
			    metrics.start();
			}
			catch (IOException e)
			{
			    log.info(lang.Get("Console.MetricsOffline"));
			}
		}
		if(config.UseUpdater())
		{
			Update();
		}
		if(config.UsePermissions())
		{
			if(!setupPermissions())
			{
				config.SetPermissionsOff();
				log.info(lang.Get("Console.NoPermPL"));
			}
		}
		RegisterEconomy();
		
		// Events Registrieren
		getCommand("marry").setExecutor(new OnCommand(this));
		RegisterEvents();

		log.info(lang.Get("Console.Enabled"));
	}
	
	public void RegisterEconomy()
	{
		if(config.UseEconomy() && getServer().getPluginManager().getPlugin("Vault") != null)
		{
			String[] vaultV = getServer().getPluginManager().getPlugin("Vault").getDescription().getVersion().split(Pattern.quote( "." ));
			try
			{
				if(Integer.parseInt(vaultV[0]) > 1 || (Integer.parseInt(vaultV[0]) == 1 && Integer.parseInt(vaultV[1]) >= 4))
				{
					economy = new Economy(this);
				}
				else
				{
					economy = new EconomyOld(this);
				}
			}
			catch(Exception e){}
		}
	}
	
	public void RegisterEvents()
	{
		getServer().getPluginManager().registerEvents(new JoinLeaveChat(this), this);
		if(config.GetAllowBlockPvP())
		{
			getServer().getPluginManager().registerEvents(new Damage(this), this);
		}
		if(config.GetHealthRegainEnabled())
		{
			getServer().getPluginManager().registerEvents(new RegainHealth(this), this);
		}
		if(config.GetBonusXPEnabled())
		{
			getServer().getPluginManager().registerEvents(new Death(this), this);
		}
		if(config.GetKissEnabled())
		{
			getServer().getPluginManager().registerEvents(new InteractEntity(this),this);
		}
	}
	 
	public void onDisable()
	{ 
		log.info(lang.Get("Console.Disabled"));
	}
	
	public boolean IsPriest(Player player)
	{
		return config.CheckPerm(player, "marry.priest", false) || DB.IsPriest(player);
	}
	
	public boolean HasPartner(Player player)
	{
		String P = DB.GetPartner(player);
		return (P != null && !P.equalsIgnoreCase(""));
	}
	
	public boolean InRadius(Player player, Player otherPlayer, double radius) 
	{
		Location pl = player.getLocation();
		Location opl = otherPlayer.getLocation();
		if(pl.getWorld().equals(opl.getWorld()) && (pl.distance(opl) <= radius || radius <= 0))
		{
			return true;
		}
		return false;
	}
	
	public boolean InRadiusAllWorlds(Player player, Player otherPlayer, double radius)
	{
		Location pl = player.getLocation();
		Location opl = otherPlayer.getLocation();
		if((pl.getWorld().equals(opl.getWorld()) && pl.distance(opl) <= radius) || (pl.getWorld().equals(opl.getWorld()) && radius == 0) || radius < 0)
		{
			return true;
		}
		return false;
	}
	
	public boolean Update()
	{
		Updater updater = new Updater(this, 74734, this.getFile(), Updater.UpdateType.DEFAULT, true);
		if(updater.getResult() == UpdateResult.SUCCESS)
		{
			return true;
		}
		return false;
	}
}
