package End;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TCPServer的抽象类
 *
 * @author BoWen
 * @version 1.1
 * @date 2022/11/17
 */
public abstract class AbstractTcpServer implements Runnable {

    private InetAddress inetAddress;

    protected String address;

    protected int port;

    protected Selector selector;

    protected final Logger logger = Logger.getLogger(AbstractTcpServer.class.getName());

    //提供指定线程数量的线程池
    ExecutorService service = Executors.newFixedThreadPool(10);

    public AbstractTcpServer(int port) {
        this.port = port;
    }

    public AbstractTcpServer(String address, int port) throws IOException {
        this.address = address;
        this.port = port;
        // 1.创建Selector对象，提供监听服务
        selector = Selector.open();
        // 2.创建serverSocketChannel，并将其绑定在指定端口，设成非阻塞工作模式
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(address, port));
        serverSocketChannel.configureBlocking(false);
        // 3.serverSocket向Selector注册“接受连接就绪事件”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        this.respond();
    }

    protected abstract void respond();

}
