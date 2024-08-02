package me.trajkot.aih.listeners.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class AIHResurrectListener implements Listener {

    @EventHandler
    public void onResurrect(EntityResurrectEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

            if (aihPlayer == null || PacketEvents.getAPI().getPlayerManager().getClientVersion(p).isOlderThan(ClientVersion.V_1_11)) return;

            if(!p.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)) {
                aihPlayer.setLastTotemUsed(System.currentTimeMillis());
                aihPlayer.setUsedTotem(true);
            }
        }
    }
}