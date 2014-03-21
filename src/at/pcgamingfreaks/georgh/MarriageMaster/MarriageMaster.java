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
import java.util.List;
import java.util.logging.Logger;

import net.gravitydevelopment.updater.Updater;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import at.pcgamingfreaks.georgh.MarriageMaster.Commands.Kiss;
import at.pcgamingfreaks.georgh.MarriageMaster.Databases.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Economy.HEconomy;
import at.pcgamingfreaks.georgh.MarriageMaster.Listener.*;

public class MarriageMaster extends JavaPlugin
{
	public Logger log;
    public HEconomy economy = null;
    public Permission perms = null;
    public Kiss kiss = null;
    public Config config;
    public Language lang;
    public Database DB;
    public List<Player> pcl;
    public List<Marry_Requests> mr;
    
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
		DB = new Database(this);
		
		pcl = new ArrayList<Player>();
		mr = new ArrayList<Marry_Requests>();
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
			@SuppressWarnings("unused")
			Updater updater = new Updater(this, 74734, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}
		if(config.UsePermissions())
		{
			if(!setupPermissions())
			{
				config.SetPermissionsOff();
				log.info(lang.Get("Console.NoPermPL"));
			}
		}
		if(config.UseEconomy())
		{
			economy = new HEconomy(this);
		}
		
		// Events Registrieren
		getCommand("marry").setExecutor(new OnCommand(this));
		getServer().getPluginManager().registerEvents(new JoinLeave(this), this);
		RegisterEvents();

		this.log.info(lang.Get("Console.Enabled"));
	}
	
	public void RegisterEvents()
	{
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
		this.log.info(lang.Get("Console.Disabled"));
	}
	
	public boolean IsPriester(Player player)
	{
		return config.CheckPerm(player, "marry.priest", false) || DB.IsPriester(player.getName());
	}
	
	public boolean HasPartner(String Playername)
	{
		String P = DB.GetPartner(Playername);
		return (P != null && !P.equalsIgnoreCase(""));
	}
	
	public boolean InRadius(Player player, Player otherPlayer, int radius) 
	{
		Location pl = player.getLocation();
		Location opl = otherPlayer.getLocation();
		
		if(pl.getWorld().equals(opl.getWorld()) && pl.distance(opl) <= radius)
		{
			return true;
		}
		
		return false;
	}
}
