import java.net.ServerSocket;
import java.net.Socket;


public class ProxyServer extends Thread{
	
	ServerSocket serverSocket;
	int port;
	
	public ProxyServer(int port) {
		
		this.port=port;
	}
	
	
	public void run() {
		try {
			
			serverSocket = new ServerSocket(port);
			
			while (true) {
				
				Socket clientSocket = serverSocket.accept();
				
				if(port==80) {
					
					new HTTPHandler(clientSocket).start();
				}
				
				else if(port==443) {
					new HTTPSHandler(clientSocket).start();
				}
				
				
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
