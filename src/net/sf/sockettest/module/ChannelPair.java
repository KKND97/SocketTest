package net.sf.sockettest.module;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ChannelPair {

    private SocketChannel targetChannel = null;
    private SocketChannel nativeChannel = null;

    public void register(Selector selector) throws ClosedChannelException {
        nativeChannel.register(selector, SelectionKey.OP_READ);
        targetChannel.register(selector, SelectionKey.OP_READ);
    }

    public void finish() {
        try {
            targetChannel.close();
        } catch (Exception e) {

        } //end try

        try {
            nativeChannel.close();
        } catch (Exception e) {

        } //end try
    }

    public void write(SocketChannel channel, ByteBuffer buffer) throws IOException {
        if (channel == targetChannel) {
            nativeChannel.write(buffer);
        } else {
            targetChannel.write(buffer);
        }
    }

    public SocketChannel getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(SocketChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public SocketChannel getNativeChannel() {
        return nativeChannel;
    }

    public void setNativeChannel(SocketChannel nativeChannel) {
        this.nativeChannel = nativeChannel;
    }

}
