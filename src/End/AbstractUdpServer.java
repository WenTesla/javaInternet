package End;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDPServer的抽象类
 * @author BoWen
 * @version 1.0
 * @date 2022/10/31
 */

public abstract class AbstractUdpServer implements Runnable  {

    private InetAddress inetAddress;
    //字节长度
    private int bufferSize;
    //端口
    private final int port;
    //日志
    private final Logger logger=Logger.getLogger(AbstractUdpServer.class.getCanonicalName());
    //是否关闭
    private volatile boolean isShutDown=false;

    //提供指定最大线程数量的线程池
    public static ExecutorService UDP_theadPool = Executors.newFixedThreadPool(10);

    public AbstractUdpServer(int bufferSize, int port) {
        this.bufferSize=bufferSize;
        this.port=port;
    }

    public AbstractUdpServer(int port){
        this(8192,port);
    }


    public AbstractUdpServer(int bufferSize, InetAddress inetAddress, int port){
        this.bufferSize=bufferSize;
        this.inetAddress=inetAddress;
        this.port=port;
    }
    public AbstractUdpServer(int bufferSize,String address,int port){
        this.bufferSize=bufferSize;
        this.port=port;
        try {
            this.inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port,inetAddress)){
            logger.log(Level.INFO,"UDP服务器启动成功！\n"+"地址: "+inetAddress.getHostName()+" 端口号: "+port);
            while (true) {
                //先检查变量
                if (isShutDown) {
                    return;
                }
                //初始化DatagramPacket
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                socket.receive(incoming);
                this.respond(socket,incoming);
            }
        } catch (SocketException e) {
            logger.log(Level.SEVERE,"服务器启动端口:"+port+"失败");
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.WARNING,e.getMessage(),e);
            e.printStackTrace();
        }
    }
    //抽象类一定定义抽象方法
    protected abstract void respond(DatagramSocket socket, DatagramPacket incoming);

    protected void shutDown(){
        isShutDown=true;
    }

}
