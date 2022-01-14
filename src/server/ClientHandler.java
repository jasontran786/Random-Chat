package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;

import client.GUIChat;

public class ClientHandler implements Runnable{

	private String name;
    private Socket socket;
    BufferedReader in;
    BufferedWriter out;
    String targetName;

    public ClientHandler() {}
    
    public ClientHandler(Socket s) {
        this.socket = s;
        try {
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			 this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		} catch (IOException e) {
				System.err.println(e);
		}
    }
	@Override
	public void run() {
					String nameisOk = "notOK", name="";
					while (!nameisOk.equals("ok")) {
							name = ReceiveFromClient();
						if (name.equals("client_canceled")) {
							System.out.println("Client canceled"); return;
						}
						if (checkName(name)) {
								nameisOk = "ok"; 
						}
						sendToClient(nameisOk);
					}
					System.out.println("Client success login. Name : "+name);
					this.name=name;
					Server.lstClient.add(this);
					
			if (Server.lstClient.size()>1) {
					//show danh sách
					String kq="";
					for (ClientHandler client:Server.lstClient) {
						if (!name.equals(client.name)) 
							kq+= (client.name + ";");
					}
					sendToClient(kq);
				System.out.println(kq);
					//báo client online
					sendToAllCilent("online;"+this.name);
			}else {
					sendToClient("clientDau");
			}
		if (Server.lstClient.size()>2) {
			int ranchat = Server.lstClient.size();
			Random rand = new Random();
			int rannum=rand.nextInt(ranchat);
			System.out.println("Random num is:"+rannum);
			sendToClient("random;"+rannum);
		}else {
			sendToClient("clientDau");
		}
			String yeuCauChat = "^chat;.+$";
			String traLoi = "^reply;.+;.+$";
			while(true) {
					String command = ReceiveFromClient();
				System.out.println("recived:" + command);
					if (command!=null) {
							if (command.equals("exit")) {
								if(Server.lstClient.size()<=1)
								{
									Server.lstClient.setSize(0);
									System.out.println("User "+ this.name+" exit.");
									break;
								}
								else
								{
									sendToAllCilent("exit;"+this.name);
									Server.lstClient.remove(this);
									System.out.println("User "+ this.name+" exit.");
									break;
								}

							}
						if (command.equals("Refresh")) {
							if (Server.lstClient.size()>1) {
								//show danh sách
								String kq="";
								for (ClientHandler client:Server.lstClient) {
									if (!name.equals(client.name))
										kq+= (client.name + ";");
								}
								sendToClientRefresh("online;"+kq);
								System.out.println(kq);
							}else {
								sendToClient("clientDau");
							}
						}
							if (command.matches(yeuCauChat)) {
									targetName = command.split(";")[1];
									sendToOtherClient(targetName,"connect;"+name);
							}
							if(command.matches(traLoi)) {
									String cauTraLoi = command.split(";")[1];
									targetName = command.split(";")[2];
									sendToOtherClient(targetName, cauTraLoi);
							}
							if(command.equals("Open-chat")) {
									while(true) {
									try {
										String msg = in.readLine();
										if (msg != null) {
												for (ClientHandler client:Server.lstClient) {

														if (!name.equals(client.name)) {
															if (msg.equals("end")) {
																System.out.println("Client Handel: Closed chat");
																break;
															}
																client.out.write(this.name+": "+msg+"\n");client.out.flush();
														}
												}

										}
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
							}
					}

			}
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
//	}
	public boolean checkName(String name) {
			for (ClientHandler client:Server.lstClient) {
					if (name.equals(client.name)) return false;
			}
			return true;
	}
	private boolean sendToClient(String msg) {
		try {
			out.write(msg);
			out.newLine();
			out.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
}
	private boolean sendToClientRefresh(String msg) {
		try {
			out.write(msg);
			out.newLine();
			out.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	private String ReceiveFromClient() {
		try {
			return in.readLine();
		} catch (IOException e) {
				return null; 
		}
	}
	private void sendToOtherClient(String name,String msg) {
			for (ClientHandler client:Server.lstClient) {	
				if (name.equals(client.name)) client.sendToClient(msg);
			}
	}
	private String ReceiveFromOtherClient(String name) {
			for (ClientHandler client:Server.lstClient) {	
				if (name.equals(client.name)) return client.ReceiveFromClient();
			}
			return null;
	}
	private void sendToAllCilent(String msg) {
		for (ClientHandler client:Server.lstClient) {	
			if (!name.equals(client.name))
				try {
					client.out.write(msg+"\n");client.out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
