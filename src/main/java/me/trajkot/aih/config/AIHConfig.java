package me.trajkot.aih.config;

import me.trajkot.aih.AIH;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class AIHConfig {

    FileConfiguration config;
    static File configf;

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
}