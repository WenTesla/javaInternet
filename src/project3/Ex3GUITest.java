package project3;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

public class Ex3GUITest extends JFrame {

	private JPanel contentPane;
	private JTable table;
//	tm�Ǳ��ģ��
	private DefaultTableModel tm;
//	count����������ÿһ�еļ�����
	private int count=0;
//	������ReceiveAndProcessDataʵ����,��ָֻ��receiveAndProcessData�Ƕ���ReceiveAndProcessData��ʵ��,�����ڲ�ϸ�ڿ�û����
	private ReceiveAndProcessData receiveAndProcessData;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
//		���Կ���main�������Ǵ����������߳���ִ��mainҪ�ɵ���.ͼ�ν������ʾҪר��һ���߳�����ʾ���
//		EventQueue�¼�����,�кܶ��߳�Ҫæ������¼�����
//		invokeLater()EventQueue������ʾ������ķ���
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Ex3GUITest frame = new Ex3GUITest();
//					����Ĭ���ǲ��ɼ�,����Ҫ����Ϊ�ɼ�
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
	public Ex3GUITest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 702, 435);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.NORTH);
		
		JButton btnStartButton = new JButton("\u542F\u52A8");
		btnStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				��һ��������û��ʵ����receiveAndProcessData��,��null,��ʵ������,��Ҫ��execute()����
				if(receiveAndProcessData==null) {
					receiveAndProcessData = new ReceiveAndProcessData();
					receiveAndProcessData.execute();
//					ִ������ϣ���������ٱ����������(�����ظ�����),�����еĴ���.����ע�͵���,��Ϊ�Ľ���,Ҫ������ͣ��������
//					btnStartButton.setEnabled(false);
				} else if(receiveAndProcessData.isCancelled()) {
					receiveAndProcessData.cancel(false);
					receiveAndProcessData=null;
				}
			}
		});
		controlPanel.add(btnStartButton);
		
		JButton btnCloseButton = new JButton("\u5173\u95ED");
		btnCloseButton.setBackground(new Color(255, 0, 0));
		btnCloseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				�˳�,����
				System.exit(0);
			}
		});
		
		JButton btnPauseButton = new JButton("\u6682\u505C");
		btnPauseButton.setBackground(new Color(255, 255, 128));
		btnPauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				�������ͣ��ťʱ������û����ͣ״̬����Խ�cancel������true
				if(!receiveAndProcessData.isCancelled()) {
					receiveAndProcessData.cancel(true);
					System.out.println("��ͣ��");
				}
			}
		});
		controlPanel.add(btnPauseButton);
		controlPanel.add(btnCloseButton);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
//		tm������ģ�ͣ���Դ��֮��������tm�Ǳ���
		tm=new DefaultTableModel(
//			new Object[][] {
//				{"11111", "DMFU", "0"},
//			},
//			new String[] {
//				"\u822A\u73ED\u7F16\u53F7", "\u822A\u73ED\u6570\u636E", "\u884C\u53F7"
//			}
				
			new Object[][] {
				{"�к�", "������", "ʼ����"},
			},
			new String[] {
				"\u884c\u53f7", "\u822a\u73ed\u7f16\u53f7", "\u59cb\u53d1\u5730"
			}
				
		);
		table.setModel(tm);
		scrollPane.setViewportView(table);
	}
	
//	SwingWorker���������Ͳ���:T��V��T��doInBackground��get�����ķ�������;V��publish��process����Ҫ�������������
//	SwingWorker�������ⶨ����߳�,Current threadӦ�þ�������SwingWorker�ĸɻ�,execute()���������ʵ���ı��߳�;
//	Worker thread,���˴���Ļд����doInBackground()��ִ�кͽ�����ֱ�ӹ�ϵ�ĺ�ʱ�����I/O�ܼ��Ͳ����� ;
//	Event Dispatch Thread,�漰��ǰ��Swing�Ļд������̣߳����ƺ͸��½��棬����Ӧ�û����롣invokes the process���� and done()
	class ReceiveAndProcessData extends SwingWorker<String, String>{

//		doInBackground�ں�ִ̨��
		@Override
//		��Ҫֱ�ӵ���doInBackground������Ӧʹ����������execute����������ִ�С�
		protected String doInBackground() throws Exception {
			String totalLines = null;
			try {
//				���ӷ�����
				Socket clientSocket = new Socket("localhost",9999);
				Scanner sc = new Scanner(clientSocket.getInputStream(), String.valueOf(Charset.forName("utf-8")));
				String line = null;
				totalLines = sc.nextLine();
				JOptionPane.showMessageDialog(null, "���ӷ������ɹ�\r\n" + totalLines);
				while(sc.hasNextLine()) {
					line = sc.nextLine();
//					��������ڿ���̨�����������table�����
//					System.out.println(line);
//					��Ҫ���߳̽��������ϵ�������Ҫ�����ͼ�ν��棬һ���̸߳����£�������ʾ�������������ˣ������رռ���ʧЧ��
//					SwingWorker��doInBackground����������Ų�����������������߳�Ҳ���Բ����͹����м����ݡ���ʱû��Ҫ�ȵ��߳���ɾͿ��Ի���м�����
//					ʹ��publish����������Ҫ������м�����
					publish(line);
				}
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			 �ڻ��ִ�н����Ӧʹ��SwingWorker��get������ȡdoInBackground�����Ľ������get������һֱ��������״̬��ֱ�������߳���ɡ�
//			���淵��������ͳ��count
			return (count) + " ";
		}

		@Override
//		 SwingWorker��doInBackground����������Ų�����������������߳�Ҳ���Բ����͹����м����ݡ���ʱû��Ҫ�ȵ��߳���ɾͿ��Ի���м�����
//	      �м����������߳��ڲ��������֮ǰ���ܲ��������ݡ��������߳�ִ��ʱ�������Է�������ΪV���м�����ͨ������process�����������м�����
//		 ���SwingWorkerͨ��publish������һЩ���ݣ���ôҲӦ��ʵ��process������������Щ�м���
//		��ô�������϶��Ž��������ѽ������List
		protected void process(List<String> chunks) {
//			Ҫ��������,:chunks��˼�������line��,�ǲ���List�͵�chunks
			for(String line:chunks)
				tm.addRow(new String[] {(count++) + " ", line, line });
		}
	}
}
