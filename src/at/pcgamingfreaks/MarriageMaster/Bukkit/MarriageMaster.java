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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.gravitydevelopment.Updater.Bukkit_Updater;
import net.gravitydevelopment.Updater.UpdateResult;
import net.gravitydevelopment.Updater.UpdateType;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.Kiss;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryChat;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryTp;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Databases.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Economy.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.*;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class MarriageMaster extends JavaPlugin
{
	public Logger log;
    public BaseEconomy economy = null;
    public Permission perms = null;
    public PluginChannel pluginchannel = null;
    public Kiss kiss = null;
    public Home home = null;
    public MarryTp tp = null;
    public MarryChat chat = null;
    public Config config;
    public Language lang;
    public boolean UseUUIDs = false;
    public boolean UsePerms = true;
    public Database DB;
    public String DBType = "";
    public List<Marry_Requests> mr;
    public List<Marry_Requests> bdr;
    public HashMap<Player, Player> dr;
    public MinePacks minepacks = null;
    public String HomeServer = null;
    
    public void onEnable()
	{
		log = getLogger();
		Load();
		log.info(lang.Get("Console.Enabled"));
	}
    
    public void Load()
    {
    	config = new Config(this);
		if(!config.Loaded())
		{
			this.setEnabled(false);
			log.warning("Failed loading config! Disabling Plugin.");
			return;
		}
		log.info("Config loaded");
		lang = new Language(this);
		DBType = config.GetDatabaseType();
		UseUUIDs = config.getUseUUIDs();
		DB = Database.getDatabase(DBType, this);
		kiss = new Kiss(this);
		home = new Home(this);
		tp = new MarryTp(this);
		chat = new MarryChat(this);
		mr = new ArrayList<Marry_Requests>();
		if(config.getConfirmationBothDivorce())
		{
			bdr = new ArrayList<Marry_Requests>();
		}
		else
		{
			dr = new HashMap<Player, Player>();
		}
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
		UsePerms = config.getUsePermissions();
		if(config.getUseVaultPermissions())
		{
			if(!setupPermissions())
			{
				log.info(lang.Get("Console.NoPermPL"));
			}
		}
		economy = BaseEconomy.GetEconomy(this);
		if(config.getUseMinepacks())
		{
			setupMinePacks();
		}
		if(config.getUseBungeeCord())
		{
			pluginchannel = new PluginChannel(this);
		}
		// Events Registrieren
		getCommand("marry").setExecutor(new OnCommand(this));
		RegisterEvents();
    }
    
    public void reload()
	{
		Disable();
		Load();
	}
    
    public void Disable()
    {
    	
    	HandlerList.unregisterAll(this);
    	getServer().getMessenger().unregisterIncomingPluginChannel(this);
    	getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    	DB.Disable();
    }
    
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
    
    public boolean setupMinePacks()
    {
    	if(getServer().getPluginManager().getPlugin("MinePacks") == null)
    	{
    		return false;
    	}
        RegisteredServiceProvider<MinePacks> mpProvider = getServer().getServicesManager().getRegistration(at.pcgamingfreaks.georgh.MinePacks.MinePacks.class);
        if (mpProvider != null)
        {
        	minepacks = mpProvider.getProvider();
        }
        return (minepacks != null);
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
			getServer().getPluginManager().registerEvents(new InteractEntity(this), this);
		}
		if(config.getUseBungeeCord())
		{
			getServer().getMessenger().registerIncomingPluginChannel(this, "MarriageMaster", pluginchannel);
			getServer().getMessenger().registerOutgoingPluginChannel(this, "MarriageMaster");
			getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pluginchannel);
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}
	}
	 
	public void onDisable()
	{
		if(config.UseUpdater())
		{
			Update();
		}
		Disable();
		log.info(lang.Get("Console.Disabled"));
	}
	
	public boolean IsPriest(Player player)
	{
		return CheckPerm(player, "marry.priest", false) || DB.IsPriest(player);
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
		if(pl.getWorld().equals(opl.getWorld()) && (pl.distance(opl) <= radius || radius <= 0 || CheckPerm(player, "marry.bypassrangelimit", false)))
		{
			return true;
		}
		return false;
	}
	
	public boolean InRadiusAllWorlds(Player player, Player otherPlayer, double radius)
	{
		Location pl = player.getLocation();
		Location opl = otherPlayer.getLocation();
		if(radius < 0 || (pl.getWorld().equals(opl.getWorld()) && (pl.distance(opl) <= radius || radius == 0 || CheckPerm(player, "marry.bypassrangelimit", false))))
		{
			return true;
		}
		return false;
	}
	
	public void AsyncUpdate(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.BLUE + lang.Get("Ingame.CheckingForUpdates"));
		getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
			{
				@Override
				public void run()
				{
					if(Update())
					{
						sender.sendMessage(ChatColor.GREEN + lang.Get("Ingame.Updated"));
					}
					else
					{
						sender.sendMessage(ChatColor.GOLD + lang.Get("Ingame.NoUpdate"));
					}
				}
			});
	}
	
	public boolean Update()
	{
		Bukkit_Updater updater = new Bukkit_Updater(this, 74734, this.getFile(), UpdateType.DEFAULT, true);
		if(updater.getResult() == UpdateResult.SUCCESS)
		{
			return true;
		}
		return false;
	}
	
	public boolean CheckPerm(Player player, String Perm)
	{
		return CheckPerm(player,Perm, true);
	}
	
	public boolean CheckPerm(Player player,String Perm, boolean def)
	{
		if(player.isOp())
		{
			return true;
		}
		if(UsePerms)
		{
			if(perms != null)
			{
				return perms.has(player, Perm);
			}
			return player.hasPermission(Perm);
		}
		return def;
	}
}