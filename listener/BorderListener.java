package org.ipvp.hcf.listener;

import org.bukkit.event.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;
import org.ipvp.hcf.ConfigurationService;
import org.bukkit.*;

public class BorderListener implements Listener
{
    private static final int BORDER_OFFSET_TELEPORTS = 50;
    
    public static boolean isWithinBorder(final Location location) {
        final int borderSize = ConfigurationService.BORDER_SIZES.get(location.getWorld().getEnvironment());
        return Math.abs(location.getBlockX()) <= borderSize && Math.abs(location.getBlockZ()) <= borderSize;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreaturePreSpawn(final CreatureSpawnEvent event) {
        if (!isWithinBorder(event.getLocation())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(final PlayerBucketFillEvent event) {
        if (!isWithinBorder(event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot fill buckets past the border.");
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (!isWithinBorder(event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot empty buckets past the border.");
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (!isWithinBorder(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks past the border.");
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!isWithinBorder(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks past the border.");
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        if (!isWithinBorder(to) && isWithinBorder(from)) {
            final Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "You cannot go past the border.");
            event.setTo(from);
            final Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                vehicle.eject();
                vehicle.teleport(from);
                vehicle.setPassenger((Entity)player);
            }
        }
    }
    
    }
