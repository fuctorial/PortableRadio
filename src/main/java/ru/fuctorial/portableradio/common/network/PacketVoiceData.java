package ru.fuctorial.portableradio.common.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PacketVoiceData implements IMessage {
    private byte[] data;

    public PacketVoiceData() {}

    public PacketVoiceData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        this.data = new byte[length];
        buf.readBytes(this.data);
    }
}