package me.rowyourboat.limitedlife.listeners;

import me.rowyourboat.limitedlife.LimitedLife;
import me.rowyourboat.limitedlife.data.SaveHandler;
import me.rowyourboat.limitedlife.util.SecondsToClockFormat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerEvents implements Listener {

    private final HashMap<UUID, Long> timeLostInSeconds = new HashMap<>();

    private final SaveHandler SaveHandler;
    private final JavaPlugin plugin;

    public PlayerEvents() {
        this.SaveHandler = LimitedLife.SaveHandler;
        this.plugin = LimitedLife.plugin;
    }

    @EventHandler
    public void addTimeOnKill(PlayerDeathEvent event) {
        if (LimitedLife.currentGlobalTimerTask == null) return;
        Player deadPlayer = event.getEntity();

        FileConfiguration config = plugin.getConfig();

        List<String> boogeymenUUIDList = SaveHandler.getBoogeymenList();
        Player killer = deadPlayer.getKiller();

        if (killer != null) {
            if (boogeymenUUIDList.contains(killer.getUniqueId().toString())) {
                SaveHandler.cureBoogeyman(killer.getUniqueId().toString(), true);
                newPendingTimeSubtractionTitle(deadPlayer, config.getLong("boogeyman.time-lost-on-boogey-death"));
            } else {
                String killerTeamName = SaveHandler.convertTimeToTeamName(SaveHandler.getPlayerTimeLeft(killer));
                String victimTeamName = SaveHandler.convertTimeToTeamName(SaveHandler.getPlayerTimeLeft(deadPlayer));

                if (killerTeamName.equalsIgnoreCase("RED") && victimTeamName.equalsIgnoreCase("RED")){
                    SaveHandler.addPlayerTime(killer, LimitedLife.plugin.getConfig().getLong("rewards.time-gain-on-kill"));
                    newPendingTimeSubtractionTitle(deadPlayer, config.getLong("penalties.time-lost-on-death"));
                } else {
                    if (killerTeamName.equalsIgnoreCase(victimTeamName)) {

                        if (config.getBoolean("rewards.add-time-on-kill-same-colour"))
                            SaveHandler.addPlayerTime(killer, LimitedLife.plugin.getConfig().getLong("rewards.time-gain-on-kill"));

                        if (config.getBoolean("penalties.subtract-time-on-death-same-colour"))
                            newPendingTimeSubtractionTitle(deadPlayer, config.getLong("penalties.time-lost-on-death"));

                    } else {
                        SaveHandler.addPlayerTime(killer, LimitedLife.plugin.getConfig().getLong("rewards.time-gain-on-kill"));
                        newPendingTimeSubtractionTitle(deadPlayer, config.getLong("penalties.time-lost-on-death"));
                    }
                }
            }
        } else {
            newPendingTimeSubtractionTitle(deadPlayer, config.getLong("penalties.time-lost-on-death"));
        }

        long timeLeft = SaveHandler.getPlayerTimeLeft(deadPlayer);

        if (config.getBoolean("other.drop-heads", false)) {
            // player has just died-- drop head
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD,1);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(deadPlayer);
            skull.setItemMeta(skullMeta);

            event.getDrops().add(skull);
        }


        if (timeLeft > 0 || LimitedLife.SaveHandler.getMarkedAsDeadList().contains(deadPlayer.getUniqueId().toString())) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }
        LimitedLife.TeamHandler.changeTeamAndGamemodeAccordingly(deadPlayer, timeLeft);
    }

    public void newPendingTimeSubtractionTitle(Player deadPlayer, long timeToSubtract) {
        SaveHandler.subtractPlayerTime(deadPlayer, timeToSubtract, false);
        timeLostInSeconds.put(deadPlayer.getUniqueId(), timeToSubtract);
    }

    @EventHandler
    public void sendTitleOnRespawn(PlayerRespawnEvent event) {
        if (LimitedLife.currentGlobalTimerTask == null) return;
        Player player = event.getPlayer();
        if (timeLostInSeconds.containsKey(player.getUniqueId())) {
            long timeToSubtract = timeLostInSeconds.get(player.getUniqueId());
            timeLostInSeconds.remove(player.getUniqueId());
            SaveHandler.sendTimeChangeTitle(player, ChatColor.RED + "-" + SecondsToClockFormat.convert(timeToSubtract, false));
        }
    }

    @EventHandler
    public void setTeamOnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long timeLeft = LimitedLife.SaveHandler.getPlayerTimeLeft(player);
        if (timeLeft == -1 && LimitedLife.currentGlobalTimerTask != null && !LimitedLife.currentGlobalTimerTask.playerHasActiveTimer(player)) {

            if (LimitedLife.plugin.getConfig().getBoolean("timer.balanced-time-for-latecomers"))
                LimitedLife.SaveHandler.setPlayerTimeLeft(player, LimitedLife.SaveHandler.getPluginTimeRemaining());
            else LimitedLife.SaveHandler.setPlayerTimeLeft(player, LimitedLife.plugin.getConfig().getInt("timer.start-time-in-seconds"));

            LimitedLife.currentGlobalTimerTask.startPlayerTimer(player);
        } else if (LimitedLife.currentGlobalTimerTask != null && !LimitedLife.currentGlobalTimerTask.playerHasActiveTimer(player)) {
            LimitedLife.currentGlobalTimerTask.startPlayerTimer(player);
        }
    }

    @EventHandler
    public void grantRecipesOnJoin(PlayerJoinEvent event) {
        LimitedLife.CustomRecipes.grant(event.getPlayer());
    }

}