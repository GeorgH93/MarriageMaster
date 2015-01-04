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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Economy;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import net.milkbowl.vault.economy.Economy;

public class BaseEconomy 
{
	protected MarriageMaster plugin;
	protected double Costs_TP, Costs_SetHome, Costs_Home, Costs_Gift, Costs_Marry, Costs_Divorce;
	protected String Message_NotEnough, Message_PartnerNotEnough, Message_MarriagePaid, Message_DivorcePaid, Message_DivNotEnoPriestI, Message_NotEnoughPriestInfo;
	protected String Message_HomeTPPaid, Message_SetHomePaid, Message_GiftPaid, Message_TPPaid;
    protected Economy econ = null;
    
    private boolean setupEconomy()
    {
    	if(plugin.getServer().getPluginManager().getPlugin("Vault") == null)
    	{
    		return false;
    	}
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
        {
        	econ = economyProvider.getProvider();
        }
        return (econ != null);
    }
	
	public BaseEconomy(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		
		if(plugin.config.UseEconomy() && !setupEconomy())
		{
			plugin.log.info("Console.NoEcoPL");
			plugin.economy = null;
			return;
		}
		
		// Load Costs
		Costs_Divorce	= plugin.config.GetEconomy("Divorce");
		Costs_Marry		= plugin.config.GetEconomy("Marry");
		Costs_Gift		= plugin.config.GetEconomy("Gift");
		Costs_Home		= plugin.config.GetEconomy("HomeTp");
		Costs_SetHome	= plugin.config.GetEconomy("SetHome");
		Costs_TP = plugin.config.GetEconomy("Tp");
		// Load Messages
		Message_NotEnough			= ChatColor.RED   + plugin.lang.Get("Economy.NotEnough");
		Message_PartnerNotEnough 	= ChatColor.RED   + plugin.lang.Get("Economy.PartnerNotEnough");
		Message_DivorcePaid			= ChatColor.GREEN + plugin.lang.Get("Economy.DivorcePaid");
		Message_MarriagePaid		= ChatColor.GREEN + plugin.lang.Get("Economy.MarriagePaid");
		Message_DivNotEnoPriestI	= ChatColor.RED   + plugin.lang.Get("Economy.DivNotEnoPriestI");
		Message_NotEnoughPriestInfo	= ChatColor.RED   + plugin.lang.Get("Economy.NotEnoughPriestInfo");
		Message_HomeTPPaid			= ChatColor.GREEN + plugin.lang.Get("Economy.HomeTPPaid");
		Message_SetHomePaid			= ChatColor.GREEN + plugin.lang.Get("Economy.SetHomePaid");
		Message_GiftPaid			= ChatColor.GREEN + plugin.lang.Get("Economy.GiftPaid");
		Message_TPPaid				= ChatColor.GREEN + plugin.lang.Get("TPPaid");
	}

	public boolean HomeTeleport(Player player) { return true; }
	
	public boolean SetHome(Player player) { return true; }
	
	public boolean Gift(Player player) { return true; }
	
	public boolean Teleport(Player player) { return true; }
	
	public boolean Divorce(CommandSender priest, Player player, Player otherPlayer) { return true; }
	
	public boolean Marry(CommandSender priest, Player player, Player otherPlayer) { return true; }
	
	public static BaseEconomy GetEconomy(MarriageMaster pl)
	{
		if(!pl.config.UseEconomy())
		{
			return null;
		}
		Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
		if(pl != null)
		{
			String[] vaultV = vault.getDescription().getVersion().split(Pattern.quote( "." ));
			try
			{
				if(Integer.parseInt(vaultV[0]) > 1 || (Integer.parseInt(vaultV[0]) == 1 && Integer.parseInt(vaultV[1]) >= 4))
				{
					return new at.pcgamingfreaks.MarriageMaster.Bukkit.Economy.Economy(pl);
				}
				else
				{
					return new EconomyOld(pl);
				}
			}
			catch(Exception e){}
		}
		return null;
	}
}
