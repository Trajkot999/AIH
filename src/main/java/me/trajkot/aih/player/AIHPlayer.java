package me.trajkot.aih.player;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AIHPlayer {

    private final Player player;

    private int violations;
    private boolean alertsEnabled;

    //client side ground but anticheats can still detect if its spoofed so
    private boolean isInventoryOpen, isUsingElytra, isOnGround, isInWater, usedTotem = false;
    private long lastInventoryOpen, lastInventoryClick, lastInventoryMove, lastVelocity, lastDeath, lastSlime, lastTotemUsed, lastFinishedFlyingWithElytra;

    private int suspiciousViolations, clicks;

    private double buffer, buffer2, buffer3;

    private boolean gotPunished;
    private int lastClickedSlot;
    private ItemStack lastClickedItemStack;

    private double x, y, z, yaw, pitch;
    private double deltaX, deltaY, deltaZ, deltaXZ, lastDeltaX, lastDeltaY, lastDeltaZ, lastDeltaXZ;
    private double deltaYaw, deltaPitch, lastDeltaYaw, lastDeltaPitch;

    public AIHPlayer(final Player player) {
        this.player = player;

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

    public void handleFlyingPacket(final WrapperPlayClientPlayerFlying wrapper) {
        double lastX = x;
        double lastY = y;
        double lastZ = z;

        x = wrapper.hasPositionChanged() ? wrapper.getLocation().getX() : x;
        y = wrapper.hasPositionChanged() ? wrapper.getLocation().getY() : y;
        z = wrapper.hasPositionChanged() ? wrapper.getLocation().getZ() : z;

        lastDeltaX = deltaX;
        lastDeltaY = deltaY;
        lastDeltaZ = deltaZ;
        lastDeltaXZ = deltaXZ;

        deltaX = lastX != 0 ? (x - lastX) : 0;
        deltaY = lastY != 0 ? (y - lastY) : 0;
        deltaZ = lastZ != 0 ? (z - lastZ) : 0;
        deltaXZ = Math.hypot(deltaX, deltaZ);

        double lastYaw = yaw;
        double lastPitch = pitch;

        yaw = wrapper.hasRotationChanged() ? wrapper.getLocation().getYaw() : yaw;
        pitch = wrapper.hasRotationChanged() ? wrapper.getLocation().getPitch() : pitch;

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;

        deltaYaw = lastYaw != 0 ? Math.abs(yaw - lastYaw) % 360F : 0;
        deltaPitch = lastPitch != 0 ? Math.abs(pitch - lastPitch) : 0;

        isOnGround = wrapper.isOnGround();
    }

    public Player getPlayer() {
        return player;
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

    public ItemStack getLastClickedItemStack() {
        return lastClickedItemStack;
    }

    public void setLastClickedItemStack(ItemStack lastClickedItemStack) {
        this.lastClickedItemStack = lastClickedItemStack;
    }

    public double getBuffer() {
        return buffer;
    }

    public void setBuffer(double buffer) {
        this.buffer = buffer;
    }

    public double getBuffer2() {
        return buffer2;
    }

    public void setBuffer2(double buffer2) {
        this.buffer2 = buffer2;
    }

    public long getLastInventoryMove() {
        return lastInventoryMove;
    }

    public void setLastInventoryMove(long lastInventoryMove) {
        this.lastInventoryMove = lastInventoryMove;
    }

    public long getLastVelocity() {
        return lastVelocity;
    }

    public void setLastVelocity(long lastVelocity) {
        this.lastVelocity = lastVelocity;
    }

    public long getLastDeath() {
        return lastDeath;
    }

    public void setLastDeath(long lastDeath) {
        this.lastDeath = lastDeath;
    }

    public long getLastSlime() {
        return lastSlime;
    }

    public void setLastSlime(long lastSlime) {
        this.lastSlime = lastSlime;
    }

    public long getLastTotemUsed() {
        return lastTotemUsed;
    }

    public void setLastTotemUsed(long lastTotemUsed) {
        this.lastTotemUsed = lastTotemUsed;
    }

    public boolean isUsingElytra() {
        return isUsingElytra;
    }

    public void setUsingElytra(boolean usingElytra) {
        isUsingElytra = usingElytra;
    }

    public void setLastFinishedFlyingWithElytra(long lastFinishedFlyingWithElytra) {
        this.lastFinishedFlyingWithElytra = lastFinishedFlyingWithElytra;
    }

    public long getLastFinishedFlyingWithElytra() {
        return lastFinishedFlyingWithElytra;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public double getDeltaZ() {
        return deltaZ;
    }

    public double getDeltaXZ() {
        return deltaXZ;
    }

    public double getLastDeltaX() {
        return lastDeltaX;
    }

    public double getLastDeltaY() {
        return lastDeltaY;
    }

    public double getLastDeltaZ() {
        return lastDeltaZ;
    }

    public double getLastDeltaXZ() {
        return lastDeltaXZ;
    }

    public double getBuffer3() {
        return buffer3;
    }

    public void setBuffer3(double buffer3) {
        this.buffer3 = buffer3;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public double getDeltaYaw() {
        return deltaYaw;
    }

    public double getDeltaPitch() {
        return deltaPitch;
    }

    public boolean isInWater() {
        return isInWater;
    }

    public void setInWater(boolean inWater) {
        isInWater = inWater;
    }

    public boolean isUsedTotem() {
        return usedTotem;
    }

    public void setUsedTotem(boolean usedTotem) {
        this.usedTotem = usedTotem;
    }
}