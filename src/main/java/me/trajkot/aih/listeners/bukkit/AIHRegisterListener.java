package me.trajkot.aih.listeners.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AIHRegisterListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        AIHPlayerManager aihPlayerManager = AIH.INSTANCE.getPlayerManager();

        Player player = event.getPlayer();

        aihPlayerManager.registerAIHPlayer(player);

        AIHPlayer aihPlayer = aihPlayerManager.getAIHPlayer(player);

        if(PacketEvents.getAPI().getPlayerManager().getClientVersion(player).isNewerThanOrEquals(ClientVersion.V_1_9) && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
            if(player.getInventory().getChestplate() != null) {
                aihPlayer.setUsingElytra(player.getInventory().getChestplate().getType().equals(Material.ELYTRA));
            } else {
                aihPlayer.setLastFinishedFlyingWithElytra(System.currentTimeMillis());
                aihPlayer.setUsingElytra(false);
            }
        } else {
            aihPlayer.setLastFinishedFlyingWithElytra(System.currentTimeMillis());
            aihPlayer.setUsingElytra(false);
        }

        if(player.hasPermission("aih.main") || player.hasPermission("aih.alerts")) {
            aihPlayerManager.registerAIHStaff(player);
            aihPlayer.setAlertsEnabled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', AIH.INSTANCE.getAihConfig().getPrefix() + " &7Alerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled")));

            if(AIH.INSTANCE.isUpdateAvailable() && AIH.INSTANCE.getAihConfig().isUpdateMessage()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', AIH.INSTANCE.getAihConfig().getPrefix() + " &aAvailable Update " + AIH.INSTANCE.getLatestVersion() + "!\n&7https://www.spigotmc.org/resources/anti-inventory-hacks.118469/"));
                player.sendMessage("");
            }
        }

        aihPlayer.resetViolations();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AIHPlayerManager aihPlayerManager = AIH.INSTANCE.getPlayerManager();

        Player player = event.getPlayer();

        aihPlayerManager.unregisterAIHPlayer(player);
        aihPlayerManager.unregisterAIHStaff(player);
    }
}