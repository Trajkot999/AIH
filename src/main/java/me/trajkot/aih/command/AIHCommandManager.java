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
            String prefix = AIH.INSTANCE.getAIHConfig().getConfig().getString("prefix");

            if(args.length > 0) {
                if (args[0].contains("help") || args[0].contains("command")) {
                    if(sender instanceof Player) {
                        sendMessage("", sender);
                        sendMessage(prefix + " &6List of available commands:", sender);
                        sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                        sendMessage("&e/aih reload &7- Reloads config", sender);
                        sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                        sendMessage("", sender);
                    } else {
                        sendMessage("", sender);
                        sendMessage(prefix + " &6List of available commands:", sender);
                        sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                        sendMessage("&e/aih reload &7- Reloads config", sender);
                        sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                        sendMessage("", sender);
                    }
                    return true;
                } else if (args.length == 1 && args[0].equalsIgnoreCase("alerts")) {
                    if(sender instanceof Player) {
                        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer((Player) sender);
                        aihPlayer.setAlertsEnabled(!aihPlayer.isAlertsEnabled());
                        sendMessage(prefix + " &fAlerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled"), sender);
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
                    AIH.INSTANCE.getAIHConfig().reloadConfig();
                    sendMessage(prefix + " &aReloaded config!", sender);
                    return true;
                } else {
                    if(sender instanceof Player) {
                        sendMessage("", sender);
                        sendMessage(prefix + " &cUnknown command!", sender);
                        sendMessage(prefix + " &6List of available commands:", sender);
                        sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                        sendMessage("&e/aih reload &7- Reloads config", sender);
                        sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                        sendMessage("", sender);
                    } else {
                        sendMessage("", sender);
                        sendMessage(prefix + " &cUnknown command!", sender);
                        sendMessage(prefix + " &6List of available commands:", sender);
                        sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                        sendMessage("&e/aih reload &7- Reloads config", sender);
                        sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                        sendMessage("", sender);
                    }
                    return true;
                }
            } else {
                if(sender instanceof Player) {
                    sendMessage("", sender);
                    sendMessage(prefix + " &6List of available commands:", sender);
                    sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                    sendMessage("&e/aih reload &7- Reloads config", sender);
                    sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                    sendMessage("", sender);
                } else {
                    sendMessage("", sender);
                    sendMessage(prefix + " &6List of available commands:", sender);
                    sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                    sendMessage("&e/aih reload &7- Reloads config", sender);
                    sendMessage("&e/aih violations <player> &7- Checks total player violations", sender);
                    sendMessage("", sender);
                }
                return true;
            }
        } else if(sender.hasPermission("aih.alerts")) {
            String prefix = AIH.INSTANCE.getAIHConfig().getConfig().getString("prefix");

            if(args.length > 0) {
                if (args[0].equalsIgnoreCase("alerts")) {
                    if(sender instanceof Player) {
                        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer((Player) sender);
                        aihPlayer.setAlertsEnabled(!aihPlayer.isAlertsEnabled());
                        sendMessage(prefix + " &fAlerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled"), sender);
                    } else {
                        sendMessage("&c[AIH] This command cannot be executed from console!", sender);
                    }
                    return true;
                }
            } else {
                if(sender instanceof Player) {
                    sendMessage("", sender);
                    sendMessage(prefix + " &6List of available commands:", sender);
                    sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                    sendMessage("", sender);
                } else {
                    sendMessage("", sender);
                    sendMessage(prefix + " &6List of available commands:", sender);
                    sendMessage("&e/aih alerts &7- Enable/Disable alerts", sender);
                    sendMessage("", sender);
                }
                return true;
            }
        }

        return false;
    }

    private void sendMessage(final String message, CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}