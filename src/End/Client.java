package End;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static End.Client.ReceiveAndProcessData.readFlag;


/**
 * @author BoWen
 * @version 1.2
 * @Data 2022/11/15
 */
public class Client extends JFrame {


    /**
     * 自定义各种航班数据
     */

    private static String flid = null;  // 即数据文件中每行数据的flid
    private static String flightNumber = null; // 航班号，如CA1402
    private static String sfawOne = null; // 共享航班公司1
    private static String sfnoOne = null; // 共享航班号1
    private static String sfawTwo = null; // 共享航班公司2
    private static String sfnoTwo = null; // 共享航班号2
    private static String sfawThree = null; // 共享航班公司3
    private static String sfnoThree = null; // 共享航班号3
    private static String aimStation = null; // 目的站
    private static String transferStation = null; // 中转站
    private static String destinationStation = null; //终点站
    private static String planToFlyTime = null; // 预计起飞时间
    private static String changeToFlyTime = null; // 变更起飞时间
    private static String actualFlyTime = null;//实际起飞时间
    private static String gate = null; // 登机口
    private static String state = null; // 登机状态


    /**
     * GUI
     */
    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tm;
    private int count = 0;
    private ReceiveAndProcessData receiveAndProcessData;


    //地址
    private JTextField addressText;
    //端口
    private JTextField portText;

    /**
     * 有关网络协议的配置
     */

    private String procotol;

    private static String TCP_Address = "localHost";

    private static String TCP_Port = "9999";

    private static String UDP_Address;

    private static String UDP_Port = "9999";


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Client frame = new Client();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */

    public Client() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 591, 640);
        setTitle("航班显示系统客户端");
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);

        JComboBox comboBox = new JComboBox();
        comboBox.addItem("TCP");
        comboBox.addItem("UDP");
        panel.add(comboBox);

        JTextArea txtAddress = new JTextArea();
        txtAddress.setText("address");
        txtAddress.setEditable(false);
        panel.add(txtAddress);

        addressText = new JTextField();
        panel.add(addressText);
        addressText.setColumns(10);

        JTextArea txtPort = new JTextArea();
        txtPort.setText("port");
        txtPort.setEditable(false);
        panel.add(txtPort);

        portText = new JTextField();
        panel.add(portText);
        portText.setColumns(10);


        JButton startButton = new JButton("Start");
        /**
         * 服务器开启
         */
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //处理tcp或者udp
                procotol = (String) comboBox.getSelectedItem();
                if ("TCP".equals(procotol)) {
                    TCP_Address = addressText.getText();
                    if (!"".equals(portText.getText()))
                        TCP_Port = portText.getText();
                } else {
                    UDP_Address = addressText.getText();
                    //非空则设置UDP端口为默认端口
                    if (!"".equals(portText.getText()))
                        UDP_Port = portText.getText();
                }
                /**
                 * 三种情况
                 * 1.第一次处理
                 * 2.被暂停
                 * 3.客户端超时
                 */
                if (receiveAndProcessData == null) {
                    receiveAndProcessData = new ReceiveAndProcessData(procotol, TCP_Address, TCP_Port, UDP_Address, UDP_Port);
                    receiveAndProcessData.execute();
//                    btnNewButton.setEnabled(false);
                } else if (receiveAndProcessData.isCancelled()) {
//                    btnNewButton.setEnabled(true);
                    receiveAndProcessData.cancel(true);
                    receiveAndProcessData = null;
                } else {
                    receiveAndProcessData = new ReceiveAndProcessData(procotol, TCP_Address, TCP_Port, UDP_Address, UDP_Port);
                    receiveAndProcessData.execute();
                }

            }
        });
        panel.add(startButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (receiveAndProcessData != null && !receiveAndProcessData.isCancelled()) {
                    receiveAndProcessData.cancel(true);
                    pauseButton.setText("continue");

                    System.out.println("停止");
                } else {
                    receiveAndProcessData.cancel(false);
                    receiveAndProcessData.execute();
                    pauseButton.setText("Pause");

                }
            }
        });
        panel.add(pauseButton);
        panel.add(closeButton);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        tm = new DefaultTableModel(
                new Object[][]{
                },
                new String[]{
                        "航班号", "共享航班号", "中转站", "终点站", "预计起飞", "实际起飞", "登记口", "备注"
                }
        );
        table.setModel(tm);
        scrollPane.setViewportView(table);
    }

    class ReceiveAndProcessData extends SwingWorker<String, String[]> {

        InputStream inputStream;

        Properties airports = new Properties();

        /**
         * 有关网络协议的配置
         */

        private String procotol;

        private String TCP_Address = "localHost";

        private String TCP_Port;

        private String UDP_Address;

        private String UDP_Port;

        //定义接受/读取到的原始航班数据
        LinkedList<String> inputData = new LinkedList<>();
        //定义处理完成的集合变量
        Hashtable<String, String[]> outputData = new Hashtable<>();

        //通过volatile的类型来监听数据是否接受完毕
        public volatile static boolean readFlag = true;


        {
            try {
                inputStream = new FileInputStream("airport.txt");
                airports.load(new InputStreamReader(inputStream, "UTF-8"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 处理传入的参数
         *
         * @param procotol
         * @param tcp_address
         * @param tcp_port
         * @param udp_address
         * @param udp_port
         */
        public ReceiveAndProcessData(String procotol, String tcp_address, String tcp_port, String udp_address, String udp_port) {
            this.procotol = procotol;
            this.TCP_Address = tcp_address;
            this.TCP_Port = tcp_port;
            this.UDP_Address = udp_address;
            this.UDP_Port = udp_port;
        }


        @Override
        protected void process(List<String[]> chunks) {
            new outputData(outputData).start();
        }

        @Override
        protected String doInBackground() throws Exception {
            String totalLine = null;
            String line;
            /**
             * UDP协议
             */
            readData readDataThread = new readData(inputData, procotol);
            analysisData analysisDataThread = new analysisData(inputData, outputData);
            if ("UDP".equals(procotol)) {
                readDataThread.start();
                analysisDataThread.start();
                publish();
            } else if ("TCP".equals(procotol)) {
                readDataThread.start();
                analysisDataThread.start();
                publish();
            } else {
                throw new Exception("网络请求异常");
            }
            return Integer.toString(count);
        }

    }

    /**
     * 数据读入进程
     */
    class readData extends Thread {
        private LinkedList<String> inputData;

        private String protocol;

        String totalLine = null;

        String line;

        public readData(LinkedList<String> inputData, String protocol) {
            this.inputData = inputData;
            this.protocol = protocol;
        }

        @Override
        public void run() {
            if ("UDP".equals(procotol)) {
                try (
                        DatagramSocket socket = new DatagramSocket(0);
                ) {
                    InetSocketAddress server = new InetSocketAddress(UDP_Address, Integer.parseInt(UDP_Port));
                    //发完请求包
                    DatagramPacket requestData = new DatagramPacket(new byte[1], 1, server);
                    socket.send(requestData);
                    //设置超时时间(10s)
                    socket.setSoTimeout(10000);
                    byte[] buffer = new byte[8 * 1024];
                    DatagramPacket responseData = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responseData);
                    //转换为字符串
                    totalLine = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());
                    System.out.println(totalLine);
                    tm.setRowCount(0);
                    //弹窗显示信息
                    JOptionPane.showMessageDialog(null, "连接服务器成功\r\n" + totalLine);
                    while (!receiveAndProcessData.isCancelled()) {
                        socket.receive(responseData);
                        line = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());
//                    System.out.println(line);
                        if ("shutDown!".equals(line)) {
                            JOptionPane.showMessageDialog(null, "系统即将停止服务!" + "\n");
                            break;
                        }
                        if ("no data!".equals(line)) {
                            JOptionPane.showMessageDialog(null, "数据接受完成!" + "\n");
                            break;
                        }
                        /**
                         * 如果有需要的信息
                         */

                        if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
                                || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
                                || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
                            //加锁，申请同步访问权
                            synchronized (inputData) {
                                //添加到链表，实现先进先出，最末尾添加
                                inputData.add(inputData.size(), line);
                                //唤醒单个线程
                                inputData.notify();
                            }
                            //休眠
                            Thread.sleep(50);
                        }
                    }
                }
                //处理超时异常
                catch (SocketTimeoutException socketTimeoutException) {
                    JOptionPane.showMessageDialog(null, "连接超时");
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if ("TCP".equals(procotol)) {
                try {
                    Socket TCP_Socker = new Socket(TCP_Address, Integer.parseInt(TCP_Port));
                    System.out.println("TCP" + TCP_Address + ": " + TCP_Port);
                    TCP_Socker.setSoTimeout(5000);
                    Scanner sc = new Scanner(TCP_Socker.getInputStream());
                    JOptionPane.showMessageDialog(null, "连接服务器成功!" + "\n");
                    //&& !ReceiveAndProcessData.isCancelled()
                    while (sc.hasNextLine()) {
                        line = sc.nextLine();
                        if ("shutDown!".equals(line)) {
                            JOptionPane.showMessageDialog(null, "系统即将停止服务!" + "\n");
                            
                            break;
                        }
                        if ("no data!!".equals(line)) {
                            JOptionPane.showMessageDialog(null, "接受数据完成!" + "\n");
                            break;
                        }
                        /**
                         * 如果有需要的信息
                         */

                        if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
                                || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
                                || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
                            //加锁，申请同步访问权
                            synchronized (inputData) {
                                //添加到链表，实现先进先出，最末尾添加
                                inputData.add(inputData.size(), line);
                                //唤醒单个线程
                                inputData.notify();
                            }
                            //休眠
                            Thread.sleep(50);
                        }

                    }
                }
                //处理超时异常
                catch (SocketTimeoutException socketTimeoutException) {
                    JOptionPane.showMessageDialog(null, "连接超时");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {

            }
        }
    }

    /**
     * 数据分析线程
     */

    class analysisData extends Thread {
        private List<String> inputData;
        private Hashtable<String, String[]> outputData;

        /**
         * 定义各种数据
         */

        static String flid = null;  // 即数据文件中每行数据的flid
        static String flightNumber = null; // 航班号，如CA1402
        static String sfawOne = null; // 共享航班公司1
        static String sfnoOne = null; // 共享航班号1
        static String sfawTwo = null; // 共享航班公司2
        static String sfnoTwo = null; // 共享航班号2
        static String sfawThree = null; // 共享航班公司3
        static String sfnoThree = null; // 共享航班号3
        static String aimStation = null; // 目的站
        static String transferStation = null; // 中转站
        static String destinationStation = null; //终点站
        static String planToFlyTime = null; // 预计起飞时间
        static String changeToFlyTime = null; // 变更起飞时间
        static String actualFlyTime = null;//实际起飞时间
        static String gate = null; // 登机口
        static String state = null; // 登机状态


        /**
         * 引入properties类，用于将机场代码转为机场名称
         */
        static Properties airports = new Properties();

        static InputStream inputStream;

        /**
         * 定义静态块只需要初始化一次airports对象
         */

        static {
            try {
                inputStream = new FileInputStream("airport.txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                airports.load(new InputStreamReader(inputStream, "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * @param inputData
         * @param outputData
         */
        public analysisData(List<String> inputData, Hashtable<String, String[]> outputData) {
            this.inputData = inputData;
            this.outputData = outputData;
        }


        @Override
        public void run() {
            while (true) {
                String line;
                synchronized (inputData) {
                    //如果数据为空，则等待
                    while (inputData.isEmpty()) {
                        //如果还数据未接受完毕
                        if (readFlag) {
                            try {
                                inputData.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            return;
                        }
                    }
                    //取出数据并移除
                    line = inputData.remove(0);
                }
                /**
                 * 对航班数据进行分析(如果有需要的信息)
                 */
                if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
                        || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
                        || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
                    analysisEveryLine(line);
                }
            }
        }

        /**
         * 专门分析每一行的数据,并添加到数据outData中
         *
         * @param line 每一行的数据
         */
        private void analysisEveryLine(String line) {
            /**
             * 每次分析一行数据初始化置空
             */
            flid = null;  // 即数据文件中每行数据的flid
            flightNumber = null; // 航班号，如CA1402
            sfawOne = null; // 共享航班公司1
            sfnoOne = null; // 共享航班号1
            sfawTwo = null; // 共享航班公司2
            sfnoTwo = null; // 共享航班号2
            sfawThree = null; // 共享航班公司3
            sfnoThree = null; // 共享航班号3
            aimStation = null; // 目的站
            transferStation = null; // 中转站
            destinationStation = null;
            planToFlyTime = null; // 预计起飞时间
            changeToFlyTime = null; // 变更起飞时间
            actualFlyTime = null;//实际起飞时间
            gate = null; // 登机口
            state = null; // 登机状态

            /**
             * 求航班唯一标识(fild)和航班号flightNumber
             * 航向分析以判断是否为出港航班
             */
            //匹配[xxxxxx]
            Matcher matcher = Pattern.compile("\\[.*\\]").matcher(line);
            if (matcher.find()) {
                String[] flight = matcher.group().split("=");
                flid = flight[1].substring(0, flight[1].lastIndexOf(','));
                String ffid = flight[2];
                flightNumber = Stream.of(ffid.split("-")).limit(2).collect(Collectors.joining());
                //如果是进港航班，直接返回
                if ("A".equals(ffid.substring(ffid.lastIndexOf(',') - 1, ffid.lastIndexOf(',')))) {
                    return;
                }
            }
            /**
             * 求共享航班号
             */
            if (line.contains("SFLG")) {
                matcher = Pattern.compile("SFLT \\[.*\\]").matcher(line);
                if (matcher.find()) {
                    String shareNo = matcher.group();
                    shareNo = shareNo.substring(6, shareNo.length() - 4);
                    String[] shareNos = shareNo.split("\\], SFLT \\[");
                    if (shareNos.length == 1) {
                        sfawOne = shareNos[0].substring(5, 7);
                        sfnoOne = shareNos[0].substring(14, 18);
                    } else if (shareNos.length == 2) {
                        sfawOne = shareNos[0].substring(5, 7);
                        sfnoOne = shareNos[0].substring(14, 18);
                        sfawTwo = shareNos[1].substring(5, 7);
                        sfnoTwo = shareNos[1].substring(14, 18);
                    } else if (shareNos.length == 3) {
                        sfawOne = shareNos[0].substring(5, 7);
                        sfnoOne = shareNos[0].substring(14, 18);
                        sfawTwo = shareNos[1].substring(5, 7);
                        sfnoTwo = shareNos[1].substring(14, 18);
                        sfawThree = shareNos[2].substring(5, 7);
                        sfnoThree = shareNos[2].substring(14, 18);
                    }
                    System.out.println("共享航班号:" + sfawOne + sfnoOne);
                }
            }
            /**
             * 求起始地与中转地
             */
            if (line.contains("apcd")) {
                matcher = Pattern.compile("ARPT\\[.*\\]").matcher(line);
                if (matcher.find()) {
                    String stationTemp = matcher.group();
                    stationTemp = stationTemp.substring(5, stationTemp.length() - 4);
                    String[] stationTempStrings = stationTemp.split("\\], ARPT\\[");
                    //含有目的站和终点站
                    if (stationTempStrings.length == 2) {
                        String destination = stationTempStrings[1].substring(13, 16);
                        destinationStation = airports.getProperty(destination);
                    }
                    //含有目的站中转站和终点站
                    if (stationTempStrings.length == 3) {
                        String transfer = stationTempStrings[1].substring(13, 16);
                        transferStation = airports.getProperty(transfer);
                        String destination = stationTempStrings[2].substring(13, 16);
                        destinationStation = airports.getProperty(destination);
                    }
                }
                System.out.println(destinationStation + " " + transferStation);
            }
            /**
             * 求登机门
             */
            if (line.contains("GATE")) {
                matcher = Pattern.compile("GATE\\[.*?\\]").matcher(line);
                if (matcher.find()) {
                    String gatee = matcher.group();
                    gatee = gatee.substring(5, gatee.length() - 1);
                    String[] strings = gatee.split(", ");
                    gate = strings[2].substring(5);
                }
            }
            /**
             * 求起飞落地时间
             * <FPTT>	计划起飞时间
             * <FETT>	预计起飞时间
             * <FRTT>   实际起飞时间
             * 后续将简化
             */
            if (line.contains("fptt") || line.contains("fett")) {//一个航班同时fett和fptt，就用fett
                matcher = Pattern.compile("ARPT\\[.*?\\]").matcher(line);

                if (matcher.find()) {
                    String timeFly = matcher.group();
                    String[] strings = timeFly.split(", ");
                    //选择planToFlyTime
                    String planFly = strings[2].substring(5);
                    //20180923080000
                    if (!"null".equals(planFly)) {
                        StringBuilder builder1 = new StringBuilder(planFly.substring(8, planFly.length() - 2));
                        builder1.insert(2, ":");
                        planToFlyTime = builder1.toString();
                    }
                    //选择
                    String realFly = strings[4].substring(5);
                    if (!"null".equals(realFly)) {
                        StringBuilder builder2 = new StringBuilder(realFly.substring(8, planFly.length() - 2));
                        builder2.insert(2, ":");
                        actualFlyTime = builder2.toString();
                        System.out.println("实际" + actualFlyTime);
                    }

                }
            }
            if (!line.contains("fptt") && line.contains("fett")) {//一个航班只有fett就用fett
                matcher = Pattern.compile("\\[.*\\]").matcher(line);

                if (matcher.find()) {
//                    System.out.println(matcher5.group());
//                    [flid=1418075, ffid=GS-7892-20180925-A, fett=null, felt=20180925012500]
                    Pattern pattern2 = Pattern.compile("(?<=fett=).*?(?=,)");
                    Matcher matcher6 = pattern2.matcher(line);
                    if (matcher6.find()) {
                        String predictToFly = matcher6.group();
                        if (!"null".equals(predictToFly)) {
                            StringBuilder builder3 = new StringBuilder(predictToFly.substring(8, predictToFly.length() - 2));
                            builder3.insert(2, ":");
                            //临时更换成actualFlyTime
                            actualFlyTime = builder3.toString();

                        }
                    }
                }
            }

            /**
             * 求航班的状态
             */
            if (line.contains("BORE"))
                state = "开始登机";
            if (line.contains("POKE"))
                state = "结束";
            if (line.contains("DLYE"))
                state = "航班延误";
            if (line.contains("CANE"))
                state = "航班取消";
            /**
             * 归并分析结果
             */

            synchronized (outputData) {
                if (outputData.get(flid) == null) {
                    outputData.put(flid, new String[]{flightNumber, sfawOne, sfnoOne, sfawTwo, sfnoTwo, sfawThree, sfnoThree,
                            transferStation, destinationStation, planToFlyTime, actualFlyTime, gate, state});
                } else {
                    String[] values = outputData.get(flid);
                    String[] items = {flightNumber, sfawOne, sfnoOne, sfawTwo, sfnoTwo, sfawThree, sfnoThree,
                            transferStation, destinationStation, planToFlyTime, actualFlyTime, gate, state};
                    for (int i = 0; i < 13; i++) {
                        values[i] = items[i] != null ? items[i] : values[i];
                    }
                    outputData.put(flid, values);
                    outputData.notify();
                }
            }


        }

    }

    /**
     * 数据输出线程
     */

    class outputData extends Thread {
        private Hashtable<String, String[]> outputData;

        public outputData(Hashtable<String, String[]> outputData) {
            this.outputData = outputData;
        }

        @Override
        public void run() {
            while (true) {
                /**
                 * 如果下面语句注释，则程序一直输出同一条数据  ？？？？？
                 */
                System.out.println("----------------更新输出!!--------------------");
                synchronized (outputData) {
                    while (outputData.isEmpty()) {
                        try {
                            outputData.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    /**
                     * 开始输出
                     */
//                System.out.println("----开始输出-----");
                    Iterator<String> iterator = outputData.keySet().iterator();
                    int count = 0;
                    while (iterator.hasNext()) {
                        String fild = iterator.next();
                        String[] values = outputData.get(fild);
                        //共享航班陈对出现，如果为空航班号也为空
                        for (int n = 1; n <= 6; n += 2) {
                            if (values[n] == null) {
                                values[n] = " ";
                                values[n + 1] = " ";
                            }
                        }
                        /**
                         * 需要优化来控制格式相同
                         */
                        values[0] = String.format("%-10s", values[0]);
//                        System.out.println("主航班号： " + values[0] + "共享航班号1：" + values[1] + values[2]
//                                + "共享航班号2：" + values[3] + values[4] + "共享航班号3：" + values[5]
//                                + values[6] + " 中转站: " + values[7] + " 目的站: " + values[8]
//                                + " 预计起飞时间： " + values[9] + " 实际起飞时间： " + values[10] + " 登机口： "
//                                + values[11] + " 航班状态： " + values[12]);
                        tm.addRow(new String[]{values[0], values[1] + values[2], values[7], values[8], values[9], values[10], values[11], values[12]});
                        if ((++count) % 10 == 0) {
//                            System.out.println("---------------------------------------------------------------------------------");
                        }
                    }
                    /**
                     * 清空数据
                     */
//                    System.out.println("此时应该清空数据");
                    try {
                        Thread.sleep(1000);
                        tm.getDataVector().removeAllElements();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}