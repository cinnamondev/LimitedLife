package me.rowyourboat.limitedlife.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MainTabCompleter implements TabCompleter {

    public List<String> getSubCommands() {
        List<String> arguments = new ArrayList<>();
        arguments.add("countdown");
        arguments.add("boogeyman");
        return arguments;
    }

    public List<String> getCountdownCommands() {
        List<String> arguments = new ArrayList<>();
        arguments.add("start");
        arguments.add("pause");
        arguments.add("reset");
        return arguments;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1)
            return getSubCommands();
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("countdown"))
                return getCountdownCommands();
        }

        return new ArrayList<>();
    }
}
