package client;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Color;
import javax.swing.JTextArea;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.DropMode;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;
import javax.swing.JLabel;

public class GUIChat {

	public JFrame frame;
	private JTextField txtMsg;
	BufferedReader in;
    BufferedWriter out;
    public JTextArea msgArea;
    ExecutorService executor;
    private ArrayList<String> lstUserOnline = new ArrayList<>();

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					GUIChat window = new GUIChat();
//					window.frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the application.
	 */
	public GUIChat(String n,BufferedReader in,BufferedWriter out) {
			this.in=in;
			this.out=out;
			initialize(n+" Room chat");
			//thêm thread để liên tục nhận tin
			executor = Executors.newFixedThreadPool(1);
			ReceiveMessage recv = new ReceiveMessage(in);
			executor.execute(recv);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String Frametitle) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					out.flush();
					System.out.flush();
					frame.dispose();
					System.out.print("GUI: Chat closed");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		});
		frame.setTitle(Frametitle);
		frame.setBounds(100, 100, 800, 582);
		//mở lên giữa màn hình
		openCenterScreen(frame);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.GRAY);
		panel.setForeground(Color.BLACK);
		panel.setBounds(10, 10, 766, 467);
		frame.getContentPane().add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		msgArea = new JTextArea();
		msgArea.setMargin(new Insets(10, 10, 10, 10));
		msgArea.setDropMode(DropMode.INSERT);
		msgArea.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		msgArea.setEditable(false);
		//tự động xuống dòng
		autoBreakLine(msgArea);
		//tạo thanh cuộn xuống
		JScrollPane jSP = new JScrollPane(msgArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
																				 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(jSP);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.GRAY);
		panel_1.setBounds(10, 487, 766, 48);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		txtMsg = new JTextField();
		txtMsg.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							String textMsg = txtMsg.getText().trim();
							if ( checkAdd(textMsg) ) {
								//send tin nhắn lên server
								if ( sendToServer(textMsg) )
								{
									//thay đổi form
									msgArea.append("You: "+textMsg+"\n");
									txtMsg.setText("");
								}
							}else {
									JOptionPane.showMessageDialog(null, "Failed");
							}
					}
			}
		});
		txtMsg.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		txtMsg.setBounds(0, 0, 627, 48);
		panel_1.add(txtMsg);
		txtMsg.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					String textMsg = txtMsg.getText().trim();
					if ( checkAdd(textMsg) ) {
						if ( sendToServer(textMsg) )
						{
							msgArea.append("You: "+textMsg+"\n");
							txtMsg.setText("");
						}else {
							JOptionPane.showMessageDialog(null, "Failed");
						}
					}
			}
		});
		btnSend.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnSend.setBounds(626, -2, 70, 48);
		panel_1.add(btnSend);

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//sendToServer("end");
				try {
					out.flush();
					System.out.flush();
					out.close();in.close();executor.shutdown();
					sendToServer("end");
					frame.dispose();
					System.out.print("GUI: Chat closed");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		});
		btnClose.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnClose.setBounds(696, -2, 70, 48);
		panel_1.add(btnClose);
	}
	private boolean checkAdd(String text) {
		    if (text.isEmpty() || text.equals("") ) return false;
			return true;
	}
	private void openCenterScreen(JFrame frame) {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
	}
	private void autoBreakLine(JTextArea textArea) {
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
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
			String data;
			try {
				data = in.readLine();
				return data; 
			} catch (IOException e) {
					return null; 
			}
	}
	class ReceiveMessage implements Runnable {
	    private BufferedReader in;
	    public ReceiveMessage(BufferedReader i) {
	        this.in = i;
	    }
	    public void run() {
	        try {
	            while(true) {
	                String data = in.readLine();
	                String str = data.split(":") [1];
	                if(str.equals("end"))
					{
						frame.dispose();
					}
	                msgArea.append(data+"\n");
	            }
	        } catch (IOException e) {}
	    }
	}
	public void addButton(JPanel panel,String name) {
			JButton btnNewButton = new JButton(name);
			panel.add(btnNewButton);
	}
}
