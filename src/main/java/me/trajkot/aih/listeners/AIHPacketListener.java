package me.trajkot.aih.listeners;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.trajkot.aih.AIH;
import me.trajkot.aih.check.AIHCheck;
import me.trajkot.aih.player.AIHPlayer;
import org.bukkit.entity.Player;

public class AIHPacketListener implements com.github.retrooper.packetevents.event.PacketListener {

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        AIHPlayer aihPlayer = AIH.INSTANCE.getPlayerManager().getAIHPlayer(player);

        if (aihPlayer == null) return;

        if(event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            aihPlayer.handleFlyingPacket(wrapper);
        } else if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus statusWrapper = new WrapperPlayClientClientStatus(event);
            if (statusWrapper.getAction().equals(WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)) {
                aihPlayer.setInventoryOpen(true);
                aihPlayer.setClicks(0);
                aihPlayer.setLastInventoryOpen(System.currentTimeMillis());
            }
        } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            aihPlayer.setInventoryOpen(false);
            aihPlayer.setClicks(0);
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
        }
    }
}