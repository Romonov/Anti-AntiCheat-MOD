package com.lalameow.anticheat.network;

import org.cat73.antianticheat.AntiAntiCheat;

import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class MPacketImpl implements MPacket {
    private String msg;

    public MPacketImpl(String msg) {
        this.msg = msg;
    }

    public void readBytes(ByteBuf paramByteBuf) {
        System.out.println(new String(paramByteBuf.array()));
    }

    public void writeBytes(ByteBuf paramByteBuf) {
        for (byte b : this.msg.getBytes()) {
            paramByteBuf.writeByte(b);
        }
    }

    public void sendClientMessage(String msg) {
        this.msg = msg;
        AntiAntiCheat.packetHandler.channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        AntiAntiCheat.packetHandler.channels.get(Side.CLIENT).writeOutbound(this);
    }
  
    public void sendServerMessage(String msg, EntityPlayer player) {
        this.msg = msg;
        AntiAntiCheat.packetHandler.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        AntiAntiCheat.packetHandler.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        AntiAntiCheat.packetHandler.channels.get(Side.SERVER).writeOutbound(this);
    }
}
