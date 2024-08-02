package me.trajkot.aih.listeners.bukkit;

import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class AIHElytraListener implements Listener {

    @EventHandler
    public void onElytra(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if (aihPlayer == null) return;

        if(e.isFlying()) {
            aihPlayer.setUsingElytra(true);
        } else {
            aihPlayer.setUsingElytra(false);
            aihPlayer.setLastFinishedFlyingWithElytra(System.currentTimeMillis());
        }
    }
}