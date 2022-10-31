package project5;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 继承UDPServer的类
 * @author BoWen
 * @version 1.0
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
        System.out.println("客户端发送请求的地址："+incoming.getSocketAddress());
        //给客户端发送数据行数信息
        String startData="待发送的行数为:10000";
        byte[] startInfoBytes = startData.getBytes();
        incoming.setData(startInfoBytes);
        incoming.setLength(startInfoBytes.length);
        try {
            socket.send(incoming);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //给客户端发送航班数据
        String lineInfo = "";
        byte[] lineData = lineInfo.getBytes();
        incoming.setData(lineData);
        incoming.setLength(lineData.length);
        int count = 0;
        while (count<10000){
            try {
                socket.send(incoming);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new UdpServer()).start();
    }
}
