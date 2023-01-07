
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Client {
	
	static String serverIP;
	
	
	public static void main(String[] args) throws Exception{
		
		MainFrame mainFrame=new MainFrame();
		mainFrame.setVisible(true);
		
		serverIP=Client.findServer();
		
		JOptionPane.showMessageDialog(null,"Server IP Address has been found: "+serverIP,"",JOptionPane.INFORMATION_MESSAGE);
		mainFrame.setPanel("login");

	}

	public static String findServer()  throws Exception{
		DatagramSocket dS = new DatagramSocket(6789);
		
		byte[] receiveData = new byte[1024];
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		dS.receive(receivePacket);
		
		return receivePacket.getAddress().getHostAddress();
	}
	
	
	public static String getServerIp() {
		
		return serverIP;
	}
	
	public static void setServerIp(String ip) {
		
		serverIP=ip;
	}
			
}


	
