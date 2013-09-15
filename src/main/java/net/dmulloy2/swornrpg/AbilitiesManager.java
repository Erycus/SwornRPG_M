package net.dmulloy2.swornrpg;

import java.util.HashMap;

import net.dmulloy2.swornrpg.data.PlayerData;
import net.dmulloy2.swornrpg.util.FormatUtil;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author dmulloy2
 */

public class AbilitiesManager
{
	public SwornRPG plugin;
	public void initialize(SwornRPG plugin)
	{
		this.plugin = plugin;
	}
	
	public HashMap<String, Long> frenzyWaiting = new HashMap<String, Long>();
	public HashMap<String, Long> spickWaiting = new HashMap<String, Long>();
	
	/**Frenzy Mode!**/
	public void activateFrenzy(final Player player, boolean command, Action... actions)
	{
		/**Enable Check**/
		if (!plugin.frenzyenabled)
		{
			if (command) sendpMessage(player, plugin.getMessage("command_disabled"));
			return;
		}
		
		/**GameMode check**/
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			if (command) sendpMessage(player, plugin.getMessage("creative_ability"));
			return;
		}
		
		/**Check for Frenzy In Progress**/
		final PlayerData data = plugin.getPlayerDataCache().getData(player);
		if (data.isFrenzy())
		{
			if (command) sendpMessage(player, plugin.getMessage("ability_in_progress"), "Frenzy Mode");
			return;
		}
		
		/**Action Check**/
		boolean activate = false;
		if (actions.length > 0)
		{
			for (Action action : actions)
			{
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK || action == Action.PHYSICAL)
					activate = true;
			}
		}
		else
		{
			activate = true;
		}
		
		/**The player did not left click**/
		if (activate == false)
		{
			if (!frenzyWaiting.containsKey(player.getName()))
			{
				String inHand = player.getItemInHand().getType().toString().toLowerCase().replaceAll("_", " ");
				sendpMessage(player, plugin.getMessage("ability_ready"), inHand);
				frenzyWaiting.put(player.getName(), System.currentTimeMillis());
				new FrenzyRemoveThread(player).runTaskLater(plugin, 60);
			}
			return;
		}
		
		/**The player is activating using an item**/
		if (command == false)
		{
			/**Check if the player is on the waiting list**/
			if (frenzyWaiting.containsKey(player.getName()))
			{
				/**Cooldown Check**/
				if (data.isFcooldown())
				{
					sendpMessage(player, plugin.getMessage("frenzy_cooldown"), (data.getFrenzycd()));
					frenzyWaiting.remove(player.getName());
					return;
				}
				frenzyWaiting.remove(player.getName());
			}
			else
			{
				return;
			}
		}
		else
		{
			if (data.isFcooldown())
			{
				sendpMessage(player, plugin.getMessage("frenzy_cooldown"), (data.getFrenzycd()));
				return;
			}
		}
		
		/**Activate Frenzy!**/
		sendpMessage(player, plugin.getMessage("frenzy_enter"));
		data.setFrenzy(true);
		int strength = 0;
		int level = data.getLevel();
		if (level == 0)
			level = 1;
		/**Duration = frenzy base duraton + (frenzy multiplier x level)**/
		final int duration = Integer.valueOf(Math.round(20*(plugin.frenzyd + (level*plugin.frenzym))));
		player.addPotionEffect(PotionEffectType.SPEED.createEffect(duration, strength));
		player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(duration, strength));
		player.addPotionEffect(PotionEffectType.REGENERATION.createEffect(duration, strength));
		player.addPotionEffect(PotionEffectType.JUMP.createEffect(duration, strength));
		player.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(duration, strength));
		player.addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(duration, strength));
		if (plugin.debug) plugin.outConsole(plugin.getMessage("log_frenzy_activate"), player.getName(), duration);
		class FrenzyThread extends BukkitRunnable
		{
			@Override
			public void run()
			{
				sendpMessage(player, plugin.getMessage("frenzy_wearoff"));
				int cooldown = (duration*plugin.frenzycd);
				data.setFrenzycd(cooldown);
				data.setFcooldown(true);
				data.setFrenzy(false);
				if (plugin.debug) plugin.outConsole(plugin.getMessage("log_frenzy_cooldown"), player.getName(), cooldown);
			}
		}
		new FrenzyThread().runTaskLater(plugin, duration);
	}
	
	/**Super Pickaxe!**/
	public void activateSpick(final Player player, boolean command, Action... actions)
	{
		/**Enable Check**/
		if (plugin.spenabled == false)
		{
			if (command) sendpMessage(player, plugin.getMessage("command_disabled"));
			return;
		}
		
		/**GameMode check**/
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			if (command) sendpMessage(player, plugin.getMessage("creative_ability"));
			return;
		}
		
		/**Check to make sure it is an iron or diamond pickaxe**/
		String inhand = player.getItemInHand().getType().toString().toLowerCase().replaceAll("_", " ");
		String[] array = inhand.split(" ");
			
		if (array.length < 2)
		{
			if (command) sendpMessage(player, plugin.getMessage("superpick_invalid_item"));
			return;
		}
			
		if (!array[0].equals("diamond") && !array[0].equals("iron"))
		{
			if (command) sendpMessage(player, plugin.getMessage("superpick_invalid_item"));
			return;
		}
			
		if (!array[1].equals("spade") && !array[1].equals("pickaxe"))
		{
			if (command) sendpMessage(player, plugin.getMessage("superpick_invalid_item"));
			return;
		}
		
		/**If the player is using SuperPick, return**/
		final PlayerData data = plugin.getPlayerDataCache().getData(player);
		if (data.isSpick())
		{
			if (command) sendpMessage(player, plugin.getMessage("ability_in_progress"), "Super Pickaxe");
			return;
		}
		
		/**Check the Action**/
		boolean activate = false;
		if (actions.length > 0)
		{
			for (Action action : actions)
			{
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK || action == Action.PHYSICAL)
					activate = true;
			}
		}
		else
		{
			activate = true;
		}
		
		/**The player did not left click**/
		if (activate == false)
		{
			if (!spickWaiting.containsKey(player.getName()))
			{
				sendpMessage(player, plugin.getMessage("ability_ready"), inhand);
				spickWaiting.put(player.getName(), System.currentTimeMillis());
				new SpickRemoveThread(player).runTaskLater(plugin, 60);
			}
			return;
		}
		
		/**The player is activating using an item**/
		if (command == false)
		{
			/**Check if the player is on the waiting list**/
			if (spickWaiting.containsKey(player.getName()))
			{
				/**Cooldown Check**/
				if (data.isScooldown())
				{
					sendpMessage(player, plugin.getMessage("superpick_cooldown"), (data.getSuperpickcd()));
					spickWaiting.remove(player.getName());
					return;
				}
				spickWaiting.remove(player.getName());
			}
			else
			{
				return;
			}
		}
		else
		{
			if (data.isScooldown())
			{
				sendpMessage(player, plugin.getMessage("superpick_cooldown"), (data.getSuperpickcd()));
				return;
			}
		}

		/**Activate Super Pickaxe!**/
		sendpMessage(player, plugin.getMessage("superpick_activated"));
		int level = data.getLevel();
		if (level == 0)
			level = 1;
		final int duration = Integer.valueOf(Math.round(20*(plugin.spbaseduration + (level*plugin.superpickm))));
		int strength = 1;
		data.setSpick(true);
		player.addPotionEffect(PotionEffectType.FAST_DIGGING.createEffect(duration, strength));
		if (plugin.debug) plugin.outConsole(plugin.getMessage("log_superpick_activate"), player.getName(), duration);
		class SuperPickThread extends BukkitRunnable
		{
			@Override
			public void run()
			{
				sendpMessage(player, plugin.getMessage("superpick_wearoff"));
				data.setSpick(false);
				data.setScooldown(true);
				int cooldown = (duration*plugin.superpickcd);
				data.setSuperpickcd(cooldown);
				if (plugin.debug) plugin.outConsole(plugin.getMessage("log_superpick_cooldown"), player.getName(), cooldown);
			}
		}
		new SuperPickThread().runTaskLater(plugin, duration);
	}
	
	/**Unlimited Ammo**/
	public void activateAmmo(final Player player)
	{
		/**PVPGunPlus Enable Check**/
		PluginManager pm = plugin.getServer().getPluginManager();
		if (!pm.isPluginEnabled("PVPGunPlus"))
		{
			sendpMessage(player, plugin.getMessage("plugin_not_found"), "PVPGunPlus");
			return;
		}
		
		/**Check to see if they are already using Unlimited Ammo**/
		final PlayerData data = plugin.getPlayerDataCache().getData(player);
		if (data.isUnlimtdammo())
		{
			sendpMessage(player, plugin.getMessage("ability_in_progress"), "Unlimited Ammo");
			return;
		}
		
		/**Cooldown Check**/
		if (data.isAmmocooling())
		{
			sendpMessage(player, plugin.getMessage("ammo_cooldown"), data.getAmmocd());
			return;
		}
		
		/**Activate Unlimited Ammo!**/
		int level = data.getLevel();
		if (level == 0)
			level = 1;
		final int duration = Integer.valueOf(Math.round(20*(plugin.ammobaseduration + (level*plugin.ammomultiplier))));
		data.setUnlimtdammo(true);
		sendpMessage(player, plugin.getMessage("ammo_now_unlimited"));
		if (plugin.debug) plugin.outConsole(plugin.getMessage("log_ammo_activate"), player.getName(), duration);
		class UnlimitedAmmoThread extends BukkitRunnable
		{
			@Override
			public void run()
			{
				data.setUnlimtdammo(false);
				sendpMessage(player, plugin.getMessage("ammo_nolonger_unlimited"));
				data.setAmmocooling(true);
				int cooldown = (duration*plugin.ammocooldown);
				data.setAmmocd(cooldown);
				if (plugin.debug) plugin.outConsole(plugin.getMessage("log_ammo_cooldown"), player.getName(), cooldown);
			}
		}
		new UnlimitedAmmoThread().runTaskLater(plugin, duration);
	}
	
	/**Send non prefixed message**/
	protected final void sendMessage(Player player, String msg, Object... args) 
	{
		player.sendMessage(FormatUtil.format(msg, args));
	}
	
	/**Send prefixed message**/
	protected final void sendpMessage(Player player, String msg, Object... args) 
	{
		player.sendMessage(plugin.prefix + FormatUtil.format(msg, args));
	}
	
	/**If the player has been on the frenzy waiting list for more than 3 seconds, remove them from the list**/
	public class FrenzyRemoveThread extends BukkitRunnable
	{
		public Player player;
		public FrenzyRemoveThread(Player player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (frenzyWaiting.containsKey(player.getName()))
			{
				long value = frenzyWaiting.get(player.getName()).longValue();
				long current = System.currentTimeMillis();
				if (current - value >= 60)
				{
					frenzyWaiting.remove(player.getName());
					if (player.isOnline())
					{
						String inHand = player.getItemInHand().getType().toString().toLowerCase().replaceAll("_", " ");
						player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("lower_item"), inHand));
					}
				}
			}
		}
	}
	
	/**If the player has been on the spick waiting list for more than 3 seconds, remove them from the list**/
	public class SpickRemoveThread extends BukkitRunnable
	{
		public Player player;
		public SpickRemoveThread(Player player)
		{
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (spickWaiting.containsKey(player.getName()))
			{
				long value = spickWaiting.get(player.getName()).longValue();
				long current = System.currentTimeMillis();
				if (current - value >= 60)
				{
					spickWaiting.remove(player.getName());
					if (player.isOnline())
					{
						String inHand = player.getItemInHand().getType().toString().toLowerCase().replaceAll("_", " ");
						player.sendMessage(plugin.prefix + FormatUtil.format(plugin.getMessage("lower_item"), inHand));
					}
				}
			}
		}
	}
}