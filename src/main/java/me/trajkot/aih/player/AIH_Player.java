package me.trajkot.aih.player;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class AIH_Player {
    public final Player player;

    public int violations;

    public boolean isInventoryOpen;
    public long lastInventoryOpen, lastInventoryClose, lastInventoryClick;

    public int itemStealerVL;

    public boolean gotPunished = false;
    public int lastClickedSlot;
    public long lastTimeSuspiciousForChestStealer;
    public final List<Long> lastItemClickDiffs = new ArrayList<>();

    public AIH_Player(Player player) {
        this.player = player;
        violations = 0;
    }

    public void resetViolations() {
        violations = 0;
        gotPunished = false;
        itemStealerVL = 0;
    }
}