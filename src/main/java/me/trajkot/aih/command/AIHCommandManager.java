package me.trajkot.aih.command;

import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AIHCommandManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("aih.main")) {
            String prefix = AIH.INSTANCE.getAihConfig().getPrefix();

            if(args.length > 0) {
                if (args[0].contains("help") || args[0].contains("command")) {
                    sendMessage("", sender);
                    sendMessage(prefix + " &eList of available commands:", sender);
                    sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                    sendMessage("&e/aih reload &7- Reloads config", sender);
                    sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                    sendMessage("", sender);
                    return true;
                } else if (args.length == 1 && args[0].equalsIgnoreCase("alerts")) {
                    if(sender instanceof Player) {
                        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer((Player) sender);
                        aihPlayer.setAlertsEnabled(!aihPlayer.isAlertsEnabled());
                        sendMessage(prefix + " &7Alerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled"), sender);
                    } else {
                        sender.sendMessage("&c[AIH] This command cannot be executed from console!");
                    }
                    return true;
                } else if (args.length == 2 && args[0].equalsIgnoreCase("violations")) {
                    if(sender instanceof Player) {
                        final Player player = Bukkit.getPlayer(args[1]);

                        if(player != null && player.isOnline()) {
                            AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(player);
                            sendMessage(prefix + " &e" + player.getName() + " &7got total of &e" + aihPlayer.getViolations() + " &7violations.", sender);
                        } else {
                            sendMessage(prefix + " &cCannot find player named " + args[1], sender);
                        }
                    } else {
                        sender.sendMessage("&c[AIH] This command cannot be executed from console!");
                    }
                    return true;
                } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    AIH.INSTANCE.reloadConfig();
                    AIH.INSTANCE.getAihConfig().reloadConfig();
                    sendMessage(AIH.INSTANCE.getAihConfig().getPrefix() + " &7Reloaded config!", sender);
                    return true;
                } else {
                    sendMessage("", sender);
                    sendMessage(prefix + " &cUnknown command!", sender);
                    sendMessage(prefix + " &eList of available commands:", sender);
                    sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                    sendMessage("&e/aih reload &7- Reloads config", sender);
                    sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                    sendMessage("", sender);
                    return true;
                }
            } else {
                sendMessage("", sender);
                sendMessage(prefix + " &eList of available commands:", sender);
                sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                sendMessage("&e/aih reload &7- Reloads config", sender);
                sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                sendMessage("", sender);
                return true;
            }
        } else if(sender.hasPermission("aih.alerts")) {
            String prefix = AIH.INSTANCE.getAihConfig().getPrefix();

            if(args.length > 0) {
                if (args[0].equalsIgnoreCase("alerts")) {
                    if(sender instanceof Player) {
                        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer((Player) sender);
                        aihPlayer.setAlertsEnabled(!aihPlayer.isAlertsEnabled());
                        sendMessage(prefix + " &7Alerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled"), sender);
                    } else {
                        sendMessage("&c[AIH] This command cannot be executed from console!", sender);
                    }
                    return true;
                }
            } else {
                sendMessage("", sender);
                sendMessage(prefix + " &eList of available commands:", sender);
                sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                sendMessage("", sender);
                return true;
            }
        }

        return false;
    }

    private void sendMessage(final String message, CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}