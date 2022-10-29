package project4;


import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author BoWen
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

    private static FileChannel fileChannel;

    private static long totalLength;
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

            //一行行读取文件
//            File file = new File("fdsdata.txt");
//            Scanner scanner = new Scanner(file);
//            while (scanner.hasNextLine()) {
//                line= scanner.nextLine();
//                buffer = ByteBuffer.wrap((line + "\r\n").getBytes());
//
////                System.out.println(line);
//            }
            FileInputStream fileInputStream = new FileInputStream("fdsdata.txt");
            fileChannel = fileInputStream.getChannel();
            totalLength=fileChannel.size();
            buffer = ByteBuffer.allocate(1024 * 1024);
            fileChannel.read(buffer);
//            FileReader fileReader = new FileReader("fdsdata.txt");
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            while ((line= bufferedReader.readLine()) != null){
//                System.out.println(line);
//            }
//            line = "ddtm=20180923000227 DFME_ARRE[flid=1415324, ffid=CA-1402-20180922-A, fatt=2403, stat=ARR, ista=ARR, frlt=20180923000100]";
//            buffer = ByteBuffer.wrap((line + "\r\n").getBytes());
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
                    //有客户端来连接，接受客户的连接
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        //非阻塞工作模式
                        socketChannel.configureBlocking(false);
                        ByteBuffer clientBuffer = buffer.duplicate();
                        SelectionKey writeableKey = socketChannel.register(selector, SelectionKey.OP_WRITE, clientBuffer);
                        System.out.println("客户端链接成功，" + socketChannel.getRemoteAddress() + ":" + socketChannel.socket().getPort());
                    }
                    //向输出流写数据
                    else if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
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
