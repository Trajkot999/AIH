package me.trajkot.aih.check;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import me.trajkot.aih.AIH;
import me.trajkot.aih.config.AIHConfig;
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

        ClientVersion version = PacketEvents.getAPI().getPlayerManager().getClientVersion(p);

        if (!aihPlayer.isInventoryOpen() && version.isOlderThan(ClientVersion.V_1_12)) {
            aihPlayer.setClicks(aihPlayer.getClicks() + 1);

            if (aihPlayer.getClicks() > 1) {
                fail(p, "closed gui click", false);

                if (config.isCancelClick() || config.isSafeCancelClick()) {
                    e.setCancelled(true);
                    p.closeInventory();
                }
            }
        }

        if (p.isSprinting()) {
            fail(p, "sprint click", false);

            if (config.isCancelClick() || config.isSafeCancelClick()) {
                p.closeInventory();
                p.setSprinting(false);
            }
        }

        if (p.isSneaking()) {
            fail(p, "sneak click", false);

            if (config.isCancelClick() || config.isSafeCancelClick()) {
                p.closeInventory();
                p.setSneaking(false);
            }
        }

        boolean isInvalid = e.getSlotType().equals(InventoryType.SlotType.FUEL) || e.getSlotType().equals(InventoryType.SlotType.CRAFTING) || e.getSlotType().equals(InventoryType.SlotType.RESULT);
        if (isInvalid) return;

        long lastTimeOpenedInv = aihPlayer.getLastInventoryOpen();

        if (System.currentTimeMillis() - lastTimeOpenedInv < 50) {
            fail(p, "suspicious first click", true);

            if (config.isCancelClick() || config.isSafeCancelClick()) {
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
            if (aihPlayer.getBuffer() > 1) {
                fail(p, "instant click", false);

                if (config.isCancelClick() || config.isSafeCancelClick()) {
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

            aihPlayer.setLastClickedSlot(slot);
            aihPlayer.setLastInventoryClick(System.currentTimeMillis());
            aihPlayer.setLastClickedItemStack(e.getCurrentItem());
        }
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