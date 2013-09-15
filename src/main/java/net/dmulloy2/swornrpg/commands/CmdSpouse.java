package net.dmulloy2.swornrpg.commands;

import net.dmulloy2.swornrpg.SwornRPG;
import net.dmulloy2.swornrpg.data.PlayerData;
import net.dmulloy2.swornrpg.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdSpouse extends SwornRPGCommand
{
	public CmdSpouse (SwornRPG plugin)
	{
		super(plugin);
		this.name = "spouse";
		this.aliases.add("spouseinfo");
		this.description = "Check information on a player's spouse";
		this.optionalArgs.add("player");
		this.mustBePlayer = false;
	}
	
	@Override
	public void perform()
	{
		if (plugin.marriage == false)
		{
			sendpMessage(plugin.getMessage("command_disabled"));
			return;
		}
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
				sendpMessage(plugin.getMessage("console_spouse"));
				return;
			}
		}
		PlayerData data = getPlayerData(target);
		if (data == null)
		{
			sendpMessage(plugin.getMessage("noplayer"));
			return;
		}
		String targetp = target.getName();
		String spouse = data.getSpouse();
		String name;
		String senderp = sender.getName();
		if (targetp == senderp)
			name = "You are";
		else
			name = targetp + " is";
		if (spouse != null)
		{
			sendpMessage(plugin.getMessage("spouse_info"), name, spouse);
		}
		else
		{
			sendpMessage(plugin.getMessage("no_spouse"), name);
		}
	}
}