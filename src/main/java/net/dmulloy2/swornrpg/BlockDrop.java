package net.dmulloy2.swornrpg;

import org.bukkit.inventory.ItemStack;

/**
 * @author dmulloy2
 */

public class BlockDrop
{
	final ItemStack item;
	final int chance;
	
	public BlockDrop(final ItemStack itemStack, final int chance) 
	{
		this.item = itemStack;
		this.chance = chance;
	}
	
	public ItemStack getItem()
	{
		return item;
	}
	
	public int getChance()
	{
		return chance;
	}
	
	public String toString()
	{
		return "[" + item.getType().toString().toLowerCase().replaceAll("_", " ") + ", " + chance + "]";
	}
}