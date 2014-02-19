package at.pcgamingfreaks.georgh.MarriageMaster;

import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import at.pcgamingfreaks.georgh.MarriageMaster.Databases.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Economy.HEconomy;
import at.pcgamingfreaks.georgh.MarriageMaster.Listener.*;

public class MarriageMaster extends JavaPlugin
{
	public Logger log;
    public HEconomy economy;
    public Permission perms;
    public Config config;
    public Language lang;
    public Database DB;

	public void onEnable()
	{			
		this.log = getLogger();

		this.setupPermissions();
		config = new Config(this);
		lang = new Language(this);
		DB = new Database(this);
		
		// Events Registrieren
		getCommand("marry").setExecutor(new OnCommand(this));
		if(config.GetInformOnPartnerJoinEnabled())
		{
			getServer().getPluginManager().registerEvents(new JoinLeave(this), this);
		}
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

		this.log.info(lang.Get("Console.Enabled"));
	}
	 
	public void onDisable()
	{ 
		this.log.info(lang.Get("Console.Disabled"));
	}
	  
	private boolean setupPermissions() 
	{
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        this.perms = rsp.getProvider();
        return this.perms != null;
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
}
