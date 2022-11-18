package End;


import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 重构后的Server GUI
 *
 * @author BoWen
 * @version 2.0
 * @date 2022/11/17
 */
public class server extends JFrame {
    /**
     * GUI
     */
    private JPanel contentPane;
    private JTextField TCP_addressText;
    private JTextField TCP_portText;
    private JTextField UDP_addressText;
    private JTextField UDP_portText;

    private JTable TCP_table;

    static DefaultTableModel TCP_tm;

    private JTable UDP_table;

    static DefaultTableModel UDP_tm;

    /**
     * 有关网络协议的配置
     */
    private String procotol;

    private static String TCP_Address = "localHost";

    private static String TCP_Port = "9999";

    private static String UDP_Address;

    private static String UDP_Port = "9999";

    /**
     * UDP 服务器
     */
    private static UdpServer udpServer;

    public static void setUdpServer(String UDP_Address, String UDP_Port) {

        //处理端口号
        int port = Integer.parseInt(UDP_Port);
//        InetAddress inetAddress = null;
//        //处理address
//        try {
//            inetAddress = InetAddress.getByName(UDP_Address);
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
        udpServer = new UdpServer(8192, UDP_Address, port);

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
    Thread UDP_thread = new Thread(udpServerThread);

    /**
     * TCP 服务器
     */
    private TcpServer tcpServer;

    private void setTcpServer(String TCP_Address, String TCP_Port) {
        try {
            tcpServer = new TcpServer(TCP_Address, TCP_Port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    FutureTask tcpServerThread = new FutureTask(new Callable() {
        @Override
        public Object call() throws Exception {
//            tcpServer.main(new String[]{TCP_Address,TCP_Port});
//            tcpServer = new TcpServer(TCP_Address,TCP_Port);
            tcpServer.run();
            return "启动成功";
        }
    });
    //创建线程对象
    Thread TCP_thread = new Thread(tcpServerThread);

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    server frame = new server();
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
    public server() {
        setTitle("Server");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 638, 541);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        /**
         * TCP 部分
         */
        JPanel TCP_Pane = new JPanel();
        tabbedPane.addTab("TCP", null, TCP_Pane, null);
        TCP_Pane.setLayout(null);
        JScrollPane TCP_scrollPane = new JScrollPane();
        TCP_scrollPane.setBounds(0, 0, 609, 258);
        TCP_Pane.add(TCP_scrollPane);

        JLabel TCP_address = new JLabel("Address");
        TCP_address.setHorizontalAlignment(SwingConstants.CENTER);
        TCP_address.setBounds(160, 300, 58, 15);
        TCP_Pane.add(TCP_address);

        JLabel TCP_port = new JLabel("Port");
        TCP_port.setHorizontalAlignment(SwingConstants.CENTER);
        TCP_port.setBounds(160, 364, 58, 15);
        TCP_Pane.add(TCP_port);
        /**
         * TCP的启动
         */
        JButton TCP_launchButton = new JButton("Launch");
        TCP_launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!"".equals(TCP_addressText.getText()))
                    TCP_Address = TCP_addressText.getText();
                if (!"".equals(TCP_portText.getText()))
                    TCP_Port = TCP_portText.getText();
                setTcpServer(TCP_Address, TCP_Port);
                TCP_thread.start();
                System.out.println("TCP" + TCP_Address + ":" + TCP_Port);
                TCP_launchButton.setText("Pause Server");
            }
        });
        TCP_launchButton.setForeground(Color.GREEN);
        TCP_launchButton.setBounds(145, 410, 97, 23);
        TCP_Pane.add(TCP_launchButton);

        JButton TCP_shutDownButton = new JButton("ShutDown");
        /**
         * TCP的关闭
         */
        TCP_shutDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("shutDown");
                TcpServer.TCP_currentThreadIsShutDown = true;

            }
        });
        TCP_shutDownButton.setForeground(Color.RED);
        TCP_shutDownButton.setBounds(373, 410, 97, 23);
        TCP_Pane.add(TCP_shutDownButton);

        TCP_addressText = new JTextField();
        TCP_addressText.setBounds(373, 297, 97, 21);
        TCP_Pane.add(TCP_addressText);
        TCP_addressText.setColumns(10);

        TCP_portText = new JTextField();
        TCP_portText.setBounds(373, 361, 97, 21);
        TCP_Pane.add(TCP_portText);
        TCP_portText.setColumns(10);

        TCP_table = new JTable();
        TCP_tm = new DefaultTableModel(
                new Object[][]{
                },
                new String[]{
                        "no", "ip", "port", "status"
                }
        );
        TCP_table.setModel(TCP_tm);
        TCP_scrollPane.setViewportView(TCP_table);

        /**
         * UDP
         */
        JPanel UDP_Pane = new JPanel();
        tabbedPane.addTab("UDP", null, UDP_Pane, null);
        UDP_Pane.setLayout(null);

        JScrollPane UDP_scrollPane = new JScrollPane();
        UDP_scrollPane.setBounds(0, 0, 609, 258);
        UDP_Pane.add(UDP_scrollPane);

        UDP_addressText = new JTextField();
        UDP_addressText.setHorizontalAlignment(SwingConstants.CENTER);
        UDP_addressText.setBounds(378, 296, 90, 21);
        UDP_Pane.add(UDP_addressText);
        UDP_addressText.setColumns(10);

        UDP_portText = new JTextField();
        UDP_portText.setHorizontalAlignment(SwingConstants.CENTER);
        UDP_portText.setBounds(378, 354, 90, 21);
        UDP_Pane.add(UDP_portText);
        UDP_portText.setColumns(10);
        /**
         * UDP启动
         */
        JButton UDP_LaunchButton = new JButton("Launch");
        UDP_LaunchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UDP_Address = UDP_addressText.getText();
                //非空则设置UDP端口为默认端口
                if ("UDP".equals(UDP_portText.getText()))
                    UDP_Port = UDP_portText.getText();
//                System.out.println(UDP_Address + ":" + UDP_Port);
                setUdpServer(UDP_Address, UDP_Port);
                UDP_thread.start();
                UDP_LaunchButton.setText("Pause Server");
            }
        });
        UDP_LaunchButton.setForeground(Color.GREEN);
        UDP_LaunchButton.setBounds(140, 403, 97, 23);
        UDP_Pane.add(UDP_LaunchButton);
        /**
         * UDP的关闭
         */
        JButton UDP_shutDownButton = new JButton("ShutDown");
        UDP_shutDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("ShutDown!");
                FDSDataHandler.UDP_currentThreadIsShutDown = true;
                //清空数据
                UDP_table.removeAll();

//                    System.exit(0);

            }
        });
        UDP_shutDownButton.setForeground(Color.RED);
        UDP_shutDownButton.setBounds(375, 403, 97, 23);
        UDP_Pane.add(UDP_shutDownButton);

        JLabel UDP_address = new JLabel("Address");
        UDP_address.setHorizontalAlignment(SwingConstants.CENTER);
        UDP_address.setBounds(160, 300, 58, 15);
        UDP_Pane.add(UDP_address);

        JLabel UDP_port = new JLabel("Port");
        UDP_port.setHorizontalAlignment(SwingConstants.CENTER);
        UDP_port.setBounds(159, 357, 58, 15);
        UDP_Pane.add(UDP_port);

        UDP_table = new JTable();
        UDP_tm = new DefaultTableModel(
                new Object[][]{
                },
                new String[]{
                        "no", "ip", "port", "status"
                }
        );
        UDP_table.setModel(UDP_tm);
        UDP_scrollPane.setViewportView(UDP_table);

    }

//	private static void addPopup(Component component, final JPopupMenu popup) {
//		component.addMouseListener(new MouseAdapter() {
//			public void mousePressed(MouseEvent e) {
//				if (e.isPopupTrigger()) {
//					showMenu(e);
//				}
//			}
//			public void mouseReleased(MouseEvent e) {
//				if (e.isPopupTrigger()) {
//					showMenu(e);
//				}
//			}
//			private void showMenu(MouseEvent e) {
//				popup.show(e.getComponent(), e.getX(), e.getY());
//			}
//		});
//	}
}
