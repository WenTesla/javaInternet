package End;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 继承UDPServer的类
 * 实现非阻塞发送UDP报文
 *
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

    public UdpServer(int bufferSize, InetAddress inetAddress,int port){
        super(bufferSize,inetAddress,port);
    }

    public UdpServer(int bufferSize, String address,int port){
        super(bufferSize,address,port);
    }
    public void setShutDown(){
        this.shutDown();

    }
    @Override
    protected void respond(DatagramSocket socket, DatagramPacket incoming) {
        //向线程池添加线程
        UDP_theadPool.execute(new FDSDataHandler(socket, incoming));
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

    private String lineInfo;
    private byte[] lineData;


    volatile static int connectingCount = 0;

    volatile static boolean UDP_currentThreadIsShutDown = false;

    FileReader fileReader;


    {
        try {
            fileReader = new FileReader("fdsdata.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    BufferedReader bufferedReader = new BufferedReader(fileReader);


    public FDSDataHandler(DatagramSocket socket, DatagramPacket incoming) {
        super();
        this.socket = socket;
        this.incoming = incoming;
    }

    @Override
    public void run() {
        System.out.println("客户端发送请求的地址:" + incoming.getSocketAddress());
        Server.UDP_tm.addRow(new String[]{String.valueOf(connectingCount++), incoming.getAddress().toString(), String.valueOf(incoming.getPort()),"normal"});
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
        int count = 0;
        while (count < 10000) {
            try {
                while ((lineInfo = bufferedReader.readLine()) != null) {
                    if (UDP_currentThreadIsShutDown){
                        System.out.println("服务器关闭此线程!");
                        lineData = "shutDown!".getBytes();
                        incoming.setData(lineData);
                        incoming.setLength(lineData.length);
                        socket.send(incoming);
                        Server.UDP_tm.addRow(new String[]{String.valueOf(connectingCount++), incoming.getAddress().toString(), String.valueOf(incoming.getPort()),"interrupted!"});
                        return;
                    }
                    lineData = lineInfo.getBytes();
                    incoming.setData(lineData);
                    incoming.setLength(lineData.length);
                    socket.send(incoming);

//                    System.out.println("发送客户端:" + incoming.getSocketAddress() + "第" + count + "行数据");
//                    System.out.println("数据如下" + lineInfo);
                    count++;
                    Thread.sleep(100);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //传输完毕，发送no data！
        String endData = "no data!";
        byte[] endDataBytes = endData.getBytes();
        incoming.setData(endDataBytes);
        incoming.setLength(endDataBytes.length);
        Server.UDP_tm.addRow(new String[]{String.valueOf(connectingCount++), incoming.getAddress().toString(), String.valueOf(incoming.getPort()),"finished!"});
        try {
            socket.send(incoming);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
