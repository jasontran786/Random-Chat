package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Client {
    private static String host = "localhost";
    private static int port = 1234;
    private static Socket socket;

    private static BufferedWriter out;
    private static BufferedReader in;

    public static void main(String[] args) throws IOException, InterruptedException {
        socket = new Socket(host, port);
        System.out.println("Client connected");   
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //nhập tên và kiểm tra
        String nameisOk = "notok";String name="";
        while ( !nameisOk.equals("ok") ) {
	        	name = JOptionPane.showInputDialog(null,"Name: ");
	        	if ("".equals(name)) {
	        			JOptionPane.showMessageDialog(null, "Empty is not allowed");
	        			continue;
	        	}
	        	//nếu nhấn cancel hoặc close
	        	if(name == null )  {
	        			out.write("client_canceled\n");
	        			out.flush();
	        			System.exit(0);
	        	}else {
	        			out.write(name+"\n");
	        			out.flush();
	        	}
	            
	            nameisOk = in.readLine();
	            if(!nameisOk.equals("ok")) JOptionPane.showMessageDialog(null, "Duplicated");
        }
       
        
        PhongCho wait= new PhongCho(name,in,out);
        wait.frmPhngCh.setVisible(true);
        
//	        socket.close();
//	        in.close();
//	        out.close();
    }
    
}
