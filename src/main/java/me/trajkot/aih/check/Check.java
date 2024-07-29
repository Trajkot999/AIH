package me.trajkot.aih.check;

import com.github.retrooper.packetevents.PacketEvents;
import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIH_Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import java.util.List;

public class Check implements Listener {

    private final int maxViolations;
    private final String punishCommand;
    private final String broadcastMessage;
    private final boolean cancelClick;
    private final boolean safeCancelClick;

    public Check() {
        cancelClick = AIH.getAIHConfig().getConfig().getBoolean("cancel-click");
        safeCancelClick = AIH.getAIHConfig().getConfig().getBoolean("safe-cancel-click");
        punishCommand = AIH.getAIHConfig().getConfig().getString("punish-command");
        broadcastMessage = AIH.getAIHConfig().getConfig().getString("broadcast-message");
        maxViolations = AIH.getAIHConfig().getConfig().getInt("max-violations");
    }

    private void fail(Player player, String message, boolean experimental) {
        AIH_Player aihPlayer = AIH.getPlayerManager().getAIHPlayer(player);

        aihPlayer.violations++;

        for(Player staffPlayer : AIH.getPlayerManager().getAihStaff()) {
            staffPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6AIH &8> &e" + player.getName() + " &7" + message + " &6x" + aihPlayer.violations + (experimental ? "&cDEV" : "")));
        }

        if(!experimental && !punishCommand.isEmpty() && aihPlayer.violations > maxViolations) {
            if(!AIH.getPlayerManager().getAIHPlayer(player).gotPunished) {
                if(!broadcastMessage.isEmpty()) Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage.replaceAll("%player%", AIH.getPlayerManager().getAIHPlayer(player).player.getName())));
                Bukkit.getScheduler().runTask(AIH.INSTANCE, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&', punishCommand.replaceAll("%player%", AIH.getPlayerManager().getAIHPlayer(player).player.getName()))));
                AIH.getPlayerManager().getAIHPlayer(player).gotPunished = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        AIH.getPlayerManager().getAIHPlayer(p).isInventoryOpen = false;
        AIH.getPlayerManager().getAIHPlayer(p).lastInventoryClose = System.currentTimeMillis();
        AIH.getPlayerManager().getAIHPlayer(p).lastItemClickDiffs.clear();
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        AIH.getPlayerManager().getAIHPlayer(p).lastInventoryOpen = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        AIH_Player aihPlayer = AIH.getPlayerManager().getAIHPlayer(p);

        int ping = PacketEvents.getAPI().getPlayerManager().getPing(p);

        if (p.getGameMode().equals(GameMode.CREATIVE)) return;

        if (aihPlayer.isInventoryOpen) {
            fail(p, "nie ma otwartego gui gdy klika &8ping: " + ping + "ms", true);
            if(cancelClick || safeCancelClick) {
                e.setCancelled(true);
                p.closeInventory();
            }
        }

        if (p.isSprinting()) {
            fail(p, "sprintuje gdy klika w gui &8ping: " + ping + "ms", false);
            if(cancelClick || safeCancelClick) {
                p.closeInventory();
            }
        }

        if (p.isSneaking()) {
            fail(p, "kuca gdy klika w gui &8ping: " + ping + "ms", false);
            if(cancelClick || safeCancelClick) {
                p.closeInventory();
            }
        }

        boolean isInvalid = e.getSlotType().equals(InventoryType.SlotType.FUEL) || e.getSlotType().equals(InventoryType.SlotType.CRAFTING) || e.getSlotType().equals(InventoryType.SlotType.RESULT);
        if(isInvalid) return;

        if (System.currentTimeMillis() - aihPlayer.lastTimeSuspiciousForChestStealer < 200L) {
            if(cancelClick) {
                e.setCancelled(true);
            }
        }

        long lastTimeOpenedInv = aihPlayer.lastInventoryOpen;

        if (System.currentTimeMillis() - lastTimeOpenedInv < 20) {
            fail(p, "kliknal od razu po otwarciu gui &8otwarl: " + (int) (System.currentTimeMillis() - lastTimeOpenedInv) + "ms temu, ping: " + ping + "ms", false);
            if(cancelClick || safeCancelClick) {
                p.closeInventory();
                e.setCancelled(true);
            }
        }

        long lastClick = System.currentTimeMillis() - aihPlayer.lastInventoryClick;

        if(e.getCurrentItem() != null && !e.getAction().equals(InventoryAction.NOTHING) && !e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            if (lastClick < 1L) {
                if(!e.getAction().equals(InventoryAction.HOTBAR_SWAP) && !e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                    fail(p, "natychmiastowo klika w gui &8instant, ping: " + ping + "ms", false);

                    if(cancelClick || safeCancelClick) {
                        e.setCancelled(true);
                    }
                }
            }

            aihPlayer.lastItemClickDiffs.add(lastClick);
        }

        if (aihPlayer.lastItemClickDiffs.size() >= 5) {
            double balance = getSquaredBalanceFromLong(aihPlayer.lastItemClickDiffs);

            int minBalance = 75;
            if(e.getSlot() == aihPlayer.lastClickedSlot && e.getAction().equals(InventoryAction.HOTBAR_SWAP) || e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                minBalance = 8;
            } else if(e.getSlot() != aihPlayer.lastClickedSlot && e.getAction().equals(InventoryAction.HOTBAR_SWAP) || e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                minBalance = 13;
            }

            if (balance < minBalance) {
                fail(p, "ma zbyt podobny czas miedzy kliknieciami &8balans: " + (int) balance + ", ping: " + ping + "ms", false);
                aihPlayer.lastTimeSuspiciousForChestStealer = System.currentTimeMillis();
                if(cancelClick) {
                    e.setCancelled(true);
                }
            }
            aihPlayer.lastItemClickDiffs.remove(0);
        }

        int slot = e.getSlot();

        if(e.getCurrentItem() != null && !e.getAction().equals(InventoryAction.NOTHING)) {
            int lastSlot = aihPlayer.lastClickedSlot;
            double distance = distanceBetween(slot, lastSlot);
            double minClickDelay = e.getAction().equals(InventoryAction.HOTBAR_SWAP) || e.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) ? 23.5D : distance * 30.0D;

            int vl = lastClick < minClickDelay ? lastClick < minClickDelay / 2.0D ? 8 : 4 : 0;

            int totalVl = aihPlayer.itemStealerVL += vl;

            if (vl < 2 && totalVl > 2) {
                aihPlayer.itemStealerVL -= ((lastClick * 2L) > minClickDelay) ? 2 : 1;
            }

            double speedAttr = lastClick / distance;

            if (totalVl > 4 && vl > 2 && lastClick > 0L && !e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                fail(p, "zbyt szybko klika w gui &8" + (int) speedAttr + "ms/slot, ping: " + ping + "ms", false);

                if(cancelClick) {
                    e.setCancelled(true);
                }
            }
        }

        aihPlayer.lastClickedSlot = slot;
        aihPlayer.lastInventoryClick = System.currentTimeMillis();
    }

    private int[] translatePosition(int slot) {
        int row = slot / 9 + 1;
        int rowPosition = slot - (row - 1) * 9;
        return new int[] { row, rowPosition };
    }

    private double distanceBetween(int slot1, int slot2) {
        int[] slot1XZ = translatePosition(slot1);
        int[] slot2XZ = translatePosition(slot2);
        return Math.sqrt(((slot1XZ[0] - slot2XZ[0]) * (slot1XZ[0] - slot2XZ[0]) + (slot1XZ[1] - slot2XZ[1]) * (slot1XZ[1] - slot2XZ[1])));
    }

    private double getSquaredBalanceFromLong(List<Long> arrayList) {
        return arrayList.stream().mapToDouble(i -> i).sum() / arrayList.size();
    }
}