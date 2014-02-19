package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import java.util.Map;
import java.util.NavigableMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.Commands.*;
import at.pcgamingfreaks.georgh.MarriageMaster.*;

public class OnCommand implements CommandExecutor 
{
	private MarriageMaster marriageMaster;

	private MarryTp marryTp;
	private Priester priester;
	private Home home;
	
	public OnCommand(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		
		home = new Home(marriageMaster);
		marryTp = new MarryTp(marriageMaster);
		priester = new Priester(marriageMaster);
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) 
	{
		Player player = null;
		if (sender instanceof Player) 
		{
			player = (Player) sender;
	    }
		else
		{
			sender.sendMessage(marriageMaster.lang.Get("Console.NotFromConsole"));
			return true;
		}
		
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) 
        {
        	ShowAvailableCmds(player);
        }
        else if (args[0].equalsIgnoreCase("reload"))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.reload"))
        	{
        		marriageMaster.config.Reload();
        		player.sendMessage(ChatColor.BLUE + "Reloaded");
        	}
        	else
	    	{
	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
	    	}
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.list"))
    		{
    			NavigableMap<String, String> map = marriageMaster.DB.LoadAllMarriedPlayers();

    			if(map.size() > 0)
    			{
    				player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListHL"));
    				
    				for(Map.Entry<String, String> item : map.entrySet())
    				{
    					player.sendMessage(ChatColor.GREEN + item.getKey() + " + " + item.getValue());
    				}	
    			}	
    			else
    			{
    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoMarriedPlayers"));
    			}
    		}
        	else
	    	{
	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
	    	}
        }        
        else if (args[0].equalsIgnoreCase("pvpon")) 
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.pvpon"))
    	    	{
    	    		String partner = marriageMaster.DB.GetPartner(player.getName());
    	    		marriageMaster.DB.SetPvPEnabled(player.getName(), partner, true);
    	    		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PvPOn"));
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if (args[0].equalsIgnoreCase("pvpoff"))
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.pvpoff"))
    	    	{
    	    		String partner = marriageMaster.DB.GetPartner(player.getName());
    	    		marriageMaster.DB.SetPvPEnabled(player.getName(), partner, false);
    	    		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PvPOff"));
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if(args[0].equalsIgnoreCase("home"))
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.home"))
    	    	{
    	        	home.TP(player);
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if(args[0].equalsIgnoreCase("sethome"))
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.home"))
    	    	{
    	        	home.SetHome(player);
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if (args[0].equalsIgnoreCase("tp"))
        {
        	if(marriageMaster.HasPartner(player.getName()) && marriageMaster.config.CheckPerm(player, "marry.tp"))
    		{
    	        marryTp.TP(player);
        	}
    		else
    		{
    			if(marriageMaster.config.CheckPerm(player, "marry.tp"))
    			{
    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    			}
    			else
    			{
    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    			}
    		}
        }
        else if(args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c"))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.chat"))
    		{
        		if(marriageMaster.HasPartner(player.getName()))
        		{
        			Player otP = marriageMaster.getServer().getPlayer(marriageMaster.DB.GetPartner(player.getName()));
        			if(otP != null && otP.isOnline())
        			{
        				String msg = player.getName() + " => " + otP.getName() + ":";
        				for(int i = 1; i < args.length; i++)
        				{
        					msg += " " + args[i];
        				}
        				otP.sendMessage(msg);
        				player.sendMessage(msg);
        			}
        			else
        			{
        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
        			}
        		}
        		else
        		{
        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
        		}
    		}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
        	}
        }
        else if(args.length == 2 && args[0].equalsIgnoreCase("priest") && player.isOp())
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.setpriest"))
        	{
        		priester.setPriester(args, player);
        	}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
        	}
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("divorce"))
        {
        	if(marriageMaster.IsPriester(player))
        	{
        		priester.Divorce(player, args);
        	}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Description.NotAPriest"));
        	}
        }
        else if (args.length == 2)
        {        	
        	priester.Marry(player, args);
        }
        return true;
	}
	
	public void ShowAvailableCmds(Player player)
	{
		player.sendMessage(ChatColor.YELLOW + "Marriage Master - " + marriageMaster.lang.Get("Description.Commands"));
		if(marriageMaster.config.CheckPerm(player, "marry.list"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry list" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListAll"));
		}
		if(marriageMaster.IsPriester(player))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Marry"));
			player.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Divorce"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.tp"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry tp" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.TP"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.home"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry home" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.TPHome"));
			player.sendMessage(ChatColor.AQUA + "/marry sethome" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.SetHome"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.chat"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry chat" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Chat"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.pvpon"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpon" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.PvPOn"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.pvpoff"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpoff" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.PvPOff"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.setpriest", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry priest <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Priest"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.reload", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Reload"));
		}
	}
}
