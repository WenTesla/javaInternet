package project5;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDPServer的抽象类
 * @author BoWen
 * @version 1.0
 * @date 2022/10/31
 */

public abstract class AbstractUdpServer implements Runnable {
    //字节
    private int bufferSize;
    //端口
    private final int port;
    //日志
    private final Logger logger=Logger.getLogger(AbstractUdpServer.class.getCanonicalName());
    //是否关闭
    private volatile boolean isShutDown=false;

    public AbstractUdpServer(int bufferSize, int port) {
        this.bufferSize=bufferSize;
        this.port=port;

    }

    public AbstractUdpServer(int port){
        this(8192,port);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port)){
            logger.log(Level.INFO,"服务器启动成功！");
            while (true) {
                //先检查变量
                if (isShutDown) {
                    return;
                }
                //
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

    protected void shuDown(){

        isShutDown=true;
    }

}
