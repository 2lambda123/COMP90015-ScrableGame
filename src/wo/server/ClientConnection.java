package wo.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class ClientConnection extends Thread {
	private Socket clientSocket;
	private BufferedReader reader; 
	private BufferedWriter writer; 
	private int clientNum;

	public ClientConnection(Socket clientSocket, int clientNum) {
		try {
			this.clientSocket = clientSocket;
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			this.clientNum = clientNum;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		try {
			System.out.println(Thread.currentThread().getName() + " - Reading instructions from client's " + clientNum + " connection");
			ServerState ss = ServerState.getInstance();
			User u1 = new User("jack","111","11",0);
			User u2 = new User("tom","112","12",0);
			User u3 = new User("jerry","113","13",0);
			List<User> l = ss.getUsers();
			l.add(u1);
			l.add(u2);
			l.add(u3);
			for(int i = 0; i<l.size();i++) {
				User u = l.get(i);
				broadWrite("connection"+" "+u.getUsername()+" "+u.getIpAddress()+" "+u.getPort()+" "+u.getScore()+"\n");
			}
			String clientMsg = null;
			while ((clientMsg = reader.readLine()) != null) {
				System.out.println(Thread.currentThread().getName() + " - Instruction from client " + clientNum + " received: " + clientMsg);
				String[] Msg = clientMsg.split(" ");
				String instruction = Msg[0];
				if (instruction.equals("add")) {
					add(Msg);
				}
//				else if (instruction.equals("add")) {
//					add(Msg);
//				} else if (instruction.equals("remove")) {
//					remove(Msg);
//				}
				
			}
			clientSocket.close();
			ss.clientDisconnected(this);
			System.out.println(Thread.currentThread().getName() + " - Client " + clientNum + " disconnected");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//Needs to be synchronized because multiple threads can me invoking this method at the same time
	public synchronized void broadWrite(String msg) {
		try {
			List<ClientConnection> clients = ServerState.getInstance().getConnectedClients();
			for(ClientConnection client : clients) {
				client.writer.write(msg);
				client.writer.flush();
			}
//			writer.write(msg);
//			writer.flush();
			System.out.println(Thread.currentThread().getName() + " - Message sent to client " + clientNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public synchronized void add(String[] Msg) {
		int row = Integer.parseInt(Msg[1]);
		int col = Integer.parseInt(Msg[2]);
		String letter = Msg[3];
		int index = row*20+col;
		System.out.println(index);
		broadWrite("add"+" "+index+" "+letter+"\n");
		//Broadcast the client message to all other clients connected
		//to the server.
//		List<ClientConnection> clients = ServerState.getInstance().getConnectedClients();
//		for(ClientConnection client : clients) {
//			client.write("add"+" "+index+" "+letter+"\n");
//		}
	}
}