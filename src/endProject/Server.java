package endProject;

import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 服务器的GUI界面
 * 用于选择TCP或者UDP协议
 * 选择address和port
 *
 * @author BoWen
 * @version 1.1
 * @date 2022/11/13
 */
public class Server extends JFrame {

    /**
     * GUI配置
     */

    private JPanel contentPane;
    private JTextField txtPleaseChooseProtol;
    private JTextField addressText;
    private JTextField portText;
    private JTextField txtAddress;
    private JTextField txtPort;
    private JComboBox<String> comboBox;

    /**
     * 用户配置
     */
    private String procotol;

    private static String TCP_Address;

    private static String TCP_Port = "9999";

    private static String UDP_Address;

    private static String UDP_Port = "9999";


    private static UdpServer udpServer;

    public static void setUdpServer(String UDP_Address, String UDP_Port) {

        //处理端口号
        int port = Integer.parseInt(UDP_Port);
        InetAddress inetAddress = null;
        //处理address
        try {
            inetAddress = InetAddress.getByName(UDP_Address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        udpServer = new UdpServer(8192,inetAddress ,port);

    }

    //创建一个未来任务类用于udp使用
    FutureTask udpServerThread = new FutureTask(new Callable() {
        @Override
        public Object call() throws Exception {
            udpServer.run();
            return "启动成功";
        }
    });
    //创建线程对象
    Thread th = new Thread(udpServerThread);

    //


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Server frame = new Server();
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
    public Server() {
        setTitle("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 410, 467);
        setResizable(false);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        txtPleaseChooseProtol = new JTextField();
        txtPleaseChooseProtol.setBounds(5, 5, 386, 32);
        txtPleaseChooseProtol.setForeground(Color.BLACK);
        txtPleaseChooseProtol.setHorizontalAlignment(SwingConstants.CENTER);
        txtPleaseChooseProtol.setEditable(false);
        txtPleaseChooseProtol.setText("please choose protocol");
        txtPleaseChooseProtol.setColumns(10);
        contentPane.setLayout(null);
        contentPane.add(txtPleaseChooseProtol);

        JButton btnNewButton = new JButton("close server");
        btnNewButton.setBackground(Color.WHITE);
        btnNewButton.setForeground(Color.RED);
        /**
         * 关闭服务器按钮
         */
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showConfirmDialog(null, "这是确认对话框吗？", "提示", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);    //确认对话框
//                if (JOptionPane.)
                System.exit(EXIT_ON_CLOSE);
            }
        });
        btnNewButton.setBounds(129, 371, 127, 32);
        contentPane.add(btnNewButton);

        addressText = new JTextField();
        addressText.setBounds(129, 116, 140, 21);
        contentPane.add(addressText);
        addressText.setColumns(10);

        portText = new JTextField();
        portText.setBounds(129, 177, 140, 21);
        contentPane.add(portText);
        portText.setColumns(10);

        JButton btnNewButton_1 = new JButton("Launch Server");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //获取当前下拉框的值
                procotol = (String) comboBox.getSelectedItem();
                //如果是TCP
                if ("TCP".equals(procotol)) {
                    if ("Launch Server".equals(btnNewButton_1.getText())) {
                        TCP_Address=addressText.getText();
                        if (!"".equals(portText.getText()))
                            TCP_Port=portText.getText();


                    }


                }
                //如果是UDP
                else {
                    //启动线程
                    if ("Launch Server".equals(btnNewButton_1.getText())) {
                        //获取值
                        UDP_Address = addressText.getText();
                        if (!"".equals(portText.getText()))
                            UDP_Port = portText.getText();


                        setUdpServer(UDP_Address, UDP_Port);
                        th.start();
                        btnNewButton_1.setText("Pause Server");
                    } else {
                        th.stop();
                        btnNewButton_1.setText("Launch Server");
                    }

//                    btnNewButton_1.set
//                    //获取线程的返回结果
//                    try {
//                        Object o = udpServerThread.get();
//                        System.out.println(o);
//                    } catch (InterruptedException exception) {
//                        exception.printStackTrace();
//                    } catch (ExecutionException exception) {
//                        exception.printStackTrace();
//                    }
//                    UdpServer udpServer = new UdpServer();
//                    udpServer.run();
//                    JOptionPane.showMessageDialog(null, "UDP服务器成功!");
//                    System.out.println("UDP运行");
                }

//				System.out.println(procotol);
            }
        });
        btnNewButton_1.setForeground(Color.GREEN);
        btnNewButton_1.setBackground(Color.WHITE);
        btnNewButton_1.setBounds(129, 273, 127, 32);
        contentPane.add(btnNewButton_1);

        JList list = new JList();
        list.setBounds(142, 57, 1, 1);
        contentPane.add(list);

        comboBox = new JComboBox<String>();
        comboBox.setBounds(129, 47, 133, 23);
        comboBox.addItem("TCP");
        comboBox.addItem("UDP");

        contentPane.add(comboBox);

        txtAddress = new JTextField();
        txtAddress.setHorizontalAlignment(SwingConstants.CENTER);
        txtAddress.setText("address");
        txtAddress.setBounds(20, 116, 66, 21);
        contentPane.add(txtAddress);
        txtAddress.setColumns(10);

        txtPort = new JTextField();
        txtPort.setHorizontalAlignment(SwingConstants.CENTER);
        txtPort.setText("port");
        txtPort.setBounds(20, 177, 66, 21);
        contentPane.add(txtPort);
        txtPort.setColumns(10);
    }


}
//class Udp extends SwingWorker<>
