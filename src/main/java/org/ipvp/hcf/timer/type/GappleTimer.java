package org.ipvp.hcf.timer.type;

import com.doctordark.util.imagemessage.ImageChar;
import com.doctordark.util.imagemessage.ImageMessage;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.hcf.DurationFormatter;
import org.ipvp.hcf.timer.PlayerTimer;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Timer used to prevent {@link Player}s from using Notch Apples too often.
 */
public class GappleTimer extends PlayerTimer implements Listener {

    private static final String GOPPLE_ART_FILE_TYPE = "png";
    private static final String GOPPLE_ART_FILE_NAME = "/gopple-art." + GOPPLE_ART_FILE_TYPE;

    // private final ImageMessage goppleArtMessage;

    public GappleTimer(JavaPlugin plugin) {
        super("Gapple", TimeUnit.HOURS.toMillis(6L));

        // Try and create the file from the JAR first.
        File file = new File(plugin.getDataFolder(), GOPPLE_ART_FILE_NAME);
        try {
            if (file.createNewFile()) {
                ImageIO.write(ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(GOPPLE_ART_FILE_NAME)), GOPPLE_ART_FILE_TYPE, file);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // TODO: enable gopple image
        /*
         * this.goppleArtMessage = ImageMessage.newInstance(GOPPLE_ART_FILE_NAME, plugin.getDataFolder(), 8, ImageChar.BLOCK.getChar()).appendText("", "", ChatColor.GOLD.toString() + ChatColor.BOLD +
         * ' ' + this.name + ':', ChatColor.GRAY + "  Consumed", ChatColor.GOLD + " Cooldown Remaining:", ChatColor.GRAY + "  " + DurationFormatUtils.formatDurationWords(this.defaultCooldown, true,
         * true));
         */
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.GOLD.toString() + ChatColor.BOLD;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack stack = event.getItem();
        if (stack != null && stack.getType() == Material.GOLDEN_APPLE && stack.getDurability() == 1) {
            Player player = event.getPlayer();
            if (setCooldown(player, player.getUniqueId(), defaultCooldown, false, new Predicate<Long>() {
                @Override
                public boolean apply(@Nullable Long value) {
                    return false;
                }
            })) {

                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "You have consumed a God Apple!");
                // goppleArtMessage.sendToPlayer(player);
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You still have a " + getDisplayName() + ChatColor.RED + " cooldown for another " + ChatColor.BOLD
                        + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.RED + '.');
            }
        }
    }
}
