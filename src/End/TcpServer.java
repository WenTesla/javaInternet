package End;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;

import static End.Server.TCP_tm;

public class TcpServer extends AbstractTcpServer {
    private static volatile int connectingCount = 0;
    private ByteBuffer fileBuffer;
    // 用RandomAccessFile以可读写模式加载flightdata.txt
    private RandomAccessFile randomAccessFile = new RandomAccessFile(new File("fdsdata2.txt"), "r");

    static volatile boolean TCP_currentThreadIsShutDown = false;

    public TcpServer(String address, String port) throws Exception {
        super(address, Integer.parseInt(port));
    }

    public static void main(String[] args) throws Exception {
//        new TcpServer(args[0],args[1] ).Service();
    }

    @Override
    protected void respond() {
        logger.log(Level.INFO, "TCP服务器启动成功！\n" + "地址:" + address + " 端口号:" + port);
        while (true) {
            SelectionKey key = null;

            try {
                selector.select(); // 查看当前是否发生了某些就绪事件，是则返回，否则等待
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); // 从selectedKeys()中取出迭代器遍历集合中的数据
                while (iterator.hasNext()) {
                    key = (SelectionKey) iterator.next();
                    iterator.remove(); // 从原始集合中删掉

                    // 接受连接
                    if (key.isAcceptable()) { // key 就绪事件抓手
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel(); // 所有信息都找key要
                        SocketChannel socketChannel = serverSocketChannel.accept(); // 创建SocketChannel

                        // 配置新生的SocketChannel
                        socketChannel.configureBlocking(false);
                        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE);

                        System.out.println(socketChannel.getRemoteAddress() + "已连接"); // 客户端地址

                        // 此步用于获取randomAccessFile中总的字节长度，便于下一步的ByteBuffer空间分配
                        //5282288
                        fileBuffer = ByteBuffer.allocate(5318324); // 重新分配一个大小与randomAccessFile相同的ByteBuffer，单位是字节
                        randomAccessFile.seek(0); // 设置文件指针到文件的开头，实现回头读

                        // 用缓冲器读取数据
                        byte[] temp = new byte[8192];
                        int n = -1; // 文件为空或已读完
                        while ((n = randomAccessFile.read(temp)) != -1) {
                            fileBuffer.put(temp, 0, n); // 根据每次实际读出的数据长度，向fileBuffer追加该长度的数据，单位字节，避免最后一个缓冲区有多余空间产生浪费
                        }
                        fileBuffer.flip(); // 把极限设成位置，把位置设成零
                        selectionKey.attach(fileBuffer); // 将fileBuffer作为附件附加到key中
                        TCP_tm.addRow(new String[]{String.valueOf(connectingCount++), address, String.valueOf(port), "normal"});
                    }
                    if (key.isReadable()) {

                    }
                    if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer bufferToSend = (ByteBuffer) key.attachment(); // 取出附件
                        if (TCP_currentThreadIsShutDown) {
                            // 发送结束
                            byte[] endMessage = "shutDown!".getBytes();
                            ByteBuffer endBuffer = ByteBuffer.wrap(endMessage);
                            socketChannel.write(endBuffer);
                            TCP_tm.addRow(new String[]{String.valueOf(connectingCount++), socketChannel.getRemoteAddress().toString(), String.valueOf(port), "interrupted!"});
                            socketChannel.close(); // 关闭连接
                            key.cancel(); // 取消key的相关channel和key的selector
                            return;
                        }
                        if (bufferToSend.hasRemaining()) {
                            socketChannel.write(bufferToSend);
                        }
                        // 发送结束
                        byte[] endMessage = "\r\nno data!\r\n".getBytes();
                        ByteBuffer endBuffer = ByteBuffer.wrap(endMessage);
                        socketChannel.write(endBuffer);
                        System.out.println(socketChannel.getRemoteAddress() + "的数据已发送完毕\n");
                        ///127.0.0.1:37769
                        TCP_tm.addRow(new String[]{String.valueOf(connectingCount++), socketChannel.getRemoteAddress().toString(), String.valueOf(port), "finish!"});
                        socketChannel.close(); // 关闭连接
                        key.cancel(); // 取消key的相关channel和key的selector

                    }
                }
            } catch (IOException e) {
                try {
                    key.channel().close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                key.cancel();
                // 客户端主动断开
                if (e.getMessage().contains("你的主机中的软件中止了一个已建立的连接。")) {
                    System.out.println("客户端已主动断开连接\n");
                }
                // e.printStackTrace();
            }
        }
    }
}
