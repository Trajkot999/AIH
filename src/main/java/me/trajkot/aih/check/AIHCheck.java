package me.trajkot.aih.check;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import me.trajkot.aih.player.AIHPlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class AIHCheck implements Listener {

    private final boolean cancelClick;
    private final boolean safeCancelClick;

    private final String prefix;
    private final String alertMessage;
    private final String devSymbol;

    private final int maxViolations;
    private final String punishCommand;
    private final String broadcastMessage;

    public AIHCheck() {
        cancelClick = AIH.INSTANCE.getAIHConfig().getConfig().getBoolean("cancel-click");
        safeCancelClick = AIH.INSTANCE.getAIHConfig().getConfig().getBoolean("safe-cancel-click");

        prefix = AIH.INSTANCE.getAIHConfig().getConfig().getString("prefix");
        alertMessage = AIH.INSTANCE.getAIHConfig().getConfig().getString("alerts.alert-message");
        devSymbol = AIH.INSTANCE.getAIHConfig().getConfig().getString("alerts.dev-symbol");

        punishCommand = AIH.INSTANCE.getAIHConfig().getConfig().getString("punishments.punish-command");
        broadcastMessage = AIH.INSTANCE.getAIHConfig().getConfig().getString("punishments.broadcast-message");
        maxViolations = AIH.INSTANCE.getAIHConfig().getConfig().getInt("violations.max-violations");
    }

    private void fail(Player player, String message, boolean dev) {
        AIHPlayerManager aihPlayerManager = AIH.INSTANCE.getPlayerManager();
        AIHPlayer aihPlayer = aihPlayerManager.getAIHPlayer(player);

        if (aihPlayer == null) return;

        aihPlayer.setViolations(aihPlayer.getViolations() + 1);

        String playerName = player.getName();

        final TextComponent configAlertMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', alertMessage)
                .replaceAll("%prefix%", ChatColor.translateAlternateColorCodes('&', prefix))
                .replaceAll("%player%", playerName)
                .replaceAll("%message%", message)
                .replaceAll("%ping%", String.valueOf(PacketEvents.getAPI().getPlayerManager().getPing(player)))
                .replaceAll("%dev%", dev ? devSymbol : "")
                .replaceAll("%vl%", String.valueOf(aihPlayer.getViolations()))
                .replaceAll("%maxvl%", String.valueOf(maxViolations)));

        configAlertMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + playerName));
        configAlertMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6&lClick to teleport to &f" + playerName)).create()));

        for (Player staffPlayer : aihPlayerManager.getAihStaff()) {
            if (aihPlayerManager.getAIHPlayer(staffPlayer).isAlertsEnabled())
                staffPlayer.spigot().sendMessage(configAlertMessage);
        }

        if (!dev && !punishCommand.isEmpty() && aihPlayer.getViolations() > maxViolations) {
            if (!aihPlayer.isGotPunished()) {
                if (!broadcastMessage.isEmpty())
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage.replaceAll("%player%", playerName)));
                Bukkit.getScheduler().runTask(AIH.INSTANCE, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&', punishCommand.replaceAll("%player%", playerName))));
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

        if (!aihPlayer.isInventoryOpen() && PacketEvents.getAPI().getPlayerManager().getClientVersion(p).isOlderThan(ClientVersion.V_1_12)) {
            aihPlayer.setClicks(aihPlayer.getClicks() + 1);

            if (aihPlayer.getClicks() > 1) {
                fail(p, "doesn't have an open gui while clicking on items", true);

                if (cancelClick || safeCancelClick) {
                    e.setCancelled(true);
                    p.closeInventory();
                }
            }
        }

        if (p.isSprinting()) {
            fail(p, "is sprinting while clicking on items", false);

            if (cancelClick || safeCancelClick) {
                p.closeInventory();
                p.setSprinting(false);
            }
        }

        if (p.isSneaking()) {
            fail(p, "is sneaking while clicking on items", false);

            if (cancelClick || safeCancelClick) {
                p.closeInventory();
                p.setSneaking(false);
            }
        }

        boolean isInvalid = e.getSlotType().equals(InventoryType.SlotType.FUEL) || e.getSlotType().equals(InventoryType.SlotType.CRAFTING) || e.getSlotType().equals(InventoryType.SlotType.RESULT);
        if (isInvalid) return;

        long lastTimeOpenedInv = aihPlayer.getLastInventoryOpen();

        if (System.currentTimeMillis() - lastTimeOpenedInv < 20) {
            fail(p, "is clicking on items immediately after opening an gui", false);

            if (cancelClick || safeCancelClick) {
                p.closeInventory();
                e.setCancelled(true);
            }
        }

        long lastClick = System.currentTimeMillis() - aihPlayer.getLastInventoryClick();

        if (e.getCurrentItem() == null || e.getAction().equals(InventoryAction.NOTHING)) return;

        if (aihPlayer.getLastClickedItemStack() != null)
            if (aihPlayer.getLastClickedItemStack().getType().equals(e.getCurrentItem().getType())) return;

        if (!e.getAction().equals(InventoryAction.HOTBAR_SWAP) && !e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) && !e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
            if (lastClick < 1L) {
                aihPlayer.setBuffer(aihPlayer.getBuffer() + 1);
                if (aihPlayer.getBuffer() > (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) ? 2 : 1)) {
                    fail(p, "is clicking on items immediately in a gui", false);

                    if (cancelClick || safeCancelClick) {
                        e.setCancelled(true);
                    }
                }
            } else {
                aihPlayer.setBuffer(Math.max(aihPlayer.getBuffer() - (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) ? 0.3 : 0.1), 0));
            }
        }

        int slot = e.getSlot();
        int lastSlot = aihPlayer.getLastClickedSlot();
        double distance = distanceBetween(slot, lastSlot);
        double minClickDelay = e.getAction().equals(InventoryAction.HOTBAR_SWAP) || e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) ? 20 : distance * 23;

        if(e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR) || e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            if(distance > 2 && distance < 5) {
                minClickDelay = 10;
            } else if(distance >= 5) {
                minClickDelay = 20;
            }
        }

        int vl = lastClick < minClickDelay ? lastClick < minClickDelay / 2.0D ? 8 : 4 : 0;

        int suspiciousViolations = aihPlayer.getSuspiciousViolations();
        int totalVl = suspiciousViolations + vl;
        aihPlayer.setSuspiciousViolations(totalVl);

        if (vl < 2 && totalVl > 2) {
            int currentSuspiciousViolations = aihPlayer.getSuspiciousViolations();
            int deduction = ((lastClick * 2L) > minClickDelay) ? 2 : 1;
            currentSuspiciousViolations -= deduction;
            aihPlayer.setSuspiciousViolations(currentSuspiciousViolations);
        }

        if (totalVl > 4 && vl > 2 && lastClick > 0L) {
            fail(p, "is clicking on items too fast", false);

            if (cancelClick) {
                e.setCancelled(true);
            }
        }

        aihPlayer.setLastClickedSlot(slot);
        aihPlayer.setLastInventoryClick(System.currentTimeMillis());
        aihPlayer.setLastClickedItemStack(e.getCurrentItem());
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