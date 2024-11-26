package me.rowyourboat.limitedlife.commands;

import me.rowyourboat.limitedlife.LimitedLife;
import me.rowyourboat.limitedlife.commands.subcommands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MainCommandExecutor implements CommandExecutor {

    public static void commandFeedback(CommandSender sender, String str) {
        if (!LimitedLife.plugin.getConfig().getBoolean("other.command-feedback")) return;

        String finalMessage = ChatColor.GRAY + ChatColor.ITALIC.toString() + "[" + sender.getName() + ": " + str + "]";
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("limitedlife.admin") && !player.getName().equalsIgnoreCase(sender.getName()))
                player.sendMessage(finalMessage);
        }
        if (!(sender instanceof ConsoleCommandSender))
            Bukkit.getConsoleSender().sendMessage(finalMessage);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender commandSender, @NonNull Command command, @NonNull String s, String[] args) {
        if (args.length >= 1) {
            return switch (args[0].toLowerCase()) {
                case "timer" -> TimerCommand.execute(commandSender, args);
                case "boogeyman" -> BoogeymanCommand.execute(commandSender, args);
                case "modifytime" -> ModifyTimeCommand.execute(commandSender, args);
                case "gettime" -> GetTimeCommand.execute(commandSender, args);
                case "reload" -> ReloadCommand.execute(commandSender);
                case "help" -> TimerCommand.execute(commandSender, args);
                case "massnerf" -> MassNerfCommand.execute(commandSender, args);
                default -> HelpCommand.execute(commandSender, args);
            };
        }
        return false;
    }
}
