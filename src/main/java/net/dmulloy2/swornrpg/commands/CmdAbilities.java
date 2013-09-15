package net.dmulloy2.swornrpg.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.dmulloy2.swornrpg.SwornRPG;
import net.dmulloy2.swornrpg.data.PlayerData;
import net.dmulloy2.swornrpg.util.Util;

/**
 * @author dmulloy2
 */

public class CmdAbilities extends SwornRPGCommand
{
	public CmdAbilities (SwornRPG plugin)
	{
		super(plugin);
		this.name = "abilities";
		this.description = "Check SwornRPG ability levels";
		this.aliases.add("skills");
		this.optionalArgs.add("player");
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		OfflinePlayer target = null;
		if (args.length == 1)
		{
			target = Util.matchPlayer(args[0]);
			if (target == null)
			{
				target = Util.matchOfflinePlayer(args[0]);
				if (target == null)
				{
					sendpMessage(plugin.getMessage("noplayer"));
					return;
				}
			}
		}
		else
		{
			if (sender instanceof Player)
			{
				target = (Player)sender;
			}
			else
			{
				sendpMessage(plugin.getMessage("console_level"));
				return;
			}
		}
		PlayerData data = getPlayerData(target);
		if (data == null)
		{
			sendpMessage(plugin.getMessage("noplayer"));
			return;
		}

		int level = data.getLevel();
		
		if (sender == target)
			sendMessage(plugin.getMessage("ability_header_self"));
		else
			sendMessage(plugin.getMessage("ability_header_others"), target.getName());
		
		if (plugin.frenzyenabled)
		{
			if (data.isFcooldown())
				sendMessage(plugin.getMessage("ability_frenzy_cooldown"), (plugin.frenzyd + (level*plugin.frenzym)), data.getFrenzycd());
			else
				sendMessage(plugin.getMessage("ability_frenzy"), (plugin.frenzyd + (level*plugin.frenzym)));
		}
		
		if (plugin.spenabled)
		{
			if (data.isScooldown())
				sendMessage(plugin.getMessage("ability_spick_cooldown"), (plugin.spbaseduration + (level*plugin.superpickm)), data.getSuperpickcd());
			else
				sendMessage(plugin.getMessage("ability_spick"), (plugin.spbaseduration + (level*plugin.superpickm)));
		}
		
		if (plugin.ammoenabled && plugin.getServer().getPluginManager().isPluginEnabled("PVPGunPlus"))
		{
			if (data.isAmmocooling())
				sendMessage(plugin.getMessage("ability_ammo_cooldown"), (plugin.ammobaseduration + (level*plugin.ammomultiplier)), data.getAmmocd());
			else
				sendMessage(plugin.getMessage("ability_ammo"), (plugin.ammobaseduration + (level*plugin.ammomultiplier)));
		}
		
		sendMessage(plugin.getMessage("ability_level"));
	}
}