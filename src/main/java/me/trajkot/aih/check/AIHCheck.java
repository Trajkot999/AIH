package me.trajkot.aih.check;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.trajkot.aih.AIH;
import me.trajkot.aih.config.AIHConfig;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import java.util.Objects;

public class AIHCheck implements Listener {

    private final AIHConfig config;

    public AIHCheck() {
        config = AIH.INSTANCE.getAihConfig();
    }

    private void fail(Player player, String message, boolean dev) {
        AIHPlayerManager aihPlayerManager = AIH.INSTANCE.getPlayerManager();
        AIHPlayer aihPlayer = aihPlayerManager.getAIHPlayer(player);

        if (aihPlayer == null) return;

        aihPlayer.setViolations(aihPlayer.getViolations() + 1);

        String playerName = player.getName();

        final TextComponent configAlertMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getAlertMessage())
                .replaceAll("%prefix%", ChatColor.translateAlternateColorCodes('&', config.getPrefix()))
                .replaceAll("%player%", playerName)
                .replaceAll("%message%", message)
                .replaceAll("%ping%", String.valueOf(PacketEvents.getAPI().getPlayerManager().getPing(player)))
                .replaceAll("%dev%", dev ? config.getDevSymbol() : "")
                .replaceAll("%vl%", String.valueOf(aihPlayer.getViolations()))
                .replaceAll("%maxvl%", String.valueOf(config.getMaxViolations())));

        configAlertMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + playerName));
        configAlertMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&eClick to teleport to &7" + playerName)).create()));

        for (Player staffPlayer : aihPlayerManager.getAihStaff()) {
            if (aihPlayerManager.getAIHPlayer(staffPlayer).isAlertsEnabled())
                staffPlayer.spigot().sendMessage(configAlertMessage);
        }

        if (!dev && !config.getPunishCommand().isEmpty() && aihPlayer.getViolations() >= config.getMaxViolations()) {
            if (!aihPlayer.isGotPunished()) {
                if (!config.getBroadcastMessage().isEmpty())
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getBroadcastMessage().replaceAll("%player%", playerName)));
                Bukkit.getScheduler().runTask(AIH.INSTANCE, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&', config.getPunishCommand().replaceAll("%player%", playerName))));
                aihPlayer.setGotPunished(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if (aihPlayer == null) return;

        aihPlayer.setInventoryOpen(false);
        aihPlayer.setClicks(0);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if (aihPlayer == null) return;

        aihPlayer.setLastInventoryOpen(System.currentTimeMillis());
        aihPlayer.setClicks(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if (aihPlayer == null) return;

        if (p.getGameMode().equals(GameMode.CREATIVE)) return;

        ClientVersion clientVersion = PacketEvents.getAPI().getPlayerManager().getClientVersion(p);
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();

        if (p.isSprinting()) {
            String message = "sprint click";

            if(clientVersion.isNewerThanOrEquals(ClientVersion.V_1_9) && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) && e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.ELYTRA)) {
                message = "elytra swapper";
            } else if(clientVersion.isNewerThanOrEquals(ClientVersion.V_1_11) && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_11) && e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.TOTEM_OF_UNDYING) && (System.currentTimeMillis() - aihPlayer.getLastTotemUsed() < 750)) {
                message = "auto totem";
            }

            fail(p, message, false);

            if (config.isCancelClick()) {
                p.closeInventory();
                p.setSprinting(false);
            }
        }

        if (p.isSneaking()) {
            String message = "sneak click";

            if(clientVersion.isNewerThanOrEquals(ClientVersion.V_1_9) && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) && e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.ELYTRA)) {
                message = "elytra swapper";
            } else if(clientVersion.isNewerThanOrEquals(ClientVersion.V_1_11) && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_11) && e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.TOTEM_OF_UNDYING) && (System.currentTimeMillis() - aihPlayer.getLastTotemUsed() < 750)) {
                message = "auto totem";
            }

            fail(p, message, false);

            if (config.isCancelClick()) {
                p.closeInventory();
                p.setSneaking(false);
            }
        }

        if (p.isBlocking()) {
            fail(p, clientVersion.isNewerThanOrEquals(ClientVersion.V_1_9) ? "shield" : "sword" + " block click", false);

            if (config.isCancelClick()) {
                p.closeInventory();
                p.setSneaking(false);
            }
        }

        if(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_12)) {
            double maxDeltaXZ = 0.165;

            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.ICE) || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.PACKED_ICE)) {
                maxDeltaXZ = 0.35;
            }

            if((aihPlayer.getDeltaXZ() > maxDeltaXZ || p.isSprinting() || p.isSneaking() || p.isBlocking() || aihPlayer.getDeltaYaw() > 1 || aihPlayer.getDeltaPitch() > 1) && aihPlayer.isOnGround() && !aihPlayer.isInWater() && !p.getAllowFlight() && !aihPlayer.isUsingElytra() && System.currentTimeMillis() - aihPlayer.getLastVelocity() > 2000 && System.currentTimeMillis() - aihPlayer.getLastFinishedFlyingWithElytra() > 3000 && System.currentTimeMillis() - aihPlayer.getLastSlime() > 3000 && System.currentTimeMillis() - aihPlayer.getLastDeath() > 3000) {
                aihPlayer.setBuffer3(aihPlayer.getBuffer3() + 1);
                if (aihPlayer.getBuffer3() > 1) {
                    fail(p, "inventory move", false);

                    if (config.isCancelClick()) {
                        e.setCancelled(true);
                    }
                }
            } else {
                aihPlayer.setBuffer(Math.max(aihPlayer.getBuffer3() - 0.5, 0));
            }
        }

        if (!aihPlayer.isInventoryOpen() && clientVersion.isOlderThan(ClientVersion.V_1_12) && serverVersion.isOlderThan(ServerVersion.V_1_12)) {
            aihPlayer.setClicks(aihPlayer.getClicks() + 1);

            if (aihPlayer.getClicks() > 1) {
                fail(p, "closed inventory click", false);

                if (config.isCancelClick()) {
                    e.setCancelled(true);
                    p.closeInventory();
                }
            }
        }

        long lastTimeOpenedInv = aihPlayer.getLastInventoryOpen();

        if (lastTimeOpenedInv < aihPlayer.getLastInventoryMove()) {
            if (config.isCancelClick()) {
                p.closeInventory();
                e.setCancelled(true);
            }
        }

        boolean isInvalid = e.getSlotType().equals(InventoryType.SlotType.FUEL) || e.getSlotType().equals(InventoryType.SlotType.CRAFTING) || e.getSlotType().equals(InventoryType.SlotType.RESULT);
        if (isInvalid) return;

        if (System.currentTimeMillis() - lastTimeOpenedInv < 50) {
            fail(p, "suspicious first click", true);

            if (config.isCancelClick()) {
                p.closeInventory();
                e.setCancelled(true);
            }
        }

        long lastClick = System.currentTimeMillis() - aihPlayer.getLastInventoryClick();

        if (e.getCurrentItem() == null || e.getAction().equals(InventoryAction.NOTHING)) return;

        if (aihPlayer.getLastClickedItemStack() != null)
            if (aihPlayer.getLastClickedItemStack().getType().equals(e.getCurrentItem().getType())) return;

        if (lastClick < 1L) {
            if (e.getAction().equals(InventoryAction.HOTBAR_SWAP) || e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) && aihPlayer.getViolations() < 10) return;
            if(e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) return;

            aihPlayer.setBuffer(aihPlayer.getBuffer() + 1);
            if (aihPlayer.getBuffer() > 1 || (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_9) && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) && e.getCurrentItem().getType().equals(Material.ELYTRA))) {
                fail(p, (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_9) && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) && e.getCurrentItem().getType().equals(Material.ELYTRA)) ? "elytra swapper" : "instant click", false);

                if (config.isCancelClick()) {
                    e.setCancelled(true);
                }
            }
        } else {
            aihPlayer.setBuffer(Math.max(aihPlayer.getBuffer() - 0.05, 0));
        }

        if(lastClick > 0) {
            int slot = e.getSlot();
            int lastSlot = aihPlayer.getLastClickedSlot();
            double distance = distanceBetween(slot, lastSlot);
            double minClickDelay = distance * 40;

            if(e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
                if(distance < 4) {
                    minClickDelay = 10;
                } else {
                    minClickDelay = 20;
                }
            }

            if(e.getAction().equals(InventoryAction.DROP_ALL_SLOT)) {
                if(distance <= 1) {
                    minClickDelay = 120;
                } else if(distance > 1 && distance <= 2) {
                    minClickDelay = 140;
                } else if(distance > 2 && distance < 4) {
                    minClickDelay = distance * 37;
                } else if(distance >= 4) {
                    minClickDelay = 140;
                }
            }

            if(e.getClickedInventory() != null) {
                if(!(e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && e.getClickedInventory().getType().equals(InventoryType.PLAYER) && (e.getSlot() >= 9 && e.getSlot() <= 35) || (e.getSlot() >= 0 && e.getSlot() <= 8))) {
                    int vl = lastClick < minClickDelay ? lastClick < minClickDelay / 2 ? 8 : 4 : 0;

                    int suspiciousViolations = aihPlayer.getSuspiciousViolations();
                    int totalVl = suspiciousViolations + vl;
                    aihPlayer.setSuspiciousViolations(totalVl);

                    if (vl < 2 && totalVl > 2) {
                        int currentSuspiciousViolations = aihPlayer.getSuspiciousViolations();
                        int deduction = ((lastClick * 2L) > minClickDelay) ? 2 : 1;
                        currentSuspiciousViolations -= deduction;
                        aihPlayer.setSuspiciousViolations(currentSuspiciousViolations);
                    }

                    if (totalVl > 4 && vl > 2) {
                        fail(p, "fast click", false);

                        if (config.isCancelClick()) {
                            e.setCancelled(true);
                        }
                    }
                }
            }

            aihPlayer.setLastClickedSlot(slot);
            aihPlayer.setLastInventoryClick(System.currentTimeMillis());
            aihPlayer.setLastClickedItemStack(e.getCurrentItem());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if(p.isDead() || aihPlayer == null) return;

        if(PacketEvents.getAPI().getPlayerManager().getClientVersion(p).isNewerThanOrEquals(ClientVersion.V_1_11) && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_11)) {
            double maxDeltaXZ = 0.165;

            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.ICE) || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.PACKED_ICE)) {
                maxDeltaXZ = 0.35;
            }

            if(aihPlayer.isUsedTotem() && p.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)) {
                if(aihPlayer.getDeltaXZ() > maxDeltaXZ || p.isSprinting() || p.isSneaking() || p.isBlocking() || aihPlayer.getDeltaYaw() > 1 || aihPlayer.getDeltaPitch() > 1) {
                    if(System.currentTimeMillis() - aihPlayer.getLastTotemUsed() < 245) {
                        fail(p, "auto totem", true);
                        aihPlayer.setUsedTotem(false);
                    }
                } else {
                    if(System.currentTimeMillis() - aihPlayer.getLastTotemUsed() < 85) {
                        fail(p, "auto totem", true);
                        aihPlayer.setUsedTotem(false);
                    }
                }
            }

            if(aihPlayer.isUsedTotem() && System.currentTimeMillis() - aihPlayer.getLastTotemUsed() >= 300) {
                aihPlayer.setUsedTotem(false);
            }
        }

        Location to = e.getTo();
        Location from = e.getFrom();

        if(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name().toLowerCase().contains("slime")) {
            aihPlayer.setLastSlime(System.currentTimeMillis());
        }

        assert to != null;
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            Location location = p.getLocation();
            World world = location.getWorld();
            double x = location.getX();
            double y = location.getY() + 1;
            double z = location.getZ();

            Block blockBelow = location.getBlock().getRelative(BlockFace.DOWN);
            Block blockBelowLoc = new Location(world, x, y, z).getBlock().getRelative(BlockFace.DOWN);
            Block blockBelowExp1 = new Location(world, x + 0.5, y, z + 0.5).getBlock().getRelative(BlockFace.DOWN);
            Block blockBelowExp2 = new Location(world, x - 0.5, y, z - 0.5).getBlock().getRelative(BlockFace.DOWN);

            boolean isInWater = blockBelow.getType().name().toLowerCase().contains("water") || blockBelowLoc.getType().name().toLowerCase().contains("water") || blockBelowExp1.getType().name().toLowerCase().contains("water") || blockBelowExp2.getType().name().toLowerCase().contains("water");
            aihPlayer.setInWater(isInWater);
        }

        if(aihPlayer.isInWater()) {
            return;
        }

        if (aihPlayer.isInventoryOpen() && !aihPlayer.isOnGround() && aihPlayer.getDeltaY() != 0 && System.currentTimeMillis() - aihPlayer.getLastVelocity() > 2000) {
            double var7 = aihPlayer.getLastDeltaX() * 0.9100000262260437D - aihPlayer.getDeltaX();
            double var11 = aihPlayer.getLastDeltaZ() * 0.9100000262260437D - aihPlayer.getDeltaZ();
            double strafe = StrictMath.sqrt(var7 * var7 + var11 * var11);
            if (strafe > 0.02D) {
                aihPlayer.setBuffer2(aihPlayer.getBuffer2() + 1);
                if (aihPlayer.getBuffer2() > 2) {
                    fail(p, "inventory move", true);

                    if(config.isCancelClick()) {
                        p.closeInventory();
                        e.setCancelled(true);
                    }
                }
            } else {
                aihPlayer.setBuffer2(Math.max(aihPlayer.getBuffer2() - 2, 0));
            }
        }

        if (p.getAllowFlight()) return;
        if (System.currentTimeMillis() - aihPlayer.getLastSlime() < 5000) return;
        if (System.currentTimeMillis() - aihPlayer.getLastDeath() < 5000) return;

        if (aihPlayer.isInventoryOpen()) {
            double maxOpenTime = 2000;
            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.ICE) || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.PACKED_ICE)) maxOpenTime = 1600;
            if (System.currentTimeMillis() - aihPlayer.getLastInventoryOpen() < maxOpenTime) return;
            if (aihPlayer.getLastVelocity() > aihPlayer.getLastInventoryOpen()) return;
            Location to2 = to.clone();
            to2.setY(from.getY());
            if (!Objects.equals(from.getWorld(), to2.getWorld())) {
                aihPlayer.setInventoryOpen(false);
                return;
            }
            double f = from.distance(to2);
            double last = aihPlayer.getDeltaXZ();
            if (last < f || (last > 0.05 && last == f)) {
                fail(p, "inventory move", true);

                if(config.isCancelClick()) {
                    p.closeInventory();
                    e.setCancelled(true);
                }
                aihPlayer.setLastInventoryMove(System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent e) {
        Player p = e.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if (aihPlayer == null) return;

        aihPlayer.setLastVelocity(System.currentTimeMillis());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(p);

        if (aihPlayer == null) return;

        aihPlayer.setLastDeath(System.currentTimeMillis());
    }

    private int[] translatePosition(int slot) {
        int row = slot / 9 + 1;
        int rowPosition = slot - (row - 1) * 9;
        return new int[]{row, rowPosition};
    }

    private double distanceBetween(int slot1, int slot2) {
        int[] slot1XZ = translatePosition(slot1);
        int[] slot2XZ = translatePosition(slot2);
        return Math.sqrt(((slot1XZ[0] - slot2XZ[0]) * (slot1XZ[0] - slot2XZ[0]) + (slot1XZ[1] - slot2XZ[1]) * (slot1XZ[1] - slot2XZ[1])));
    }
}