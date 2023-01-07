import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Broadcast extends Thread{

	private String serverStatus="clsd";
	private ProxyServer proxyHTTP;
	private ProxyServer proxyHTTPS;
	private String[] filterList;
	
	public Broadcast() {
		filterList=new String[50];
		this.start();
	}
	public void run() {
		
		try {
				DatagramSocket dSocket;
				dSocket = new DatagramSocket();
				dSocket.setBroadcast(true);
				byte[] buffer = "Server Open".getBytes();
				
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 6789);
			while(true){
				
				dSocket.send(packet);
				
				Thread.sleep(5000);
			}
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getServerStatus() {
		return serverStatus;
	}
	
	public void setServerStatus(String serverStatus) {
		this.serverStatus=serverStatus;
	}
	
	public void setProxyHTTP(ProxyServer proxy) {
		this.proxyHTTP=proxy;
	}
	
	public ProxyServer getProxyHTTP() {
		return this.proxyHTTP;
	}
	
	public void setProxyHTTPS(ProxyServer proxy) {
		this.proxyHTTPS=proxy;
	}
	
	public ProxyServer getProxyHTTPS() {
		return this.proxyHTTPS;
	}
	public String[] getFilterList() {
		
		return this.filterList;
	}
	
}
