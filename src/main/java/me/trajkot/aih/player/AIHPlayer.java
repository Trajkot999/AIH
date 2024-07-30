package me.trajkot.aih.player;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class AIHPlayer {

    private int violations;
    private boolean alertsEnabled;

    private boolean isInventoryOpen;
    private long lastInventoryOpen, lastInventoryClick;

    private int suspiciousViolations, clicks;

    private boolean gotPunished;
    private int lastClickedSlot;
    private ItemStack lastClickedItemStack;
    private long lastTimeSuspicious;
    private final List<Long> lastClicks = new ArrayList<>();

    public AIHPlayer() {
        alertsEnabled = false;
        violations = 0;
        clicks = 0;
        gotPunished = false;
        suspiciousViolations = 0;
    }

    public void resetViolations() {
        violations = 0;
        gotPunished = false;
        suspiciousViolations = 0;
    }

    public int getViolations() {
        return violations;
    }

    public void setViolations(int violations) {
        this.violations = violations;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
    }

    public boolean isInventoryOpen() {
        return isInventoryOpen;
    }

    public void setInventoryOpen(boolean inventoryOpen) {
        isInventoryOpen = inventoryOpen;
    }

    public long getLastInventoryOpen() {
        return lastInventoryOpen;
    }

    public void setLastInventoryOpen(long lastInventoryOpen) {
        this.lastInventoryOpen = lastInventoryOpen;
    }

    public long getLastInventoryClick() {
        return lastInventoryClick;
    }

    public void setLastInventoryClick(long lastInventoryClick) {
        this.lastInventoryClick = lastInventoryClick;
    }

    public int getSuspiciousViolations() {
        return suspiciousViolations;
    }

    public void setSuspiciousViolations(int vl) {
        this.suspiciousViolations = vl;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public boolean isGotPunished() {
        return gotPunished;
    }

    public void setGotPunished(boolean gotPunished) {
        this.gotPunished = gotPunished;
    }

    public int getLastClickedSlot() {
        return lastClickedSlot;
    }

    public void setLastClickedSlot(int lastClickedSlot) {
        this.lastClickedSlot = lastClickedSlot;
    }

    public long getLastTimeSuspicious() {
        return lastTimeSuspicious;
    }

    public void setLastTimeSuspicious(long time) {
        this.lastTimeSuspicious = time;
    }

    public List<Long> getLastClicks() {
        return lastClicks;
    }

    public ItemStack getLastClickedItemStack() {
        return lastClickedItemStack;
    }

    public void setLastClickedItemStack(ItemStack lastClickedItemStack) {
        this.lastClickedItemStack = lastClickedItemStack;
    }
}