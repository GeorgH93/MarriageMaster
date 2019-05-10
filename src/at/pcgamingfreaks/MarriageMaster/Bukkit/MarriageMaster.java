/*
 *   Copyright (C) 2014-2018 GeorgH93
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.Kiss;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryChat;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryTp;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Databases.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Economy.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Minepacks.MinePacksIntegrationBase;
import at.pcgamingfreaks.MarriageMaster.Updater.UpdateResult;

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
    public MinePacksIntegrationBase minepacks = null;
    public String HomeServer = null;

    @Override
    public void onEnable()
	{
		log = getLogger();
		if (System.getProperty("java.version").startsWith("1.7"))
		{
			log.warning("You are still using Java 1.7. Java 1.7 ist EOL for over a year now! You should really update to Java 1.8!");
			log.info("For now I this plugin will still work fine with Java 1.7 but no warranty that this won't change in the future.");
		}
		config = new Config(this);
		load();
		log.info(lang.get("Console.Enabled"));
	}
    
    public void load()
    {
		if(!config.Loaded())
		{
			setEnabled(false);
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
		mr = new ArrayList<>();
		bdr = new ArrayList<>();
		dr = new HashMap<>();
		if(config.UseUpdater())
		{
			update();
		}
		UsePerms = config.getUsePermissions();
		if(config.getUseVaultPermissions())
		{
			if(!setupPermissions())
			{
				log.info(lang.get("Console.NoPermPL"));
			}
		}
		economy = BaseEconomy.getEconomy(this);
		if(config.getUseMinepacks()) minepacks = MinePacksIntegrationBase.getIntegration();
		if(config.getUseBungeeCord()) pluginchannel = new PluginChannel(this);
		// Register events
		getCommand("marry").setExecutor(new OnCommand(this));
		registerEvents();
    }
    
    public void reload()
	{
		disable();
		config.Reload();
		load();
	}
    
    public void disable()
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
	
	public void registerEvents()
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
			getServer().getPluginManager().registerEvents(new BonusXP(this), this);
		}
		if(config.GetKissEnabled() && config.isKissInteractEnabled())
		{
			getServer().getPluginManager().registerEvents(new InteractEntity(this), this);
		}
	}
	 
	@Override
	public void onDisable()
	{
		Updater updater = null;
		if(config.UseUpdater())
		{
			log.info("Checking for updates ...");
			updater = new Updater(this, this.getFile(), true, 74734);
			updater.update();
		}
		disable();
		if(updater != null)
		{
			updater.waitForAsyncOperation();
		}
		log.info(lang.get("Console.Disabled"));
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
		return pl.getWorld().equals(opl.getWorld()) && (pl.distance(opl) <= radius || radius <= 0 || CheckPerm(player, "marry.bypassrangelimit", false));
	}
	
	public boolean InRadiusAllWorlds(Player player, Player otherPlayer, double radius)
	{
		return radius < 0 || InRadius(player, otherPlayer, radius);
	}
	
	public void update(final Player sender)
	{
		log.info("Checking for updates ...");
		sender.sendMessage(ChatColor.BLUE + lang.get("Ingame.CheckingForUpdates"));
		Updater updater = new Updater(this, this.getFile(), true, 74734);
		updater.update(new at.pcgamingfreaks.MarriageMaster.Updater.Updater.UpdaterResponse()
		{
			@Override
			public void onDone(UpdateResult result)
			{
				if(result == UpdateResult.UPDATE_AVAILABLE_V2)
				{
					if(MarriageMasterV2IsAvailable.instance == null) new MarriageMasterV2IsAvailable(MarriageMaster.this);
					MarriageMasterV2IsAvailable.instance.announce(sender);
				}
				else if(result == UpdateResult.SUCCESS)
				{
					sender.sendMessage(ChatColor.GREEN + lang.get("Ingame.Updated"));
				}
				else
				{
					sender.sendMessage(ChatColor.GOLD + lang.get("Ingame.NoUpdate"));
				}
			}
		});
	}
	
	public void update()
	{
		log.info("Checking for updates ...");
		Updater updater = new Updater(this, this.getFile(), true, 74734);
		updater.update(new at.pcgamingfreaks.MarriageMaster.Updater.Updater.UpdaterResponse()
		{
			@Override
			public void onDone(UpdateResult result)
			{
				if(result == UpdateResult.UPDATE_AVAILABLE_V2)
				{
					new MarriageMasterV2IsAvailable(MarriageMaster.this);
				}
			}
		});
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