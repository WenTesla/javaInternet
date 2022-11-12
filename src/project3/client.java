package project3;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.awt.event.ActionEvent;


/**
 * client
 * @author BoWen
 * @Data
 */
public class client extends JFrame {


    /**
     * 定义各种数据
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

    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tm;
    private int count=0;
    private ReceiveAndProcessData receiveAndProcessData;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    client frame = new client();
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

    public client() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 591, 640);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);

        JButton btnNewButton = new JButton("Start");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

//				try {
//					Socket socket = new Socket("43.140.200.253",9999);
//					Scanner sc = new Scanner(socket.getInputStream());
//					String line = sc.nextLine();
//					JOptionPane.showMessageDialog(null, "连接服务器成功!"+"\n"+line);
//
//					while(sc.hasNextLine()) {
//						line=sc.nextLine();
//						tm.addRow(new String[]{(count++)+" ",line,"xxxx"});
//					}
//
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}

                if (receiveAndProcessData == null) {
                    receiveAndProcessData= new ReceiveAndProcessData();
                    receiveAndProcessData.execute();
                    btnNewButton.setEnabled(false);

                }

                else if (receiveAndProcessData.isCancelled()) {
                    btnNewButton.setEnabled(true);
                    receiveAndProcessData.cancel(false);
                    receiveAndProcessData=null;

                }

            }
        });
        panel.add(btnNewButton);

        JButton btnEnd = new JButton("Close");
        btnEnd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JButton btnNewButton_1 = new JButton("Pause");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(receiveAndProcessData!=null && !receiveAndProcessData.isCancelled()) {
                    receiveAndProcessData.cancel(true);
                    btnNewButton_1.setText("continue");

                    System.out.println("停止");
                }
                else {
                    receiveAndProcessData.cancel(false);
                    btnNewButton_1.setText("Pause");

                }
            }
        });
        panel.add(btnNewButton_1);
        panel.add(btnEnd);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        tm=new DefaultTableModel(
                new Object[][] {
                        {null, null, null, null, null, null,null,null,null},
                },
                new String[] {
                         "航班号", "共享航班号", "中转站", "终点站", "预计起飞", "实际起飞","登记口","备注"
                }
        );


        table.setModel(tm);

//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane.setViewportView(table);



    }
    class ReceiveAndProcessData extends SwingWorker<String, String[]>{

        String[] outputData= new String[10];

        InputStream inputStream;

        Properties airports = new Properties();

        {
            try {
                inputStream = new FileInputStream("airport.txt");
                airports.load(new InputStreamReader(inputStream,"UTF-8"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }






        @Override
        protected void process(List<String[]> chunks) {
            for (String[] data:chunks){

                if (data != null) {
                    System.out.println(Arrays.toString(data));
                    tm.addRow(data);
                }
            }


        }
        /**
         * 开始分析每一行
         * @param line
         * @return
         */
        private String[] analysisData(String line) {

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

            //匹配[xxxxxx]
            Matcher matcher = Pattern.compile("\\[.*\\]").matcher(line);
            if (matcher.find()) {
//            System.out.println(matcher.group());
                String[] flight = matcher.group().split("=");
                flid = flight[1].substring(0, flight[1].lastIndexOf(','));
                String ffid = flight[2];
//            System.out.println(ffid);
                flightNumber = Stream.of(ffid.split("-")).limit(2).collect(Collectors.joining());
//            System.out.println("flid:"+flid+" flightNumber:"+flightNumber);
//            if (flightNumber.length()>=7){
//                System.out.println(flightNumber);
//            }
//            System.out.println(ffid.substring(ffid.lastIndexOf(',') - 1, ffid.lastIndexOf(',')));
                //如果是进港航班，直接返回
                if ("A".equals(ffid.substring(ffid.lastIndexOf(',') - 1, ffid.lastIndexOf(',')))) {
//                System.out.println(ffid);
                    return null;
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
                    //sfaw=ZH, sfno=1402], SFLT [sfaw=KY, sfno=1402], SFLT [sfaw=SC, sfno=1402
//                System.out.println(shareNo);
                    String[] shareNos = shareNo.split("\\], SFLT \\[");
//                    System.out.println(sharNos);
                    if (shareNos.length == 1) {
                        sfawOne = shareNos[0].substring(5, 7);
//                    System.out.println(sfawOne);
                        sfnoOne = shareNos[0].substring(14, 18);
//                    System.out.println(sfnoOne);
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

//                System.out.println("共享航班号1: " + sfawOne  + "\t"+
//                        "共享航班号2: " + sfawTwo + sfnoTwo + "\t"+  "共享航班号3: " + sfawThree + sfnoThree + "\t"  );
                }
            }



            /**
             * 求起始地与中转战
             */
            if (line.contains("apcd")) {
                matcher = Pattern.compile("ARPT\\[.*\\]").matcher(line);
                if (matcher.find()) {
                    String stationTemp = matcher.group();
//                    System.out.println(stationTemp);
                    //ARPT[arno=1, apcd=TSN, fptt=20180924123000, fett=null, frtt=null, fplt=null, felt=null, frlt=null, apat=2403], ARPT[arno=2, apcd=HSN, fptt=20180924152000, fett=null, frtt=null, fplt=20180924143500, felt=null, frlt=null, apat=2403], ARPT[arno=3, apcd=FOC, fptt=null, fett=null, frtt=null, fplt=20180924164000, felt=null, frlt=null, apat=2403], ]

                    stationTemp = stationTemp.substring(5, stationTemp.length() - 4);
                    String[] stationTempStrings = stationTemp.split("\\], ARPT\\[");
                    //arno=1, apcd=TSN, fptt=20180923080000, fett=null, frtt=null, fplt=null, felt=null, frlt=null, apat=2403
                    //arno=2, apcd=PVG, fptt=null, fett=null, frtt=null, fplt=20180923093500, felt=null, frlt=null, apat=2403
                    //含有目的站和终点站
                    if (stationTempStrings.length == 2) {
                        String destination = stationTempStrings[1].substring(13, 16);
                        destinationStation = airports.getProperty(destination);
//                        System.out.println(destinationStation);
                    }
                    //含有目的站中转站和终点站
                    if (stationTempStrings.length == 3) {
                        String transfer = stationTempStrings[1].substring(13, 16);
                        transferStation = airports.getProperty(transfer);
                        String destination = stationTempStrings[2].substring(13, 16);
                        destinationStation = airports.getProperty(destination);
                    }
//                System.out.println("中转站:"+transferStation+" 终点站："+destinationStation);
                }
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
//            System.out.println("gate:" + gate);
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

//                matcher = Pattern.compile("ARPT\\[.*?\\]").matcher(line);
                if (matcher.find()) {
                    String timeFly = matcher.group();
//                    System.out.println(timeFly);
//                    ARPT[arno=1, apcd=TSN, fptt=20180923001000, fett=null, frtt=null, fplt=null, felt=null, frlt=null, apat=2403]
                    String[] strings = timeFly.split(", ");
                    //选择planToFlyTime
                    String planFly = strings[2].substring(5);
                    //20180923080000
                    if (!"null".equals(planFly)) {
                        StringBuilder builder1 = new StringBuilder(planFly.substring(8, planFly.length() - 2));
                        builder1.insert(2, ":");
                        planToFlyTime = builder1.toString();
//                    System.out.println("预计"+planToFlyTime);
//                   strings[4]
                    }
                    //选择
                    String realFly = strings[4].substring(5);
                    if (!"null".equals(realFly)) {
                        StringBuilder builder2 = new StringBuilder(realFly.substring(8, planFly.length() - 2));
                        builder2.insert(2, ":");
                        actualFlyTime = builder2.toString();
//                        System.out.println("实际"+actualFlyTime);
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
                            planToFlyTime = builder3.toString();

                        }
//                        System.out.println(planToFlyTime);
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




            return new String[]{flightNumber,sfnoOne,transferStation,destinationStation,planToFlyTime,actualFlyTime,gate,state};
        }

        @Override
        protected String doInBackground() throws Exception {

            // TODO Auto-generated method stub
            try {
//                Socket clientSockerSocket = new Socket("43.140.200.253",9999);
				Socket clientSockerSocket = new Socket("localHost",9999);
                Scanner sc = new Scanner(clientSockerSocket.getInputStream());
                String line = sc.nextLine();
                JOptionPane.showMessageDialog(null, "连接服务器成功!");

                while(sc.hasNextLine() && !isCancelled()) {
                    line=sc.nextLine();
                    /**
                     * 如果有需要的信息
                     */
                    if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
                            || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
                            || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
                        outputData = analysisData(line);

                        publish(outputData);

                    }

                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return Integer.toString(count);
        }

    }

}
