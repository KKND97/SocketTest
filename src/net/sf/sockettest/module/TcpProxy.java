package net.sf.sockettest.module;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.sockettest.swing.SocketTestProxy;

public class TcpProxy extends Thread {
    private String targetIp = "";
    private int targetPort = 0;
    private boolean targetSsl = false;

    private String nativeIp = "";
    private int nativePort = 0;
    private boolean nativeSsl = false;

    private List<ChannelPair> channelPairList = new LinkedList<ChannelPair>();

    /**
     * 任务锁
     */
    private ReentrantLock rennlock = new ReentrantLock();

    private ServerSocket serverSocket = null;

    private Selector selector = null;

    private boolean working = false;

    public TcpProxy(SocketTestProxy parent) throws IOException {
        selector = Selector.open();
    }

    public void run() {

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        while (!this.isInterrupted()) {
            if (!working) {
                try {
                    selector.select(1);
                } catch (Exception e) {
                    this.interrupt();
                }
                continue;
            } //end if

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = (SelectionKey) keyIterator.next();
                if (key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    ChannelPair pair = new ChannelPair();
                    try {
                        SocketChannel nativeChannel;
                        nativeChannel = server.accept();
                        pair.setNativeChannel(nativeChannel);
                        channelPairList.add(pair);
                        buildTargetChannel(pair);
                    } catch (IOException e) {
                        pair.finish();
                    }

                } else if (key.isConnectable()) {
                    ChannelPair pair = (ChannelPair) key.attachment();
                    try {
                        pair.setTargetChannel((SocketChannel) key.channel());
                        pair.register(selector);
                    } catch (Exception e) {
                        pair.finish();
                    } //end try
                } else if (key.isReadable()) {
                    ChannelPair pair = (ChannelPair) key.attachment();
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.read(buffer);
                        buffer.flip();
                        if (buffer.position() == 0) {
                            pair.finish();
                        } else {
                            pair.write(channel, buffer);
                        }
                    } catch (IOException e) {
                        pair.finish();
                    }
                } else if (key.isWritable()) {
                    // a channel is ready for writing
                } else {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof ChannelPair) {
                        ChannelPair pair = (ChannelPair) attachment;
                        pair.finish();
                    }
                } //end if
                keyIterator.remove();
            } //end while
        } //end while
    }

    public void begin() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }

        buildNativeChannel();

        this.working = true;
    }

    private void buildNativeChannel() throws IOException, ClosedChannelException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();// 打开一个未绑定的serversocketchannel
        serverSocket = serverChannel.socket();// 得到一个ServerSocket去和它绑定 
        serverSocket.bind(new InetSocketAddress(this.nativeIp, this.nativePort));//设置server channel将会监听的端口
        serverChannel.configureBlocking(false);//设置非阻塞模式
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);//将ServerSocketChannel注册到Selector
    }

    private void buildTargetChannel(ChannelPair pair) throws UnknownHostException, IOException {

        //获取socket通道
        SocketChannel target_channel = SocketChannel.open();
        target_channel.configureBlocking(false);
        //客户端连接服务器，需要调用channel.finishConnect();才能实际完成连接。
        target_channel.connect(new InetSocketAddress(targetIp, targetPort));

        //为该通道注册SelectionKey.OP_CONNECT事件
        SelectionKey register = target_channel.register(selector, SelectionKey.OP_CONNECT);

        register.attach(pair);

    }

    public void finish() throws IOException {
        rennlock.lock();
        try {
            for (ChannelPair index : channelPairList) {
                index.finish();
            } //end for channelPairList
            channelPairList.clear();
            serverSocket.close();
        } finally {
            rennlock.unlock();
        } //end try
        this.working = false;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String target_ip) {
        this.targetIp = target_ip;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int target_port) {
        this.targetPort = target_port;
    }

    public boolean isTargetSsl() {
        return targetSsl;
    }

    public void setTargetSsl(boolean target_ssl) {
        this.targetSsl = target_ssl;
    }

    public String getNativeIp() {
        return nativeIp;
    }

    public void setNativeIp(String native_ip) {
        this.nativeIp = native_ip;
    }

    public int getNativePort() {
        return nativePort;
    }

    public void setNativePort(int native_port) {
        this.nativePort = native_port;
    }

    public boolean isNativeSsl() {
        return nativeSsl;
    }

    public void setNativeSsl(boolean native_ssl) {
        this.nativeSsl = native_ssl;
    }

}
