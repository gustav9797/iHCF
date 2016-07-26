package org.ipvp.hcf.eventgame;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.hcf.DateTimeFormats;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.eventgame.crate.EventKey;
import org.ipvp.hcf.eventgame.faction.ConquestFaction;
import org.ipvp.hcf.eventgame.faction.EventFaction;
import org.ipvp.hcf.eventgame.faction.KothFaction;
import org.ipvp.hcf.faction.event.CaptureZoneEnterEvent;
import org.ipvp.hcf.faction.event.CaptureZoneLeaveEvent;
import org.ipvp.hcf.faction.type.Faction;
import org.ipvp.hcf.faction.type.PlayerFaction;
import org.ipvp.hcf.timer.GlobalTimer;
import org.ipvp.hcfold.EventSignListener;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Timer that handles the cooldown for KingOfTheHill events.
 */
public class EventTimer extends GlobalTimer implements Listener {

    private static final long RESCHEDULE_FREEZE_MILLIS = TimeUnit.SECONDS.toMillis(15L);
    private static final String RESCHEDULE_FREEZE_WORDS = DurationFormatUtils.formatDurationWords(RESCHEDULE_FREEZE_MILLIS, true, true);

    private long startStamp; // the milliseconds at when the current event started.
    private long lastContestedEventMillis; // the milliseconds at when the last event was contested.
    private EventFaction eventFaction;

    private final HCF plugin;

    public EventFaction getEventFaction() {
        return this.eventFaction;
    }

    public EventTimer(HCF plugin) {
        super("Event", 0L);
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (eventFaction != null) {
                    eventFaction.getEventType().getEventTracker().tick(EventTimer.this, eventFaction);
                    return;
                }

                // There isn't an active event, find one!
                LocalDateTime now = LocalDateTime.now(DateTimeFormats.SERVER_ZONE_ID);
                int day = now.getDayOfYear();
                int hour = now.getHour();
                int minute = now.getMinute();
                for (Map.Entry<LocalDateTime, String> entry : plugin.getEventScheduler().getScheduleMap().entrySet()) {
                    // Compare now with the scheduled time..
                    LocalDateTime scheduledTime = entry.getKey();
                    if (day != scheduledTime.getDayOfYear() || hour != scheduledTime.getHour() || minute != scheduledTime.getMinute()) {
                        continue;
                    }

                    // Make sure the faction found in schedule exists.
                    Faction faction = plugin.getFactionManager().getFaction(entry.getValue());
                    if (faction instanceof EventFaction && tryContesting((EventFaction) faction, Bukkit.getConsoleSender())) {
                        break;
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.AQUA.toString();
    }

    @Override
    public String getName() {
        return this.eventFaction == null ? "Event" : this.eventFaction.getName();
    }

    @Override
    public boolean clearCooldown() {
        boolean result = super.clearCooldown();
        if (this.eventFaction != null) {
            for (CaptureZone captureZone : this.eventFaction.getCaptureZones()) {
                captureZone.setCappingPlayer(null);
            }

            // Make sure to set the land back as Deathban.
            this.eventFaction.setDeathban(true);
            this.eventFaction.getEventType().getEventTracker().stopTiming();
            this.eventFaction = null;
            this.startStamp = -1L;
            result = true;
        }

        return result;
    }

    @Override
    public long getRemaining() {
        if (this.eventFaction == null) {
            return 0L;
        } else if (this.eventFaction instanceof KothFaction) {
            return ((KothFaction) this.eventFaction).getCaptureZone().getRemainingCaptureMillis();
        } else {
            return super.getRemaining();
        }
    }

    /**
     * Handles the winner for this event.
     *
     * @param winner
     *            the {@link Player} that won
     */
    public void handleWinner(Player winner) {
        if (this.eventFaction == null)
            return;

        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(winner);
        Bukkit.broadcastMessage(ChatColor.GOLD + "[" + this.eventFaction.getEventType().getDisplayName() + "] " + ChatColor.DARK_AQUA + winner.getName() + ChatColor.AQUA + '['
                + (playerFaction == null ? Faction.FACTIONLESS_PREFIX : playerFaction.getName()) + ']' + ChatColor.BLUE + " has captured " + ChatColor.DARK_AQUA + this.eventFaction.getName()
                + ChatColor.BLUE + " after " + ChatColor.DARK_AQUA + DurationFormatUtils.formatDurationWords(getUptime(), true, true) + " of up-time" + ChatColor.BLUE + '.');

        EventType eventType = this.eventFaction.getEventType();
        World world = winner.getWorld();
        Location location = winner.getLocation();
        EventKey eventKey = plugin.getKeyManager().getEventKey();
        Collection<Inventory> inventories = eventKey.getInventories(eventType);
        ItemStack keyStack = eventKey.getItemStack(new EventKey.EventKeyData(eventType, inventories.isEmpty() ? 1 : plugin.getRandom().nextInt(inventories.size()) + 1));
        Map<Integer, ItemStack> excess = winner.getInventory().addItem(keyStack, EventSignListener.getEventSign(eventFaction.getName(), winner.getName()));
        for (ItemStack entry : excess.values()) {
            world.dropItemNaturally(location, entry);
        }

        this.clearCooldown(); // must always be cooled last as this nulls some variables.
    }

    /**
     * Tries contesting an {@link EventFaction}.
     *
     * @param eventFaction
     *            the {@link EventFaction} to be contested
     * @param sender
     *            the contesting {@link CommandSender}
     * @return true if the {@link EventFaction} was successfully contested
     */
    public boolean tryContesting(EventFaction eventFaction, CommandSender sender) {
        if (this.eventFaction != null) {
            sender.sendMessage(ChatColor.RED + "There is already an active event, use /event cancel to end it.");
            return false;
        }

        if (eventFaction instanceof KothFaction) {
            KothFaction kothFaction = (KothFaction) eventFaction;
            if (kothFaction.getCaptureZone() == null) {
                sender.sendMessage(ChatColor.RED + "Cannot schedule " + eventFaction.getName() + " as its' capture zone is not set.");
                return false;
            }
        } else if (eventFaction instanceof ConquestFaction) {
            ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
            Collection<ConquestFaction.ConquestZone> zones = conquestFaction.getConquestZones();
            for (ConquestFaction.ConquestZone zone : ConquestFaction.ConquestZone.values()) {
                if (!zones.contains(zone)) {
                    sender.sendMessage(ChatColor.RED + "Cannot schedule " + eventFaction.getName() + " as capture zone '" + zone.getDisplayName() + ChatColor.RED + "' is not set.");
                    return false;
                }
            }
        }

        // Don't allow events to reschedule their-self before they are allowed to.
        long millis = System.currentTimeMillis();
        if (this.lastContestedEventMillis + EventTimer.RESCHEDULE_FREEZE_MILLIS - millis > 0L) {
            sender.sendMessage(ChatColor.RED + "Cannot reschedule events within " + EventTimer.RESCHEDULE_FREEZE_WORDS + '.');
            return false;
        }

        this.lastContestedEventMillis = millis;
        this.startStamp = millis;
        this.eventFaction = eventFaction;

        eventFaction.getEventType().getEventTracker().onContest(eventFaction, this);
        if (eventFaction instanceof ConquestFaction) {
            setRemaining(1000L, true); // TODO: Add a unpredicated timer impl instead of this xD.
            setPaused(true);
        }

        Collection<CaptureZone> captureZones = eventFaction.getCaptureZones();
        for (CaptureZone captureZone : captureZones) {
            if (captureZone.isActive()) {
                Player player = Iterables.getFirst(captureZone.getCuboid().getPlayers(), null);
                if (player != null && eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone)) {
                    captureZone.setCappingPlayer(player);
                }
            }
        }

        eventFaction.setDeathban(false); // the event should be lowered deathban whilst active.
        return true;
    }

    /**
     * Gets the total uptime in milliseconds since the event started.
     *
     * @return the time in milliseconds since event started
     */
    public long getUptime() {
        return System.currentTimeMillis() - startStamp;
    }

    /**
     * Gets the time in milliseconds since the event started.
     *
     * @return the time in milliseconds since event started
     */
    public long getStartStamp() {
        return startStamp;
    }

    private void handleDisconnect(Player player) {
        Preconditions.checkNotNull(player);

        if (this.eventFaction == null)
            return;
        Collection<CaptureZone> captureZones = this.eventFaction.getCaptureZones();
        for (CaptureZone captureZone : captureZones) {
            if (Objects.equal(captureZone.getCappingPlayer(), player)) {
                captureZone.setCappingPlayer(null);
                this.eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, this.eventFaction);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.handleDisconnect(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent event) {
        this.handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        this.handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(CaptureZoneEnterEvent event) {
        if (this.eventFaction == null)
            return;

        CaptureZone captureZone = event.getCaptureZone();
        if (!this.eventFaction.getCaptureZones().contains(captureZone))
            return;

        Player player = event.getPlayer();
        if (captureZone.getCappingPlayer() == null && this.eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone)) {
            captureZone.setCappingPlayer(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(CaptureZoneLeaveEvent event) {
        if (Objects.equal(event.getFaction(), this.eventFaction)) {
            Player player = event.getPlayer();
            CaptureZone captureZone = event.getCaptureZone();
            if (Objects.equal(player, captureZone.getCappingPlayer())) {
                captureZone.setCappingPlayer(null);
                this.eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, this.eventFaction);

                // Try and find a new capper.
                for (Player target : captureZone.getCuboid().getPlayers()) {
                    if (target != null && !target.equals(player) && eventFaction.getEventType().getEventTracker().onControlTake(target, captureZone)) {
                        captureZone.setCappingPlayer(target);
                        break;
                    }
                }
            }
        }
    }
}