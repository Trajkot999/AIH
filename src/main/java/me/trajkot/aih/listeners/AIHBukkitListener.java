package me.trajkot.aih.listeners;

import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AIHBukkitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        AIHPlayerManager aihPlayerManager = AIH.INSTANCE.getPlayerManager();

        aihPlayerManager.registerAIHPlayer(event.getPlayer());

        AIHPlayer aihPlayer = aihPlayerManager.getAIHPlayer(event.getPlayer());

        if(event.getPlayer().hasPermission("aih.main") || event.getPlayer().hasPermission("aih.alerts")) {
            aihPlayerManager.registerAIHStaff(event.getPlayer());
            aihPlayer.setAlertsEnabled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', AIH.INSTANCE.getAIHConfig().getConfig().getString("prefix") + " &fAlerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled")));
        }

        aihPlayer.resetViolations();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AIHPlayerManager aihPlayerManager = AIH.INSTANCE.getPlayerManager();

        aihPlayerManager.unregisterAIHPlayer(event.getPlayer());
        aihPlayerManager.unregisterAIHStaff(event.getPlayer());
    }
}