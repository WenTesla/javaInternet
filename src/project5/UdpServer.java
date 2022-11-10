package project5;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 继承UDPServer的类
 * 实现非阻塞发送UDP报文
 * @author BoWen
 * @version 2.0
 * @date 2022/10/31
 */
public class UdpServer extends AbstractUdpServer {
    /**
     * 默认端口
     */
    private final static int DEFAULT_PORT = 9999;

    public UdpServer(int bufferSize, int port) {
        super(bufferSize, port);
    }

    public UdpServer() {
        super(DEFAULT_PORT);
    }

    @Override
    protected void respond(DatagramSocket socket, DatagramPacket incoming) {
        //为每一个开启线程
        new Thread(new FDSDataHandler(socket,incoming)).start();

    }

    public static void main(String[] args) {
        new Thread(new UdpServer()).start();
    }
}

/**
 * 定义线程类
 *
 * @author BoWen
 * @date 2022/11/10
 */
class FDSDataHandler implements Runnable {
    private DatagramSocket socket;
    private DatagramPacket incoming;

    public FDSDataHandler(DatagramSocket socket, DatagramPacket incoming) {
        super();
        this.socket = socket;
        this.incoming = incoming;
    }

    @Override
    public void run() {
        System.out.println("客户端发送请求的地址：" + incoming.getSocketAddress());
        //给客户端发送数据行数信息
        String startData = "待发送的行数为:10000";
        byte[] startInfoBytes = startData.getBytes();
        incoming.setData(startInfoBytes);
        incoming.setLength(startInfoBytes.length);
        try {
            socket.send(incoming);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //给客户端发送航班数据
        String lineInfo = "ddtm=20180923000231 DFME_STLS[flid=1416561, ffid=CA-1529-20180923-D, fatt=2403, stnd=STND[stno=1, code=205, estr=20180923000700, eend=20180923071500, rstr=20180923000700, rend=null, btsc=null]]\n";

        byte[] lineData = lineInfo.getBytes();
        incoming.setData(lineData);
        incoming.setLength(lineData.length);
        int count = 0;
        while (count < 10000) {
            try {
                socket.send(incoming);
                System.out.println("发生客户端:"+incoming.getSocketAddress()+"第"+count+"行数据");
                count++;
                Thread.sleep(1000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
