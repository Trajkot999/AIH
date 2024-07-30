package me.trajkot.aih;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.trajkot.aih.check.Check;
import me.trajkot.aih.command.AIHCommandManager;
import me.trajkot.aih.config.AIHConfig;
import me.trajkot.aih.listener.PacketEventsListener;
import me.trajkot.aih.listener.RegisterListener;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class AIH extends JavaPlugin {

    private AIHPlayerManager playerManager;
    private AIHConfig aihConfig;

    public static AIH INSTANCE;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        playerManager = new AIHPlayerManager();
        aihConfig = new AIHConfig();

        AIHCommandManager commandManager = new AIHCommandManager();

        Bukkit.getOnlinePlayers().forEach(player -> {
            playerManager.registerAIHPlayer(player);

            AIHPlayer aihPlayer = playerManager.getAIHPlayer(player);

            if(player.hasPermission("aih.main") || player.hasPermission("aih.alerts")) {
                playerManager.registerAIHStaff(player);
                aihPlayer.setAlertsEnabled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', aihConfig.getConfig().getString("prefix") + " &fAlerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled")));
            }

            aihPlayer.resetViolations();
        });

        saveDefaultConfig();
        getCommand("aih").setExecutor(commandManager);

        Bukkit.getServer().getPluginManager().registerEvents(new RegisterListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new Check(), this);

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener(), PacketListenerPriority.NORMAL);

        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> playerManager.getAIHPlayer(player).resetViolations());
        }, aihConfig.getConfig().getInt("violations.violations-reset-interval") * 1000L, aihConfig.getConfig().getInt("violations.violations-reset-interval") * 1000L);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> playerManager.unregisterAIHPlayer(player));
        playerManager = null;
        PacketEvents.getAPI().terminate();
    }

    public AIHPlayerManager getPlayerManager() {
        return playerManager;
    }

    public AIHConfig getAIHConfig() {
        return aihConfig;
    }
}