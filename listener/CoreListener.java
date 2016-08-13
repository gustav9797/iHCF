package org.ipvp.hcf.listener;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.struct.Raidable;
import org.ipvp.hcf.faction.type.ClaimableFaction;
import org.ipvp.hcf.faction.type.Faction;
import org.ipvp.hcf.faction.type.PlayerFaction;
import org.ipvp.hcf.faction.type.WarzoneFaction;

import com.doctordark.util.BukkitUtils;

public class CoreListener implements Listener {

    private final HCF plugin;

    public CoreListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() == World.Environment.NETHER && event.getBlock().getState() instanceof CreatureSpawner
                && !player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You may not break spawners in the nether.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() == World.Environment.NETHER && event.getBlock().getState() instanceof CreatureSpawner
                && !player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You may not place spawners in the nether.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }
    
    @EventHandler
    void onTarg(final EntityTargetEvent event) {
        if (event.getEntityType() == EntityType.CREEPER) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent ev) {
        final Player p = ev.getPlayer();
        if (ev.getMessage().equalsIgnoreCase("/ver") || ev.getMessage().equalsIgnoreCase("/version")) {
            p.sendMessage(ChatColor.WHITE + "This server is running WarfineSpigot version \n git-Spigot-1.7.10-R0.2-204-gd7db08e (MC: 1.7.10) (Implementing \n API version 1.7.10-R0.1-SNAPSHOT");
            ev.setCancelled(true);
         }
        }
    
    @EventHandler
    public void onPlayerCommandPreprocess1(final PlayerCommandPreprocessEvent ev) {
        final Player p = ev.getPlayer();
        if (ev.getMessage().equalsIgnoreCase("/hcfcore") || ev.getMessage().equalsIgnoreCase("/hcfstuff") || ev.getMessage().equalsIgnoreCase("/about") || ev.getMessage().equalsIgnoreCase("/plugins")|| ev.getMessage().equalsIgnoreCase("/?")) {
            p.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            p.sendMessage(ChatColor.RED + "Warfine HCF Core v1.0");
            p.sendMessage(ChatColor.RED + "For more help please join ts.warfine.com");
            p.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
         }
        }
    
    @EventHandler
    public void onPlayerCommandPreprocess2(final PlayerCommandPreprocessEvent ev) {
        final Player p = ev.getPlayer();
        if (ev.getMessage().equalsIgnoreCase("/help") || ev.getMessage().equalsIgnoreCase("/ehelp") || ev.getMessage().equalsIgnoreCase("/essentials:help")) {
            p.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            p.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "Warfine HCF Help");
            p.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD.toString()+ ChatColor.BOLD + "Map Information:");
            p.sendMessage(ChatColor.YELLOW + "Current Map§7: 3");
            p.sendMessage(ChatColor.YELLOW + "World Border§7: 2000 (World and Nether)");
            p.sendMessage(ChatColor.YELLOW + "Enchantment Limits§7: /mapkit");
            p.sendMessage("§eDeathban§7: &bMedic§7: 15 minutes, §6Gold§7: 30 minutes, §8Platinum§7: 1hr and 30mins, &7Silver: 2hr and 30min");
            p.sendMessage("§eRegular Deathban§7: 3 hours");
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD.toString()+ ChatColor.BOLD + "Useful Commands:");
            p.sendMessage("§e/report <player> <reason> §7- Report Cheaters by using this command!");
            p.sendMessage("§e/lives §7- View information on lives.");
            p.sendMessage("§e/stats <player> §7- View Stats on a player.");
            p.sendMessage("§e/ores <player> §7- View a players mining information.");
            p.sendMessage(ChatColor.YELLOW + "/f help §7- Faction Help");
            p.sendMessage(ChatColor.YELLOW + "/coords §7- Coords to Events/Locations");
            p.sendMessage(ChatColor.YELLOW + "/koth help §7- KoTH Help");
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD.toString()+ ChatColor.BOLD + "Other Information:");
            p.sendMessage("§eTeamspeak§7: §fts.warfine.com");
            p.sendMessage("§eStore§7: §fstore.warfine.com");
            p.sendMessage("§eForums§7: §fwarfine.com");
            p.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
         }
        }
    
    
    @EventHandler
    public void onLoginJoinFull(final PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (e.getPlayer().hasPermission("serverfull.bypass")) {
                e.allow();
            }
            else {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "&cServer is full! Buy a reserved slot at warfine.com");
            }
        }
    }
    

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonExtend(final BlockPistonExtendEvent event) {
        final Block block = event.getBlock();
        final Block targetBlock = block.getRelative(event.getDirection(), event.getLength() + 1);
        if (targetBlock.isEmpty() || targetBlock.isLiquid()) {
            final Faction targetFaction = this.plugin.getFactionManager().getFactionAt(targetBlock.getLocation());
            if (targetFaction instanceof Raidable && !((Raidable)targetFaction).isRaidable() && !targetFaction.equals(this.plugin.getFactionManager().getFactionAt(block))) {
                event.setCancelled(true);
            }
        }
    }
    
  //  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    //public void onPlayerTeleport(final PlayerTeleportEvent event) {
      //  if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
        //    final Faction toFactionAt = this.plugin.getFactionManager().getFactionAt(event.getTo());
          //  if (toFactionAt.isSafezone() && !this.plugin.getFactionManager().getFactionAt(event.getFrom()).isSafezone()) {
            //    final Player player = event.getPlayer();
              //  player.sendMessage(ChatColor.RED + "You may not Enderpearl into safe-zones, used Enderpearl has been refunded.");
                //this.plugin.getTimerManager().getEnderPearlTimer().refund(player);
                //event.setCancelled(true);
           // }
        //}
   // }  
    
 /*   @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(final PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            final Location from = event.getFrom();
            final Location to = event.getTo();
            final Player player = event.getPlayer();
            final Faction fromFac = this.plugin.getFactionManager().getFactionAt(from);
            if (fromFac.isSafezone()) {
                event.setTo(to.getWorld().getSpawnLocation().add(0.5, 0.0, 0.5));
                event.useTravelAgent(false);
                player.sendMessage(ChatColor.YELLOW + "Teleported to over-world spawn!");
                return;
            }
            if (event.useTravelAgent() && to.getWorld().getEnvironment() == World.Environment.NORMAL) {
                final TravelAgent travelAgent = event.getPortalTravelAgent();
                if (!travelAgent.getCanCreatePortal()) {
                    return;
                }
                final Location foundPortal = travelAgent.findPortal(to);
                if (foundPortal != null) {
                    return;
                }
                final Faction factionAt = this.plugin.getFactionManager().getFactionAt(to);
                if (factionAt instanceof ClaimableFaction) {
                    final Faction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                    if (playerFaction != null && playerFaction.equals(factionAt)) {
                        return;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Portal would have created portal in territory of " + factionAt.getDisplayName(playerFaction) + ChatColor.YELLOW + '.');
                    event.setCancelled(true);
                }
            }
        }
    }
   */ 
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBurn(final BlockBurnEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof WarzoneFaction || (factionAt instanceof Raidable && !((Raidable)factionAt).isRaidable())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFade(final BlockFadeEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLeavesDelay(final LeavesDecayEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockForm(final BlockFormEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && !attemptBuild(entity, event.getBlock().getLocation(), null)) {
            event.setCancelled(true);
        }
    }
    
    private boolean attemptBuild(Entity entity, Location location, Object object) {
		// TODO Auto-generated method stub
		return false;
	}

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent event) {
        final Entity remover = event.getRemover();
        if (remover instanceof Player && !attemptBuild(remover, event.getEntity().getLocation(), ChatColor.YELLOW + "You may not build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingPlace(final HangingPlaceEvent event) {
        if (!attemptBuild((Entity)event.getPlayer(), event.getEntity().getLocation(), ChatColor.YELLOW + "You may not build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingDamageByEntity(final EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof Hanging) {
            final Player attacker = BukkitUtils.getFinalAttacker((EntityDamageEvent)event, false);
            if (!attemptBuild((Entity)attacker, entity.getLocation(), ChatColor.YELLOW + "You may not build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();
        plugin.getVisualiseHandler().clearVisualBlocks(player, null, null, false);
        plugin.getUserManager().getUser(player.getUniqueId()).setShowClaimMap(false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        plugin.getVisualiseHandler().clearVisualBlocks(player, null, null, false);
        plugin.getUserManager().getUser(player.getUniqueId()).setShowClaimMap(false);
        
        
    }
}
