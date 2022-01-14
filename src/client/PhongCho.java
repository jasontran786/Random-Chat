package client;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.FlowLayout;

public class PhongCho {

	public JFrame frmPhngCh;
	private JTable jtbDanhSach;
	BufferedReader in;
    BufferedWriter out;
    DefaultTableModel tableModel;
    ExecutorService executor;
    
    String name = "";
	String add ="^online;.+$";
	String remove = "^exit;.+$";
	String connectRes = "^connect;.+$";
	String random = "^random;.+$";
	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					PhongCho window = new PhongCho();
//					window.frmPhngCh.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the application.
	 */
	public PhongCho(String name,BufferedReader in,BufferedWriter out) {
			this.in=in;
			this.out=out;
			this.name=name;
			initialize();
			System.out.println("User "+name+" In lobby");
			String lstName = ReceiveFromServer();
			if (!lstName.equals("clientDau") ) {
					StringTokenizer st = new StringTokenizer(lstName,";");
					while (st.hasMoreTokens()) {
							tableModel.addRow(new Object[] {st.nextToken()});
					}
			}
			Waiting wait = new Waiting();
			executor= Executors.newFixedThreadPool(1);
			executor.execute(wait);
	}
	public PhongCho() {
			initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPhngCh = new JFrame();
		frmPhngCh.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
					//executor.shutdown();

					sendToServer("exit");
					System.out.flush();
					executor.shutdown();
					System.exit(0);


			}
		});
		frmPhngCh.setTitle(name + " Waiting");
		frmPhngCh.setBounds(100, 100, 286, 471);
		frmPhngCh.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPhngCh.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 272, 434);
		frmPhngCh.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 30, 252, 394);
		panel.add(scrollPane);
		
		jtbDanhSach = new JTable(tableModel);
		jtbDanhSach.setRowMargin(2);
		jtbDanhSach.setRowHeight(20);
		jtbDanhSach.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
							JTable target = (JTable)e.getSource();
							int index = target.getSelectedRow();
							String targetName = (String)jtbDanhSach.getValueAt(index, 0);
							int input = JOptionPane.showConfirmDialog(frmPhngCh, 
					                "Do you want to chat with "+targetName, "Customized Dialog",
					                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);		
							if (input==0){
								//choose ok
								sendToServer("chat;"+targetName);
							}
					}
			}
		});
		scrollPane.setViewportView(jtbDanhSach);
		jtbDanhSach.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		jtbDanhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jtbDanhSach.setColumnSelectionAllowed(true);
		jtbDanhSach.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		tableModel = new DefaultTableModel(
	            new Object [][] {
	            },
	            new String [] {
	                "Online User"
	            }
	        ) {
	            boolean[] canEdit = new boolean [] {
	                false
	            };
	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        };

		jtbDanhSach.setModel(tableModel);
		
		JLabel lblNewLabel = new JLabel("Double click to invite");
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblNewLabel.setBounds(10, 10, 252, 20);
		panel.add(lblNewLabel);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.setColumnCount(0);
				tableModel.setRowCount(0);
				tableModel.addColumn("Online User");
				sendToServer("Refresh");
			}
		});
		btnRefresh.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnRefresh.setBounds(160, 10, 100, 20);
		panel.add(btnRefresh);
	}
	private boolean sendToServer(String msg) {
			try {
				out.write(msg);
				out.newLine();
				out.flush();
				return true;
			} catch (IOException e) {
				return false;
			}
	}
	private String ReceiveFromServer() {
			try {
				return in.readLine(); 
			} catch (IOException e) {
					return null; 
			}
}
	public class Waiting implements Runnable{
		//int solan =0;
		@Override
		public void run() {
			while (true) {
					String capNhat = ReceiveFromServer();
					//System.out.println(capNhat+"   "+( solan++));
					if (capNhat!=null) {
							if(capNhat.matches(connectRes)) {
									String targetName = capNhat.split(";")[1];
									int input = JOptionPane.showConfirmDialog(frmPhngCh, 
							                targetName+" wants to chat with you", "Customized Dialog",
							                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);	
									String traLoi = "reply;";
									if (input==0){
											sendToServer(traLoi+"ok;"+targetName);
											GUIChat gui = new GUIChat(name, in, out);
									        gui.frame.setVisible(true);
									        sendToServer("Open-chat");
									}else {
											sendToServer(traLoi+"Notok;"+targetName);
									}
										
							}
							if(capNhat.equals("Notok")) JOptionPane.showMessageDialog(frmPhngCh, "Denied");
							if (capNhat.equals("ok")) {
							        GUIChat gui = new GUIChat(name, in, out);
							        gui.frame.setVisible(true);
							        sendToServer("Open-chat");
							}
							if (capNhat.matches(add)) {
									//String name = capNhat.split(";")[1];
									StringTokenizer st = new StringTokenizer(capNhat,";");
									tableModel.setRowCount(0);
									while(st.hasMoreTokens())
									{
										String name = st.nextToken();
										if (name!=null){
											{
												if (!name.equals("online"))
												{
													tableModel.addRow(new Object[] { name });
												}
											}
										}
									}
							}
							if(capNhat.matches(remove)) {
									String removeName =capNhat.split(";")[1] ;
									if (removeName!=null) {
											int n = jtbDanhSach.getRowCount();
											for (int i=0;i<n;i++) {
													String name = (String)jtbDanhSach.getValueAt(i, 0);
													if (name.equals(removeName)) {
														tableModel.removeRow(i);break;
													}
											}
									}
							}
							if(capNhat.matches(random)){
								String randomName = capNhat.split(";")[1];
								if(randomName!=null){
									int n = jtbDanhSach.getRowCount();
									int b = Integer.parseInt(randomName);
									for (int i=0;i<n;i++) {
										String name = (String)jtbDanhSach.getValueAt(i, 0);
										if (name.equals((String)jtbDanhSach.getValueAt(b,0) ))
										{
											String targetName = (String)jtbDanhSach.getValueAt(b, 0);
											int input = JOptionPane.showConfirmDialog(frmPhngCh,
													"Do you want to chat with "+targetName, "Customized Dialog",
													JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
											if (input==0){
												//choose ok
												sendToServer("chat;"+targetName);
											}
										}
									}
								}
							}
					}
			 }
		}
	}
}
