import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class HTTPSHandler extends Thread {

	Socket clientSocket;
	DataInputStream dis;
	DataOutputStream dos;
	ByteArrayOutputStream outputStream;
	int numberOfReadBytes;

	public HTTPSHandler(Socket clientSocket) throws Exception {
		
		this.clientSocket = clientSocket;
		dis = new DataInputStream(clientSocket.getInputStream());
		dos = new DataOutputStream(clientSocket.getOutputStream());
		outputStream=new ByteArrayOutputStream();
		numberOfReadBytes=0;
	}

	@Override
	public void run() {
		
		try {	
			System.out.println("-------------------------------------");
			System.out.println("-------------------------------------");
			System.out.println("NEW HTTPS CONNECTION");
			//find Server Name from SNI Extension
			
			int type=readFromDIS(1,false,false);
			
			if (type == 22) {
				
				readOnly(4);
				int handshakeType = readFromDIS(1,false,false);
				
				if ( handshakeType == 1 ){
					
					readOnly(37);
					readFromDIS( 1, true, false );
					readFromDIS( 2, true, false );
					readFromDIS( 1, true, false );
					int extensionLength=readFromDIS( 2, false, false );
					
					while( readFromDIS( 2,false,false ) != 0 ) { //while ExtensionType != 0
						
						readFromDIS( 2, true, false );
					}
					
					readOnly(5);
					int hostLength = readFromDIS( 2, false, false );
					
					byte[] arr= new byte[hostLength];
					dis.read(arr);
					numberOfReadBytes = numberOfReadBytes + hostLength + 2;
					outputStream.write(arr);
					String hostname = new String(arr);
					System.out.println("host: " + hostname);
					int rest =  extensionLength - numberOfReadBytes;
					if (rest != 0) {
						
						arr= new byte[rest];
						dis.read(arr);
						outputStream.write(arr);
					}
						
					byte[] ipArr =  Main.findIP(hostname);
					InetAddress ip = InetAddress.getByAddress(ipArr);
					
					Socket proxiedSocket = new Socket( ip, 443 );
					
					
					System.out.println("Connection Established");
					DataInputStream dis1 = new DataInputStream(proxiedSocket.getInputStream());
					DataOutputStream dos1 = new DataOutputStream(proxiedSocket.getOutputStream());
					

					
					dos1.write(outputStream.toByteArray());
					new HTTPSConnection(dos,dis1,"Server").start();
					new HTTPSConnection(dos1,dis,"Client").start();
					
				}
			}
			
			
		}
		catch(Exception E) {
			E.printStackTrace();
		}
	}
	
	public int readFromDIS(int numberOfBytes,boolean isRead, boolean isCounted) throws Exception {
		
		
		int number=0;
		
		if ( numberOfBytes == 1 ) {
			
			byte a= (byte) dis.read();
			
			outputStream.write(a);
			number=Byte.toUnsignedInt(a);
			
			if ( isCounted )
				numberOfReadBytes += 1;
		}
		else if(numberOfBytes == 2 ) {
			
			byte[] arr=new byte[numberOfBytes];
			dis.read(arr);
			
			outputStream.write(arr);
			number=ByteBuffer.wrap(arr).getShort();
			
			if ( isCounted )
				numberOfReadBytes += 2;
		}
		
		if ( isRead == true) {
			
			byte[] arr=new byte[number];
			dis.read(arr);
			
			outputStream.write(arr);
			
			if ( isCounted )
				numberOfReadBytes += number;
			
		}

		return number;
	}
	
	public  void readOnly(int numberOfBytes) {
		
		
		try {
			
			byte[] arr = new byte[numberOfBytes];
			dis.read(arr);
			
			outputStream.write(arr);
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
}