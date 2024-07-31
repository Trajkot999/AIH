package me.trajkot.aih;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.trajkot.aih.check.AIHCheck;
import me.trajkot.aih.command.AIHCommandManager;
import me.trajkot.aih.config.AIHConfig;
import me.trajkot.aih.listeners.AIHPacketListener;
import me.trajkot.aih.listeners.AIHBukkitListener;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import org.bstats.bukkit.Metrics;
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

        saveDefaultConfig();
        aihConfig = new AIHConfig();

        AIHCommandManager commandManager = new AIHCommandManager();

        Bukkit.getOnlinePlayers().forEach(player -> {
            playerManager.registerAIHPlayer(player);

            AIHPlayer aihPlayer = playerManager.getAIHPlayer(player);

            if(player.hasPermission("aih.main") || player.hasPermission("aih.alerts")) {
                playerManager.registerAIHStaff(player);
                aihPlayer.setAlertsEnabled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', aihConfig.getPrefix() + " &7Alerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled")));
            }

            aihPlayer.resetViolations();
        });

        getCommand("aih").setExecutor(commandManager);

        Bukkit.getServer().getPluginManager().registerEvents(new AIHBukkitListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new AIHCheck(), this);

        new Metrics(this, 22851);

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new AIHPacketListener(), PacketListenerPriority.NORMAL);

        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> playerManager.getAIHPlayer(player).resetViolations());
        }, aihConfig.getViolationsResetInterval() * 1000L, aihConfig.getViolationsResetInterval() * 1000L);
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

    public AIHConfig getAihConfig() {
        return aihConfig;
    }
}