package net.dmulloy2.swornrpg.listeners;

import java.util.List;

import net.dmulloy2.swornrpg.SwornRPG;
import net.dmulloy2.swornrpg.data.PlayerData;
import net.dmulloy2.swornrpg.events.PlayerLevelupEvent;
import net.dmulloy2.swornrpg.events.PlayerXpGainEvent;
import net.dmulloy2.swornrpg.util.FormatUtil;
import net.dmulloy2.swornrpg.util.InventoryWorkaround;
import net.dmulloy2.swornrpg.util.Util;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.CropState;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.bukkit.material.NetherWarts;
import org.bukkit.plugin.PluginManager;

/**
 * @author dmulloy2
 */

public class ExperienceListener implements Listener 
{
	private SwornRPG plugin;
	public ExperienceListener(SwornRPG plugin) 
	{
		this.plugin = plugin;
	}
		
	/**Rewards XP in PvP situations**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		/**Checks for player kills to be enabled in the config**/
		if (plugin.playerkills == false)
			return;
		
		Player killed = event.getEntity().getPlayer();
		Player killer = event.getEntity().getKiller();
		
		/**Checks to see if it was PvP**/
		if (killer instanceof Player)
		{
			/**Warzone Checks**/
			if (plugin.checkFactions(killer, false))
				return;
			
			if (plugin.checkFactions(killed, false))
				return;
			
			String killerp = killer.getName();
			String killedp = killed.getName();
			
			/**Suicide Check**/
			if (killedp == killerp)
				return;
			
			String message = "";
			PluginManager pm = plugin.getServer().getPluginManager();
			
			/**Killer Xp Gain**/
			int killxp = plugin.killergain;
			message = (plugin.prefix + FormatUtil.format(plugin.getMessage("pvp_kill_msg"), killxp, killedp));
			pm.callEvent(new PlayerXpGainEvent (killer, killxp, message));
			
			/**Killed Xp Loss**/
			int killedxp = -(plugin.killedloss);
			int msgxp = Math.abs(killedxp);
			message = (plugin.prefix + FormatUtil.format(plugin.getMessage("pvp_death_msg"), msgxp, killerp));
			pm.callEvent(new PlayerXpGainEvent (killed, killedxp, message));
			
			/**Debug Message**/
			if (plugin.debug) 
			{
				plugin.outConsole(plugin.getMessage("log_pvp_killed"), killedp, msgxp, killerp);
				plugin.outConsole(plugin.getMessage("log_pvp_killer"), killerp, killxp, killedp);
			} 
			
		}
	}
	
	/**Rewards XP in PvE situations**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event)
	{
		/**Checks for mob kills to be enabled in the config**/
		if (plugin.mobkills == false)
			return;
		
		Entity kill = event.getEntity().getKiller();
		Entity killed = event.getEntity();
		
		/**Checks to make sure it wasn't pvp**/
		if (killed instanceof Player)
			return;
		
		/**Checks to make sure the killer is a player**/
		if (kill instanceof Player)
		{
			Player killer = event.getEntity().getKiller();
			String mobname = event.getEntity().getType().toString().toLowerCase().replaceAll("_", " ");
			PluginManager pm = plugin.getServer().getPluginManager();
			
			/**Warzone and Safezone check**/
			if (plugin.checkFactions(killer, true))
				return;
			
			/**Camping Check**/
			if (plugin.checkCamper(killer))
				return;
			
			/**XP gain calculation**/
			int killxp;
			if (
					mobname.equals("wither")
					||mobname.equals("ender dragon")
				)
				killxp = plugin.mobkillsxp*3;
			else if (
						mobname.equals("creeper")
						||mobname.equals("enderman")
						||mobname.equals("iron golem")
						||mobname.equals("skeleton")
						||mobname.equals("blaze")
						||mobname.contains("zombie")
						||mobname.contains("spider")
						||mobname.equals("ghast")
						||mobname.equals("magma cube")
						||mobname.equals("witch")
						||mobname.equals("slime")
					)
				killxp = plugin.mobkillsxp*2;
			else
				killxp = plugin.mobkillsxp;
			
			/**Message**/
			String article = "";
			if (mobname.startsWith("a")||mobname.startsWith("e")||mobname.startsWith("i")||mobname.startsWith("o")||mobname.startsWith("u"))
				article = "an";
			else
				article = "a";
			String message = (plugin.prefix + FormatUtil.format(plugin.getMessage("mob_kill"), killxp, article,  mobname));
			
			/**Call Event**/
			pm.callEvent(new PlayerXpGainEvent (killer, killxp, message));
			if (plugin.debug) plugin.outConsole(plugin.getMessage("log_mob_kill"), killer.getName(), killxp, mobname);
		}
	}
	
	/**Rewards XP on Minecraft xp levelup**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLevelChange(PlayerLevelChangeEvent event)
	{
		/**Checks to make sure xp level xp gain is enabled in the config**/
		if (plugin.xplevel == false)
			return;
		
		Player player = event.getPlayer();
		if (player == null)
			return;

		/**Warzone Check**/
		if (plugin.checkFactions(player, true))
			return;
		
		/**Camping Check**/
		if (plugin.checkCamper(player))
			return;
		
		/**Define Stuff**/
		int oldlevel = event.getOldLevel();
		int newlevel = event.getNewLevel();
		if (newlevel - oldlevel != 1)
			return;
		
		int xpgained = plugin.xplevelgain;
		String message = (plugin.prefix + FormatUtil.format(plugin.getMessage("mc_xp_gain"), xpgained));
		
		/**Call Event**/
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.callEvent(new PlayerXpGainEvent (player, xpgained, message));
		if (plugin.debug) plugin.outConsole(plugin.getMessage("log_mcxpgain"), player.getName(), xpgained);
	}
	
	/**Herbalism : Breaking**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onHerbalismBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
			
		if (plugin.herbalism == false)
			return;
			
		Player player = event.getPlayer();
		if (player == null)
			return;
			
		Block block = event.getBlock();
		if (block == null)
			return;
			
		BlockState blockState = block.getState();
		if (blockState == null)
			return;
			
		GameMode gm = player.getGameMode();
		if (gm != GameMode.SURVIVAL)
			return;
			
		PlayerData data = plugin.getPlayerDataCache().getData(player);
		int concurrentHerbalism = data.getHerbalism();
		if (herbalismNeeded(blockState))
		{
			if (concurrentHerbalism >= 10)
			{
				int xp = plugin.herbalismgain * 10;
				String message = FormatUtil.format(plugin.prefix + plugin.getMessage("herbalism_gain"), xp);
				PlayerXpGainEvent xpgain = new PlayerXpGainEvent(player, xp, message);
				plugin.getServer().getPluginManager().callEvent(xpgain);
				data.setHerbalism(0);
			}
			else
			{
				data.setHerbalism(data.getHerbalism() + 1);
			}
		}
	}
	
	/**Herbalism : Instant Growth**/
	@EventHandler(priority = EventPriority.NORMAL)
	public void onHerbalismPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;
			
		if (plugin.herbalism == false)
			return;
			
		Player player = event.getPlayer();
		if (player == null)
			return;
		
		if (plugin.isDisabledWorld(player))
			return;
			
		Block block = event.getBlock();
		if (block == null)
			return;
			
		BlockState blockState = block.getState();
		if (blockState == null)
			return;
			
		GameMode gm = player.getGameMode();
		if (gm != GameMode.SURVIVAL)
			return;
	
		/**Insta-Growth**/
		PlayerData data = plugin.getPlayerDataCache().getData(player);
		int level = data.getLevel();
		if (level == 0) level = 1;
		if (level > 150) level = 150;
		int rand = Util.random(200 - level);
		if (rand == 0)
		{
			boolean message = false;
			Material mat = blockState.getType();
			if (mat == Material.NETHER_WARTS)
			{
				((NetherWarts) blockState.getData()).setState(NetherWartsState.RIPE);
				block.setData(blockState.getData().getData());
				message = true;
			}
			else if (
					mat == Material.CARROT
					||mat == Material.CROPS
					||mat == Material.POTATO
					)
			{
				blockState.setRawData(CropState.RIPE.getData());
				block.setData(blockState.getData().getData());
				message = true;
			}
			else if (mat == Material.SAPLING)
			{
				block.setType(Material.AIR);
				block.getWorld().generateTree(block.getLocation(), TreeType.TREE);
				message = true;
			}
			else if (mat == Material.RED_MUSHROOM)
			{
				block.setType(Material.AIR);
				block.getWorld().generateTree(block.getLocation(), TreeType.RED_MUSHROOM);
				message = true;
			}
			else if (mat == Material.BROWN_MUSHROOM)
			{
				block.setType(Material.AIR);
				block.getWorld().generateTree(block.getLocation(), TreeType.BROWN_MUSHROOM);
				message = true;
			}
			if (message == true)
			{
				player.sendMessage(FormatUtil.format(plugin.getMessage("insta_growth")));
			}
		}
	}
	
	/**Herbalism Check**/
	public boolean herbalismNeeded(BlockState blockState)
	{
        switch (blockState.getType())
        {
        	case CACTUS:
        	case MELON_BLOCK:
        	case PUMPKIN:
        		return true;
        		
            case CARROT:
            case CROPS:
            case POTATO:
                return blockState.getRawData() == CropState.RIPE.getData();

        	case NETHER_WARTS:
        		return ((NetherWarts) blockState.getData()).getState() == NetherWartsState.RIPE;

        	case COCOA:
        		return ((CocoaPlant) blockState.getData()).getSize() == CocoaPlantSize.LARGE;

        	default:
        		return false;
        }
	}
	
	/**Taming XP Gain**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityTame(EntityTameEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (plugin.taming == false)
			return;
		
		if (event.getOwner() instanceof Player)
		{
			Player player = (Player)event.getOwner();
			if (player != null)
			{
				/**XP Gain**/
				String mobname = event.getEntity().getType().toString().toLowerCase().replaceAll("_", " ");
				String article;
				if (mobname.startsWith("o"))
					article = "an";
				else
					article = "a";
				String message = FormatUtil.format(plugin.prefix + plugin.getMessage("taming_gain"), plugin.taminggain, article, mobname);
				PlayerXpGainEvent xpgainevent = new PlayerXpGainEvent(player, plugin.taminggain, message);
				plugin.getServer().getPluginManager().callEvent(xpgainevent);
				
				/**Wolf/Ocelot's Pal**/
				PlayerData data = plugin.getPlayerDataCache().getData(player);
				int level = data.getLevel();
				if (level <= 0) level = 1;
				if (level >= 50) level = 50;
				int rand2 = Util.random(150/level);
				if (rand2 == 0)
				{ 
					if (event.getEntity() instanceof Wolf)
					{
						Wolf wolf = (Wolf) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
						wolf.setOwner(player);
					}
					else if (event.getEntity() instanceof Ocelot)
					{
						Ocelot ocelot = (Ocelot) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.OCELOT);
						ocelot.setOwner(player);
					}
				}
				
				/**Taming Bomb!**/
				int rand1 = Util.random(150/level);
				if (rand1 == 0)
				{
					boolean msg = false;
					List<Entity> entities = player.getNearbyEntities(10,10,10);
					if (entities.size() > 0)
					{
						for (Entity entity : entities)
						{
							if (entity != null && entity instanceof Tameable)
							{
								((Tameable) entity).setOwner(player);
								msg = true;
							}
						}
					}
					if (msg == true)
					{
						player.sendMessage(FormatUtil.format(plugin.prefix + plugin.getMessage("tame_bomb")));
					}
				}
			}
		}
	}
	
	/**Enchanting XP**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerEnchant(EnchantItemEvent event)
	{
		Player player = event.getEnchanter();
		if (player == null)
			return;
		
		int cost = event.getExpLevelCost();
		if (cost < 15)
			return;
		
		PlayerData data = plugin.getPlayerDataCache().getData(player);
		int level = data.getLevel();
		if (level == 0) level = 1;
		if (level > 30) level = 30;
		
		if (plugin.enchanting == true)
		{
			int xp = (cost/2) + plugin.enchantbase;
			String message = FormatUtil.format(plugin.prefix + plugin.getMessage("enchant_gain"), xp);
		
			PlayerXpGainEvent xpgain = new PlayerXpGainEvent(player, xp, message);
			plugin.getServer().getPluginManager().callEvent(xpgain);
		}
	}
	
	/**Rewards items and money on player levelup**/
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLevelup(PlayerLevelupEvent event)
	{
		/**Cancellation check**/
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		if (player == null)
			return;
		
		/**Disabled World Check**/
		if (plugin.isDisabledWorld(player))
		{
			event.setCancelled(true);
			return;
		}
		
		PlayerData data = plugin.getPlayerDataCache().getData(player.getName());
		
		/**Prior Skill Data**/
		int oldfrenzy = (plugin.frenzyd + (data.getLevel()*plugin.frenzym));
		int oldspick = (plugin.spbaseduration + (data.getLevel()*plugin.superpickm));
		int oldammo = (plugin.ammobaseduration + (data.getLevel()*plugin.ammomultiplier));
		
		/**Prepare data for the next level**/
		if (data.getLevel() < 250) // set the level cap at 250, seems fair enough
			data.setLevel(data.getLevel() + 1);
		
		data.setXpneeded(data.getXpneeded() + (data.getXpneeded()/4));
		data.setPlayerxp(0);
		
		/**New Skill Data**/
		int newfrenzy = (plugin.frenzyd + (data.getLevel()*plugin.frenzym));
		int newspick = (plugin.spbaseduration + (data.getLevel()*plugin.superpickm));
		int newammo = (plugin.ammobaseduration + (data.getLevel()*plugin.ammomultiplier));
		
		/**Send messages**/
		int level = data.getLevel();
		if (level == 250)
		{
			player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("level_cap")));
		}
		else
		{
			player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup"), level));
		}
		if (plugin.debug) plugin.outConsole(plugin.getMessage("log_levelup"), player.getName(), level);
		
		/**Award money if enabled**/
		if (plugin.money == true)
		{
			/**Vault Check**/
			PluginManager pm = plugin.getServer().getPluginManager();
			if (pm.isPluginEnabled("Vault"))
			{
				Economy economy = plugin.getEconomy();
				if (economy != null)
				{
					int money = level*plugin.basemoney;
					economy.depositPlayer(player.getName(), money);
					player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup_money"), economy.format(money)));
				}
			}
		}
		
		/**Award items if enabled**/
		if (plugin.items == true)
		{
			int rewardamt = level*plugin.itemperlevel;
			ItemStack item = new ItemStack(plugin.itemreward, rewardamt);
			String friendlyitem = item.getType().toString().toLowerCase().replaceAll("_", " ");
			InventoryWorkaround.addItems(player.getInventory(), item);
			player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup_items"), rewardamt, friendlyitem));
		}
		
		/**Tell Players if Skill(s) went up**/
		double frenzy = newfrenzy - oldfrenzy;
		double spick = newspick - oldspick;
		double ammo = newammo - oldammo;
		
		player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup_skills")));
		if (frenzy > 0)
			player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup_frenzy"), String.valueOf(frenzy)));
		if (spick > 0)
			player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup_spick"), String.valueOf(spick)));
		if (ammo > 0)
			player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("levelup_ammo"), String.valueOf(ammo)));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerXpGain(PlayerXpGainEvent event)
	{	
		/**Cancellation check**/
		if (event.isCancelled())
			return;
		
		/**Cancel event if in a disabled world**/
		Player player = event.getPlayer();
		if (player == null)
			return;
		
		/**Disabled World Check**/
		if (plugin.isDisabledWorld(player))
		{
			event.setCancelled(true);
			return;
		}
		
		/**Send The Message**/
		String message = event.getMessage();
		if (message != "")
			player.sendMessage(message);

		/**Add the xp gained to their overall xp**/
		PlayerData data = plugin.getPlayerDataCache().getData(player.getName());
		int xpgained = event.getXpGained();
		data.setPlayerxp(data.getPlayerxp() + xpgained);
		data.setTotalxp(data.getTotalxp() + xpgained);
		
		/**Levelup check**/
		int xp = data.getPlayerxp();
		int xpneeded = data.getXpneeded();
		int newlevel = (xp/xpneeded);
		int oldlevel = data.getLevel();
		
		if ((xp - xpneeded) >= 0)
		{
			/**If so, call levelup event**/
			PluginManager pm = plugin.getServer().getPluginManager();
			pm.callEvent(new PlayerLevelupEvent (player, newlevel, oldlevel));
		}
	}
}