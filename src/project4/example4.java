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

/**
 * @author BoWen
 * @version 1.1
 * @date 2022/10/28
 */
public class example4 {
    //端口号
    private static int port = 9999;
    //serverSocketChannel类
    private static ServerSocketChannel serverSocketChannel;
    //select类
    private static Selector selector;
    //字节缓冲流
    private static ByteBuffer buffer;
    //String 类
    private static String line = "连接服务器成功";
    //fileBuffer类
    private ByteBuffer fileBuffer;
    // 用RandomAccessFile以可读写模式加载文件
    private RandomAccessFile randomAccessFile;

    //初始化RandomAccessFile文件
    {
        try {
            randomAccessFile = new RandomAccessFile(new File("fdsdata2.txt"), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用构造方法初始化服务器
     */
    public example4() {
        try {
            //调用静态工厂方法来创建一个新的SocketChannel对象并初始化
            serverSocketChannel = ServerSocketChannel.open();
            //使得服务关掉重启时立马可使用该端口，而不是提示端口占用。
            serverSocketChannel.socket().setReuseAddress(true);
            //绑定监听端口
            serverSocketChannel.bind(new InetSocketAddress(port));
            //Selector.open()方法创建一个Selector对象
            selector = Selector.open();
            //非阻塞工作模式
            serverSocketChannel.configureBlocking(false);
            //注册就绪时间
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动成功！\n服务端口为：" + port);

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
                //遍历就绪事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    //从集合中删除这个键，从而不会处理两次
                    iterator.remove();
                    //有客户端来连接，接受客户的连接,处理连接请求就绪事件。
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel(); // 所有信息都找key要
                        SocketChannel socketChannel = serverSocketChannel.accept(); // 创建SocketChannel
                        // 配置新生的SocketChannel
                        socketChannel.configureBlocking(false);
                        SelectionKey writeableKey = socketChannel.register(selector, SelectionKey.OP_WRITE);
                        System.out.println("客户端链接成功，" + socketChannel.getRemoteAddress() + ":" + socketChannel.socket().getPort());

                        // 此步用于获取randomAccessFile中总的字节长度，便于下一步的ByteBuffer空间分配
                        fileBuffer = ByteBuffer.allocate(531832400); // 重新分配一个大小与randomAccessFile相同的ByteBuffer，单位是字节
                        randomAccessFile.seek(0); // 设置文件指针到文件的开头，实现回头读

                        // 用缓冲器读取数据
                        byte[] temp = new byte[8192];
                        int n = -1; // 文件为空或已读完
                        while ((n = randomAccessFile.read(temp)) != -1) {
                            fileBuffer.put(temp, 0, n); // 根据每次实际读出的数据长度，向fileBuffer追加该长度的数据，单位字节，避免最后一个缓冲区有多余空间产生浪费
                        }
                        fileBuffer.flip(); // 把极限设成位置，把位置设成零

                        writeableKey.attach(fileBuffer); // 将fileBuffer作为附件附加到key中,方便send方法调用

                    }
                    //向输出流写数据,处理写就绪事件。
                    else if (key.isWritable()) {
                        send(key);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送数据
     *
     * @param key
     * @throws IOException
     */
    public void send(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer bufferToSend = (ByteBuffer) key.attachment(); // 取出附件

        if (bufferToSend.hasRemaining()) {
            socketChannel.write(bufferToSend);
        } else {
            // 发送结束
            byte[] endMessage = "\r\nno data!\r\n".getBytes();
            ByteBuffer endBuffer = ByteBuffer.wrap(endMessage);
            socketChannel.write(endBuffer);

            System.out.println(socketChannel.getRemoteAddress() + "的数据已发送完毕\n");

            socketChannel.close(); // 关闭连接
            key.cancel(); // 取消key的相关channel和key的selector
        }
    }

    /**
     * 主方法
     *
     * @param args
     */
    public static void main(String[] args) {
        new example4().service();
    }

}
