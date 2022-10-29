package project4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author BoWen
 * @date 2022/10/28
 */
public class example4 {
    private int port = 9999;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer;

    /**
     * 初始化服务器 serverSocketChannel
     */
    public example4() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().setReuseAddress(true);
            serverSocketChannel.bind(new InetSocketAddress(port));
            selector = Selector.open();
            //非阻塞工作模式
            serverSocketChannel.configureBlocking(false);
            //注册就绪时间
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动成功！\n服务端口为：" + port);
            String line = "ddtm=20180923000227 DFME_ARRE[flid=1415324, ffid=CA-1402-20180922-A, fatt=2403, stat=ARR, ista=ARR, frlt=20180923000100]";
            buffer = ByteBuffer.wrap((line + "\r\n").getBytes());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动服务
     */
    public void service() {
        try {
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel =
                                (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        //非阻塞工作模式
                        socketChannel.configureBlocking(false);
                        ByteBuffer clientBuffer = buffer.duplicate();


                        SelectionKey writeableKey = socketChannel.register(selector, SelectionKey.OP_WRITE, clientBuffer);
                        System.out.println("客户端链接成功，" + socketChannel.getRemoteAddress()
                                + ":" + socketChannel.socket().getPort());


                    } else if (key.isWritable()) {
                        SocketChannel socketChannel =
                                (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            socketChannel.write(buffer);
                        }
                        buffer.flip();

                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new example4().service();
    }

}
