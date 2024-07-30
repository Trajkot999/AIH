package me.trajkot.aih.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import me.trajkot.aih.AIH;
import me.trajkot.aih.player.AIHPlayer;
import org.bukkit.entity.Player;

public class PacketEventsListener implements PacketListener {

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(player);

        if (aihPlayer == null) return;

        if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus statusWrapper = new WrapperPlayClientClientStatus(event);
            if (statusWrapper.getAction().equals(WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)) {
                aihPlayer.setInventoryOpen(true);
                aihPlayer.setClicks(0);
                aihPlayer.setLastInventoryOpen(System.currentTimeMillis());
            }
        } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            aihPlayer.setInventoryOpen(false);
            aihPlayer.setClicks(0);
            aihPlayer.getLastClicks().clear();
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(player);

        if (aihPlayer == null) return;

        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            aihPlayer.setInventoryOpen(true);
            aihPlayer.setClicks(0);
            aihPlayer.setLastInventoryOpen(System.currentTimeMillis());
        } else if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
            aihPlayer.setInventoryOpen(false);
            aihPlayer.setClicks(0);
            aihPlayer.getLastClicks().clear();
        }
    }
}