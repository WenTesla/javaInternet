package project4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class FlightServer {
    private int port = 9999;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer;


    //	与类同名构造方法
    public FlightServer() {
        try {
//			实例化serverSocketChannel
            serverSocketChannel = ServerSocketChannel.open();
//			我希望每次释放端口，端口都能马上被其他程序调用
            serverSocketChannel.socket().setReuseAddress(true);
//			绑定端口
            serverSocketChannel.bind(new InetSocketAddress(port));
//			选择器实例化 
            selector = Selector.open();
//			把serverSocketChannel置非阻塞工作模式
            serverSocketChannel.configureBlocking(false);
//			注册就绪事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动成功，端口为：" + port);
//			文件5.07MB，定义缓冲区5.08*1024*1024B
            buffer = ByteBuffer.allocate(5318324);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void service() throws FileNotFoundException {
        RandomAccessFile myFile = new RandomAccessFile(new File("fdsdata2.txt"), "r");
        try {
//			selector.select()是一个阻塞方法，！>0就不会返回
            while (selector.select() > 0) {
//				把发生的就绪事件取出来，注：就绪事件是一个集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
//				定义一个迭代器来帮我们遍历,迭代器等于selectionKeys关联的迭代
                Iterator it = selectionKeys.iterator();
                while (it.hasNext()) {
//					当it还有元素的时候就取出这个元素,两边类型不一致，就强制转换指明我取出来的是就绪事件
                    SelectionKey key = (SelectionKey) it.next();
//					取出it.next()后要删掉，不然一直满足it.hasNext()一直遍历下去
                    it.remove();
//					取出就绪事件后要判断就绪事件类型
//					isAcceptable : Tests whether this key's channel is ready to accept a new socketconnection. 
//					即判断key是否是连接就绪事件
                    if (key.isAcceptable()) {
//						是则在key的通道上接受连接请求，所以先获得通道对象。
//						key.channel()可以获得不同的类型，我们指定（强转）为ServerSocketChannel型
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
//						让服务器和客户端建立连接，通过通道的接受连接方法
                        SocketChannel socketChannel = serverSocketChannel.accept();
//						让SocketChannel工作在非阻塞线程
                        socketChannel.configureBlocking(false);
//						绑定 写就绪事件
                        SelectionKey writableKey = socketChannel.register(selector, SelectionKey.OP_WRITE);
                        System.out.println("客户端连接成功，客户端IP：" + socketChannel.getRemoteAddress()
                                + "，端口：" + socketChannel.socket().getPort());
//						指针置0
                        myFile.seek(0);
//						定义大小决定速率，即单次吞吐量
                        byte[] temp = new byte[8192];
                        int n = -1;
//						myFile.read(temp)等于-1时是到末尾了
                        while ((n = myFile.read(temp)) != -1) {
                            buffer.put(temp, 0, n);
                        }
                        buffer.flip();
                        writableKey.attach(buffer);
                    } else if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
//						key可以关联的数据有很多类型，我明确指明此处是ByteBuffer型
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
//						当buffer还有没发的数据，就继续下去
//                        while (buffer.hasRemaining()) {
////							buffer一次存一行数据，我们循环，一次发一个buffer(一行)
//                            socketChannel.write(buffer);
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
                        byte[] endMessage = "shutDown!".getBytes();
                        ByteBuffer endBuffer = ByteBuffer.wrap(endMessage);
                        socketChannel.write(endBuffer);
                        System.out.println(socketChannel.getRemoteAddress() + "的数据已发送完毕\n");
                        // 关闭连接
                        socketChannel.close();
                        // 取消key的相关channel和key的selector
                        key.cancel();
                        buffer.flip();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 客户端主动断开
            if (e.getMessage().contains("Connection reset by peer")) {
                System.out.println("客户端已主动断开连接\n");
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
//		为什么刚刚用实验三示例程序接收数据没有显示？因为服务端没加每行结束标记，导致客户端认为还没一行发完，就等待，就阻塞
//		改进：去wrap()那里加入“\r\n”
        new FlightServer().service();
    }
}
