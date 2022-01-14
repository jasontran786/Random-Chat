package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.swing.JOptionPane;




public class Server {
	private Socket socket=null;
	private ServerSocket server=null;
	public static Vector<ClientHandler> lstClient = new Vector<>();
	 LocalDateTime now = LocalDateTime.now();  

	//tạo server đợi client
	public Server(int port) {
				//tạo server đợi client
				try {
						server = new ServerSocket(port);
				}catch ( BindException e ){
						JOptionPane.showMessageDialog(null, "Server is already opened");
						System.exit(0);
				} catch (IOException e1 ) {
						System.err.print("Error: "+e1);
				} 
				System.out.println("Server is running");
				boolean flag=true;
				ExecutorService executor = Executors.newFixedThreadPool(5);
				while(flag==true) {
							try {
								socket = server.accept();   
								System.out.println("Client connected "+now);
								ClientHandler client = new ClientHandler(socket);
								executor.execute(client);
								lstClient.toString();
							} catch (IOException e) {
								System.err.print("Client disconnected "+now);
							}
				}
				executor.shutdown();
				//xong hết rồi đóng hết để giải phóng tài nguyên
				try {
					server.close();
					socket.close();
					System.out.print("Server off");
				} catch (IOException e) {
						System.err.print("Can't close socket,server,inout");
				}
	}
	
	public static void main(String[] args) {
		Server ser=new Server(1234);
	}
}
