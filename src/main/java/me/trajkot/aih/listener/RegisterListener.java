package me.trajkot.aih.listener;

import me.trajkot.aih.AIH;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RegisterListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        AIH.getPlayerManager().registerAIHPlayer(event.getPlayer());

        if(event.getPlayer().isOp() || event.getPlayer().hasPermission("aih.alerts")) {
            AIH.getPlayerManager().registerAIHStaff(event.getPlayer());
        }

        AIH.getPlayerManager().getAIHPlayer(event.getPlayer()).resetViolations();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AIH.getPlayerManager().unregisterAIHPlayer(event.getPlayer());
        AIH.getPlayerManager().unregisterAIHStaff(event.getPlayer());
    }
}