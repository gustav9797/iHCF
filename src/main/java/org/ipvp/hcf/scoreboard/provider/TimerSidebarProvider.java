package org.ipvp.hcf.scoreboard.provider;

import com.google.common.collect.Ordering;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.ipvp.hcf.ConfigurationService;
import org.ipvp.hcf.DateTimeFormats;
import org.ipvp.hcf.DurationFormatter;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.eventgame.EventTimer;
import org.ipvp.hcf.eventgame.eotw.EotwHandler;
import org.ipvp.hcf.eventgame.faction.ConquestFaction;
import org.ipvp.hcf.eventgame.faction.EventFaction;
import org.ipvp.hcf.eventgame.faction.KothFaction;
import org.ipvp.hcf.eventgame.tracker.ConquestTracker;
import org.ipvp.hcf.faction.type.PlayerFaction;
import org.ipvp.hcf.pvpclass.PvpClass;
import org.ipvp.hcf.pvpclass.archer.ArcherClass;
import org.ipvp.hcf.pvpclass.archer.ArcherMark;
import org.ipvp.hcf.pvpclass.bard.BardClass;
import org.ipvp.hcf.scoreboard.SidebarEntry;
import org.ipvp.hcf.scoreboard.SidebarProvider;
import org.ipvp.hcf.sotw.SotwTimer;
import org.ipvp.hcf.timer.PlayerTimer;
import org.ipvp.hcf.timer.Timer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TimerSidebarProvider implements SidebarProvider {

    public static final ThreadLocal<DecimalFormat> CONQUEST_FORMATTER = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("00.0");
        }
    };

    private static final SidebarEntry EMPTY_ENTRY_FILLER = new SidebarEntry(" ", " ", " ");
    private static final Comparator<Map.Entry<UUID, ArcherMark>> ARCHER_MARK_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());

    private final HCF plugin;

    public TimerSidebarProvider(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getTitle() {
        return ConfigurationService.SCOREBOARD_TITLE;
    }

    @Override
    public List<SidebarEntry> getLines(Player player) {
        List<SidebarEntry> lines = new ArrayList<>();
        EotwHandler.EotwRunnable eotwRunnable = plugin.getEotwHandler().getRunnable();
        if (eotwRunnable != null) {
            long remaining = eotwRunnable.getMillisUntilStarting();
            if (remaining > 0L) {
                lines.add(new SidebarEntry(ChatColor.RED.toString() + ChatColor.BOLD, "EOTW" + ChatColor.RED + " starts", " in " + ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true)));
            } else if ((remaining = eotwRunnable.getMillisUntilCappable()) > 0L) {
                lines.add(new SidebarEntry(ChatColor.RED.toString() + ChatColor.BOLD, "EOTW" + ChatColor.RED + " cappable", " in " + ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true)));
            }
        }

        SotwTimer.SotwRunnable sotwRunnable = plugin.getSotwTimer().getSotwRunnable();
        if (sotwRunnable != null) {
            lines.add(new SidebarEntry(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD, "SOTW",
                    ChatColor.GRAY + ": " + ChatColor.WHITE + DurationFormatter.getRemaining(sotwRunnable.getRemaining(), true)));
        }

        EventTimer eventTimer = plugin.getTimerManager().getEventTimer();
        List<SidebarEntry> conquestLines = null;

        EventFaction eventFaction = eventTimer.getEventFaction();
        if (eventFaction instanceof KothFaction) {
            lines.add(new SidebarEntry(eventTimer.getScoreboardPrefix(), eventFaction.getScoreboardName() + ChatColor.GRAY,
                    ": " + ChatColor.WHITE + DurationFormatter.getRemaining(eventTimer.getRemaining(), true)));
        } else if (eventFaction instanceof ConquestFaction) {
            ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
            DecimalFormat format = CONQUEST_FORMATTER.get();

            conquestLines = new ArrayList<>();
            conquestLines.add(new SidebarEntry(ChatColor.BLUE.toString(), ChatColor.BOLD + conquestFaction.getName() + ChatColor.GRAY, ":"));

            conquestLines.add(new SidebarEntry("  " + ChatColor.RED.toString() + conquestFaction.getRed().getScoreboardRemaining(), ChatColor.RESET + " ",
                    ChatColor.YELLOW.toString() + conquestFaction.getYellow().getScoreboardRemaining()));

            conquestLines.add(new SidebarEntry("  " + ChatColor.GREEN.toString() + conquestFaction.getGreen().getScoreboardRemaining(), ChatColor.RESET + " " + ChatColor.RESET,
                    ChatColor.AQUA.toString() + conquestFaction.getBlue().getScoreboardRemaining()));

            // Show the top 3 factions next.
            ConquestTracker conquestTracker = (ConquestTracker) conquestFaction.getEventType().getEventTracker();
            int count = 0;
            for (Map.Entry<PlayerFaction, Integer> entry : conquestTracker.getFactionPointsMap().entrySet()) {
                String factionName = entry.getKey().getName();
                if (factionName.length() > 14)
                    factionName = factionName.substring(0, 14);
                conquestLines.add(new SidebarEntry(ChatColor.RED, ChatColor.BOLD + factionName, ChatColor.GRAY + ": " + ChatColor.WHITE + entry.getValue()));
                if (++count == 3)
                    break;
            }
        }

        // Show the current PVP Class statistics of the player.
        PvpClass pvpClass = plugin.getPvpClassManager().getEquippedClass(player);
        if (pvpClass != null) {
            lines.add(new SidebarEntry(ChatColor.YELLOW, "Current Class" + ChatColor.GRAY + ": ", ChatColor.GREEN + pvpClass.getName()));
            if (pvpClass instanceof BardClass) {
                BardClass bardClass = (BardClass) pvpClass;
                lines.add(
                        new SidebarEntry(ChatColor.GOLD + " \u00bb ", ChatColor.AQUA + "Energy", ChatColor.GRAY + ": " + ChatColor.WHITE + handleBardFormat(bardClass.getEnergyMillis(player), true)));

                long remaining = bardClass.getRemainingBuffDelay(player);
                if (remaining > 0) {
                    lines.add(new SidebarEntry(ChatColor.GOLD + " \u00bb ", ChatColor.AQUA + "Buff Delay", ChatColor.GRAY + ": " + ChatColor.WHITE + DurationFormatter.getRemaining(remaining, true)));
                }
            } else if (pvpClass instanceof ArcherClass) {
                ArcherClass archerClass = (ArcherClass) pvpClass;

                List<Map.Entry<UUID, ArcherMark>> entryList = Ordering.from(ARCHER_MARK_COMPARATOR).sortedCopy(archerClass.getSentMarks(player).entrySet());
                entryList = entryList.subList(0, Math.min(entryList.size(), 3));
                for (Map.Entry<UUID, ArcherMark> entry : entryList) {
                    ArcherMark archerMark = entry.getValue();
                    Player target = Bukkit.getPlayer(entry.getKey());
                    if (target != null) {
                        ChatColor levelColour;
                        switch (archerMark.currentLevel) {
                        case 1:
                            levelColour = ChatColor.GREEN;
                            break;
                        case 2:
                            levelColour = ChatColor.RED;
                            break;
                        case 3:
                            levelColour = ChatColor.DARK_RED;
                            break;

                        default:
                            levelColour = ChatColor.YELLOW;
                            break;
                        }

                        // Add the current mark level to scoreboard.
                        // Needs to be re-written. May be &6&lArcher Mark&7:
                        // >>(/u00bb) Interlagos [Mark 1]
                        // *NOTE THIS IS EXPERIMENTAL MAY CAUSE ERRORS THEREFORE NOT COMMITING TO MAIN!
                        String targetName = target.getName();
                        targetName = targetName.substring(0, Math.min(targetName.length(), 15));
                        lines.add(new SidebarEntry(ChatColor.GOLD + "" + ChatColor.BOLD, "Archer Mark" + ChatColor.GRAY + ": ", ""));
                        lines.add(
                                new SidebarEntry(ChatColor.GOLD + " \u00bb" + ChatColor.RED, ' ' + targetName, ChatColor.YELLOW.toString() + levelColour + " [Mark " + archerMark.currentLevel + ']'));
                    }
                }
            }
        }

        Collection<Timer> timers = plugin.getTimerManager().getTimers();
        for (Timer timer : timers) {
            if (timer instanceof PlayerTimer) {
                PlayerTimer playerTimer = (PlayerTimer) timer;
                long remaining = playerTimer.getRemaining(player);
                if (remaining <= 0)
                    continue;

                String timerName = playerTimer.getName();
                if (timerName.length() > 14)
                    timerName = timerName.substring(0, timerName.length());
                lines.add(new SidebarEntry(playerTimer.getScoreboardPrefix(), timerName + ChatColor.GRAY, ": " + ChatColor.WHITE + DurationFormatter.getRemaining(remaining, true)));
            }
        }

        if (conquestLines != null && !conquestLines.isEmpty()) {
            if (!lines.isEmpty()) {
                conquestLines.add(new SidebarEntry(" ", "  ", " "));
            }

            conquestLines.addAll(lines);
            lines = conquestLines;
        }

        return lines;
    }

    private static String handleBardFormat(long millis, boolean trailingZero) {
        return (trailingZero ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get().format(millis * 0.001);
    }
}
