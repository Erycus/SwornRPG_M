package net.dmulloy2.swornrpg.commands;

import net.dmulloy2.swornrpg.SwornRPG;
import net.dmulloy2.swornrpg.permissions.PermissionType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author dmulloy2
 */

public class CmdItemName extends SwornRPGCommand
{
	public CmdItemName (SwornRPG plugin)
	{
		super(plugin);
		this.name = "iname";
		this.description = "Set the name of your inhand item";
		this.aliases.add("itemname");
		this.requiredArgs.add("name");
		this.permission = PermissionType.CMD_INAME.permission;
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
	    ItemStack hand = player.getItemInHand();
	    if ((hand == null)||hand.getType().equals(Material.AIR))
	    {
	    	sendpMessage(plugin.getMessage("hand_empty"));
	    }
	    else
	    {
	    	ItemMeta meta = hand.getItemMeta();
	    	StringBuilder name = new StringBuilder();
	    	for (int i = 0; i < args.length; i++) 
	    	{ 
	    		name = name.append(ChatColor.translateAlternateColorCodes('&', args[i]) + " ");
		    }
	    	name.deleteCharAt(name.lastIndexOf(" "));
	    	meta.setDisplayName(name.toString());
	    	hand.setItemMeta(meta);
	    	String inhand = hand.getType().toString().toLowerCase().replaceAll("_", " ");
	    	sendpMessage(plugin.getMessage("item_name"), inhand, name);
	    }
	}
}