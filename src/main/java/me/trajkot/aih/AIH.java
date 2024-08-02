package me.trajkot.aih;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.trajkot.aih.check.AIHCheck;
import me.trajkot.aih.command.AIHCommandManager;
import me.trajkot.aih.config.AIHConfig;
import me.trajkot.aih.listeners.AIHPacketListener;
import me.trajkot.aih.listeners.bukkit.AIHElytraListener;
import me.trajkot.aih.listeners.bukkit.AIHRegisterListener;
import me.trajkot.aih.listeners.bukkit.AIHResurrectListener;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import me.trajkot.aih.updatechecker.AIHUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class AIH extends JavaPlugin {

    private AIHPlayerManager playerManager;
    private AIHConfig aihConfig;

    public static AIH INSTANCE;

    private String latestVersion;
    private boolean isUpdateAvailable;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true).bStats(false).checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        playerManager = new AIHPlayerManager();

        saveDefaultConfig();
        aihConfig = new AIHConfig();

        new AIHUpdateChecker().getLatestVersion(version -> {
            if (!getDescription().getVersion().equalsIgnoreCase(version)) {
                latestVersion = version;
                isUpdateAvailable = true;
                if(aihConfig.isUpdateMessage()) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if(player.hasPermission("aih.main") || player.hasPermission("aih.alerts")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', aihConfig.getPrefix() + " &aAvailable Update " + latestVersion + "!\n&7https://www.spigotmc.org/resources/anti-inventory-hacks.118469/"));
                            player.sendMessage("");
                        }
                    });

                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', aihConfig.getPrefix() + " &aAvailable Update " + latestVersion + "!\n&7https://www.spigotmc.org/resources/anti-inventory-hacks.118469/"));
                    Bukkit.getConsoleSender().sendMessage("");
                }
            } else {
                latestVersion = getDescription().getVersion();
                isUpdateAvailable = false;
            }
        });

        AIHCommandManager commandManager = new AIHCommandManager();

        Bukkit.getOnlinePlayers().forEach(player -> {
            playerManager.registerAIHPlayer(player);

            AIHPlayer aihPlayer = playerManager.getAIHPlayer(player);

            if(player.hasPermission("aih.main") || player.hasPermission("aih.alerts")) {
                playerManager.registerAIHStaff(player);
                aihPlayer.setAlertsEnabled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', aihConfig.getPrefix() + " &7Alerts " + (aihPlayer.isAlertsEnabled() ? "enabled" : "disabled")));
                if(isUpdateAvailable && aihConfig.isUpdateMessage()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', aihConfig.getPrefix() + " &aAvailable Update " + latestVersion + "!\n&7https://www.spigotmc.org/resources/anti-inventory-hacks.118469/"));
                    player.sendMessage("");
                }
            }

            aihPlayer.resetViolations();
        });

        getCommand("aih").setExecutor(commandManager);

        Bukkit.getServer().getPluginManager().registerEvents(new AIHRegisterListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new AIHCheck(), this);
        if(PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) Bukkit.getServer().getPluginManager().registerEvents(new AIHElytraListener(), this);
        if(PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_11)) Bukkit.getServer().getPluginManager().registerEvents(new AIHResurrectListener(), this);

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

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}