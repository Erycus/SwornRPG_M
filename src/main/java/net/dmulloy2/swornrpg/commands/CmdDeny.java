package net.dmulloy2.swornrpg.commands;

import net.dmulloy2.swornrpg.SwornRPG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdDeny extends SwornRPGCommand
{
	public CmdDeny (SwornRPG plugin)
	{
		super(plugin);
		this.name = "deny";
		this.aliases.add("reject");
		this.description = "Deny a player's proposal";
		this.optionalArgs.add("player");
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		if (plugin.marriage == false)
		{
			sendpMessage(plugin.getMessage("command_disabled"));
			return;
		}
		if (plugin.proposal.containsKey(sender.getName()))
		{
			Player target = Bukkit.getServer().getPlayer((String)plugin.proposal.get(sender.getName()));
			if (target != null)
			{
				String targetp = target.getName();
				String senderp = sender.getName();
				plugin.proposal.remove(senderp);
				plugin.proposal.remove(targetp);
				sendpMessage(plugin.getMessage("deny_sender"), targetp);
				sendMessageTarget(plugin.getMessage("deny_rejcted"), target, senderp);
			}
			else
			{
				sendpMessage(plugin.getMessage("noplayer"));
			}
		}
		else
		{
			sendpMessage(plugin.getMessage("no_proposal"));
		}
	}
}