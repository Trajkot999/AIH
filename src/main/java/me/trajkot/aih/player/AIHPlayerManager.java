package me.trajkot.aih.player;

import org.bukkit.entity.Player;
import java.util.*;

public class AIHPlayerManager {
    private final Map<UUID, AIHPlayer> aihPlayers = new HashMap<>();
    private final List<Player> aihStaff = new ArrayList<>();

    public final AIHPlayer getAIHPlayer(final Player player) {
        return aihPlayers.get(player.getUniqueId());
    }

    public final void registerAIHPlayer(final Player player) {
        aihPlayers.put(player.getUniqueId(), new AIHPlayer(player));
    }

    public final void unregisterAIHPlayer(final Player player) {
        aihPlayers.remove(player.getUniqueId());
    }

    public final void registerAIHStaff(final Player player) {
        aihStaff.add(player);
    }

    public final void unregisterAIHStaff(final Player player) {
        aihStaff.remove(player);
    }

    public List<Player> getAihStaff() {
        return aihStaff;
    }
}