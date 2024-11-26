package me.rowyourboat.limitedlife.commands.subcommands;

import me.rowyourboat.limitedlife.LimitedLife;
import me.rowyourboat.limitedlife.listeners.EnchantmentLimitations;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MassNerfCommand {
    public static boolean execute(CommandSender sender, String[] args) {
        Collection<? extends Player> p = sender.getServer().getOnlinePlayers();
        p.forEach(player -> {
            player.getInventory().forEach(item -> {
                if (item == null) return;
                Map<Enchantment, Integer> e = item.getEnchantments();
                e.forEach(((enchantment, level) -> {
                    int levelLimit = LimitedLife.plugin.getConfig().getInt("anvil.level-limit",3);
                    if (level > levelLimit) {
                        String displayName;
                        if (item.getItemMeta().hasDisplayName()) {
                            displayName = item.getItemMeta().getDisplayName();
                        } else {
                            displayName = item.getType().name();
                        }
                        player.sendMessage(
                                "Enchantment Nerf!! Your " + displayName +
                                        " has had the enchantment " + enchantment.getKey() +
                                        " nerfed!"
                        );
                        item.removeEnchantment(enchantment);
                        item.addUnsafeEnchantment(enchantment, 3);
                    }
                }));

            });
        });
        return true;
    }

}
