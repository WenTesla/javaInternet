package End;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public abstract class AbstractTcpServer implements Runnable{

    private InetAddress inetAddress;

    protected String address;

    protected int port;

    protected Selector selector;

    protected final Logger logger = Logger.getLogger(AbstractTcpServer.class.getName());


    public AbstractTcpServer(int port){
        this.port = port;
    }

    public AbstractTcpServer(String address,int port) throws IOException {
        this.address=address;
        this.port=port;
        // 1.创建Selector对象，提供监听服务
        selector = Selector.open();
        // 2.创建serverSocketChannel，并将其绑定在指定端口，设成非阻塞工作模式
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(address,port));
        serverSocketChannel.configureBlocking(false);
        // 3.serverSocket向Selector注册“接受连接就绪事件”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.log(Level.INFO, "TCP服务器启动成功！\n" +"地址:"+address+"端口号:" + port);
    }



    @Override
    public void run() {
        this.respond();

    }

    protected abstract void respond();



}
