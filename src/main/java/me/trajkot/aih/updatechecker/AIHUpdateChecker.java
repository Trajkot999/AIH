package me.trajkot.aih.updatechecker;

import me.trajkot.aih.AIH;
import org.bukkit.Bukkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class AIHUpdateChecker {

    public void getLatestVersion(Consumer<String> c) {
        Bukkit.getScheduler().runTaskAsynchronously(AIH.INSTANCE, () -> {
            try (InputStream url = new URL("https://api.spigotmc.org/legacy/update.php?resource=118469").openStream();
                 Scanner s = new Scanner(url)) {
                 if (s.hasNext()) {
                     c.accept(s.next());
                 }
            } catch (IOException e) {
                AIH.INSTANCE.getLogger().info("Cannot retrieve update information: " + e.getMessage());
            }
        });
    }
}