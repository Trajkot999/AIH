package me.trajkot.aih;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.trajkot.aih.check.Check;
import me.trajkot.aih.config.AIHConfig;
import me.trajkot.aih.listener.RegisterListener;
import me.trajkot.aih.player.AIH_PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AIH extends JavaPlugin {

    private static AIH_PlayerManager playerManager;
    private static AIHConfig aihConfig;

    public static AIH INSTANCE;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true).checkForUpdates(false).bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        playerManager = new AIH_PlayerManager();
        aihConfig = new AIHConfig();

        Bukkit.getOnlinePlayers().forEach(player -> playerManager.registerAIHPlayer(player));
        Bukkit.getServer().getPluginManager().registerEvents(new RegisterListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new Check(), this);

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketReceive(final PacketReceiveEvent event) {
                if (!(event.getPlayer() instanceof Player)) return;
                Player player = (Player) event.getPlayer();

                if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
                    if (new WrapperPlayClientClientStatus(event).getAction().equals(WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)) {
                        playerManager.getAIHPlayer(player).isInventoryOpen = true;
                        playerManager.getAIHPlayer(player).lastInventoryOpen = System.currentTimeMillis();
                    }
                } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
                    playerManager.getAIHPlayer(player).isInventoryOpen = false;
                    playerManager.getAIHPlayer(player).lastInventoryClose = System.currentTimeMillis();
                }
            }
        });

        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                playerManager.getAIHPlayer(player).resetViolations();
            });
        }, (AIH.getAIHConfig().getConfig().getInt("violations-reset-interval") * 1000L), (AIH.getAIHConfig().getConfig().getInt("violations-reset-interval") * 1000L));
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> playerManager.unregisterAIHPlayer(player));
        playerManager = null;
        PacketEvents.getAPI().terminate();
    }

    public static AIH_PlayerManager getPlayerManager() {
        return playerManager;
    }

    public static AIHConfig getAIHConfig() {
        return aihConfig;
    }
}