package project3;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Flow.Publisher;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class ex3 extends JFrame {

	private JPanel contentPane;
	private JTable table;
	private  DefaultTableModel tm; 
	private int count = 0;
	private ReceveAndProcessDate receveAndProcessDate;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ex3 frame = new ex3();
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
	public ex3() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //关闭窗口方式
		setBounds(100, 100, 754, 469); //边界

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0)); //内容面板
		
		JPanel controlpanel = new JPanel();
		contentPane.add(controlpanel, BorderLayout.NORTH); //控制面板在上方
		
		JButton btnStartButton = new JButton("\u542F\u52A8"); //启动按钮
		btnStartButton.addActionListener(new ActionListener() { //启动按钮事件监听器
			public void actionPerformed(ActionEvent e) {
				if(receveAndProcessDate==null) {
					receveAndProcessDate = new ReceveAndProcessDate(); //调用ReceveAndProcessDate()方法
					receveAndProcessDate.execute(); //执行，StringWorker的方法，类似于Thread类的start()方法
					btnStartButton.setEnabled(false); //设置启动按钮不能重复点击
				}
				
				
			}

			private void publish(String line) {
				// TODO Auto-generated method stub
				
			}
		});
		controlpanel.add(btnStartButton); //控制面板添加启动按钮
		
		JButton btnStopButton = new JButton("\u505C\u6B62"); //停止按钮
		btnStopButton.setBackground(Color.RED); //停止按钮背景颜色设为红色
		controlpanel.add(btnStopButton); //控制面板添加停止按钮
		
		JScrollPane scrollPane = new JScrollPane(); //滚动面板
		contentPane.add(scrollPane, BorderLayout.CENTER); //滚动面板布局在中心
		
		table = new JTable(); //表格控件，以表格形式显示航显信息
		tm = new DefaultTableModel(
				new Object[][] {
				{0, "DMFU", null},
			}, //第一行内容
			new String[] {
				"\u822a\u73ed\u53f7", "\u6765\u81ea", "\u884c\u674e\u8f6c\u76d8" //从左到右分别是：航班编号，航班数据，行号
			} //列表顶层每列的属性
		);
		
		table.setModel(tm); //设定表格形式
		scrollPane.setViewportView(table); //在滚动面板上显示航显信息表
	}
	class ReceveAndProcessDate extends SwingWorker<String, String>{ //SwingWorker，分离EDT和任务线程

		@Override //覆盖doInBackground，实现多态
		protected String doInBackground() throws Exception {

			Pattern p = Pattern.compile("(?<=DFME_BLLS).*?(?=], ])");
			int count = 0;
			String[] outputList = new String[10];
			try {
				Socket clientSocket = new Socket("localhost",9999);
				//构造和连接 服务器Socket，本地主机，端口9999
				Scanner sc = new Scanner(clientSocket.getInputStream());
				//用clientSocket从服务器读取输入流
				String line = null;
				// InputStream is = clientSocket.getInputStream();
				while(sc.hasNextLine()) { //逐行读取
					line = sc.nextLine();
					Matcher m = p.matcher(line);
					while (m.find()) {
						outputList[count] = line;
						count++;
					}
					if (!(outputList[9] == null)) {
						if (checkSameFlightID(outputList)) {
							System.out.println("check");
							outputList = fixoutputList(outputList);
							System.out.println("fixed");
							for (int i = 0;i<10;i++) {
								if (outputList[i] == null) {
									count = i-1;
									break;
								}
							}
							continue;
						}
						System.out.println("4");
						count = 0;
						for(int i = 0;i<10;i++) {
							outputList[i] = LineProcess(outputList[i]);
							publish(outputList[i]);
							outputList[i] = null;
						}
						publish("**********************************************-**********************************************-**********************************************");
						sleep(500);
						//间隔500ms输出10个数据,实际非常可能大于500ms,取决于if前方代码执行快慢
					}
				}

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return (count)+" ";
		}

		@Override //覆盖process
		protected void process(List<String> chunks) { //接收publish发布的内容
			for(String line:chunks) {
				String[] row = line.split("-");
				tm.addRow(new String[] {row[0],row[1],row[2]}); //添加行
			}
		}
		
	}

	String LineProcess(String line) throws Exception {

		String flightOutputid = new String(); //输出航班编号
		String flightid = new String(); //航班参数，找机场用的
		String airportid = new String(); //输出机场编码
		String luggageid = new String(); //行李编码

		Pattern p1 = Pattern.compile("(?<=ffid=).*?(?=, )");
		Matcher m1 = p1.matcher(line);
		while (m1.find()) {
				String[] tt = m1.group().split("-");
				flightOutputid = tt[0] + tt[1]; //航班号
				flightid = tt[0] + "-" + tt[1];
				try {
					airportid = airportCode(flightid); // 机场编码
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				// 行李转盘号
				Pattern p2 = Pattern.compile("(?<=code=).*?(?=,)");
				Matcher m2 = p2.matcher(line);
				while (m2.find()) {
					luggageid = m2.group();
				}
		}
		String airportCNName = airportCodeToChineseName(airportid);
		String outputline = new String();
		outputline = flightOutputid + "-" +airportCNName + "-" + luggageid;
		System.out.println("LineProcess执行完毕");
		return outputline;
	}

	String airportCodeToChineseName(String airportid) throws Exception{
		String airportlist = new String();
		String Chinesename = new String();

		FileInputStream fis1 = new FileInputStream("airport.txt");
		try (fis1) {
			byte[] buffer = fis1.readAllBytes();
			airportlist = new String(buffer);
		}

		String[] s1 = airportlist.split("[A-Z]{3} "); //将airport.txt内容按“机场三字码空格”分隔
		StringBuffer s2 = new StringBuffer();
		for (int i = 0; i < s1.length; i++){
			s2. append(s1[i]);
		}

		String s3 = s2.toString();
		String[] airportChineseName = s3.split("\r\n"); //把中间结果按“回车换行”分隔
		//得到机场中文

		String[] airport_code = airportlist.split(" .+\r\n"); //将airport.txt的内容按“空格机场中文名回车换行”分隔
		// 得到机场编码

		for (int i=0;i<airport_code.length;i++){
			if(airportid.equals(airport_code[i])){
				Chinesename = airportChineseName[i];
			}
		}
		System.out.println("airportCodeToChineseName执行完毕");
		return Chinesename;
	}

	String airportCode(String flightid) throws Exception{ //输入航班号参数，返回机场编码
		String fds = new String();
		String airportid = new String();
		FileInputStream fis2 = new FileInputStream("fdsdata.txt");
		try (fis2) {
			byte[] buffer = fis2.readAllBytes();
			fds = new String(buffer);
		}

		Pattern p = Pattern.compile("(?<=DFME_AIRL).*?(?=], ])"); //提取AIRL航班航线
		Matcher m = p.matcher(fds);
		while (m.find()){
			Pattern p1 = Pattern.compile("(?<=ffid=)[A-Z0-9]{2}-[0-9]{3,4}"); //提取航班编号，例如SC-8633
			Matcher m1 = p1.matcher(m.group());
			while (m1.find()) {
				if (flightid.equals(m1.group())){ //匹配输入的航班编号参数，找到航线起点机场
					Pattern p2 = Pattern.compile("(?<=arno=1, apcd=).*?(?=,)"); //提取起点机场三字码
					Matcher m2 = p2.matcher(m.group());
					while (m2.find()) {
//                        IDtoName(m2.group());
						airportid = m2.group();
					}
				}
			}
		}
		System.out.println("airportCode执行完毕");
		return airportid; //给IDtoChineseName
	}

	boolean checkSameFlightID (String[] List) {
		String flid = new String();
		String[] flidList = new String[10];
		Pattern p = Pattern.compile("(?<=flid=).*?(?=,)");
		for (int i=0; i<10; i++) {
			Matcher m = p.matcher(List[i]);
			if(m.find())
				flidList[i] = m.group();
		}

		for (int i=0; i<10; i++) {
			for (int j=i+1; j<10; j++) {
				if (flidList[i].equals(flidList[j]))
					return true;
			}
		}
		return false;
	}

	String[] fixoutputList(String[] List) {
		String ddtm1 = new String();
		String ddtm2 = new String();
		String[] fixedList = new String[10];
		String flid = new String();
		String[] flidList = new String[10];
		Pattern p = Pattern.compile("(?<=flid=).*?(?=,)");
		for (int i=0; i<10; i++) {
			Matcher m = p.matcher(List[i]);
			if(m.find())
				flidList[i] = m.group();
		}

		for (int i=0; i<10; i++) {
			for (int j=i+1; j<10; j++) {
				if (flidList[i].equals(flidList[j])) {
					ddtm1 = List[i].substring(5,19);
					ddtm2 = List[j].substring(5,19);
					int compare = ddtm1.compareTo(ddtm2);
					if(compare > 0)
						flidList[j] = "0";
					else
						flidList[i] = "0";


				}

			}
			System.out.println(flidList[i]);
		}

		int count = 0;
		for (int i=0;i<10;i++) {
			if (!flidList[i].equals("0")){
				fixedList[count] = List[i];
				count++;
			}
		}
		return fixedList;
	}

}