package me.trajkot.aih.config;

import me.trajkot.aih.AIH;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class AIHConfig {

    private FileConfiguration config;
    private File configf;

    public AIHConfig() {
        createConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void createConfig() {
        configf = new File(AIH.INSTANCE.getDataFolder(), "config.yml");

        if(!configf.exists()) {
            configf.getParentFile().mkdirs();
            AIH.INSTANCE.saveResource( "config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configf);
        } catch (Exception ignore) {}
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configf);
        try {
            config.load(configf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}