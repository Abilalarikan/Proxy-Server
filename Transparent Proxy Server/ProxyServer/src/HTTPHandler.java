import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class HTTPHandler extends Thread {

	Socket clientSocket;
	DataInputStream dis;
	DataOutputStream dos;

	public HTTPHandler(Socket clientSocket) throws Exception {
		this.clientSocket = clientSocket;
		dis = new DataInputStream(clientSocket.getInputStream());
		dos = new DataOutputStream(clientSocket.getOutputStream());
	}

	@Override
	public void run() {

		try {
			System.out.println("-------------------------------------");
			System.out.println("-------------------------------------");
			System.out.println("NEW HTTP CONNECTION");
			byte[] headerArr = new byte[5000];
			int hc = 0;

			// only for header part
			while (true) {
				byte i = (byte) dis.read();
				headerArr[hc++] = i;
				if (headerArr[hc - 1] == '\n' && headerArr[hc - 2] == '\r' && headerArr[hc - 3] == '\n'
						&& headerArr[hc - 4] == '\r') { // \r\n\r\n
					break;
				}

			}

			String header = new String(headerArr, 0, hc);
			System.out.println("-------HEADER FROM CLIENT----");
			System.out.println(header);

			// GET / HTTP/1.1\r\n
			// Host: asd.com
			//
			int fsp = header.indexOf(' ');
			int ssp = header.indexOf(' ', fsp + 1);
			int eol = header.indexOf("\r\n");

			String methodName = header.substring(0, fsp);

			String restHeader = header.substring(eol + 2);

			String modHeader = restHeader;

			if (modHeader.contains("Proxy-Connection")) {
				int proxIndex = modHeader.indexOf("Proxy-Connection");
				int eolProxIndex = modHeader.indexOf("\r\n", proxIndex);

				modHeader = modHeader.substring(0, proxIndex) + modHeader.substring(eolProxIndex + 2);
			}

			if (modHeader.contains("Cookie: Unwanted Cookie")) {
				int cookieIndex = modHeader.indexOf("Cookie: Unwanted Cookie");
				int eolCookieIndex = modHeader.indexOf("\r\n", cookieIndex);

				modHeader = modHeader.substring(0, cookieIndex) + modHeader.substring(eolCookieIndex + 2);
			}



			String path = header.substring(fsp + 1, ssp);
			int hostIndex = modHeader.indexOf("Host");
			int eolHostIndex = modHeader.indexOf("\r\n", hostIndex);
			String domain=modHeader.substring(hostIndex+6,eolHostIndex);
			System.out.println("domain: " + domain);
			System.out.println("path: " + path);

			if (methodName.equals("GET") || methodName.equals("OPTIONS") || methodName.equals("HEAD")) {
				
				if (isFiltered(domain) && !Main.getipInfo().get(clientSocket.getInetAddress().getHostAddress()).equals("admin") ) {
					Main.writeReport(clientSocket.getInetAddress().getHostAddress(),domain+path,methodName,401);
					sendNotAuthorized(dos,domain);
					
					System.out.println( "Connection: "+domain+" denied.");
					

				} else {

					handleProxy(methodName, modHeader, null, domain, path);

				}

			} else if (methodName.equals("POST")) {
				
				if (isFiltered(domain) && !Main.getipInfo().equals("admin")) {
					Main.writeReport(clientSocket.getInetAddress().getHostAddress(),domain+path,methodName,401);
					sendNotAuthorized(dos,domain);
					System.out.println( "Connection: "+domain+" denied.");

				} else {

					int contIndex = header.indexOf("Content-Length: ");
					int eol2 = header.indexOf("\r\n", contIndex);
					String contSize = header.substring(contIndex + 16, eol2);
					int contSizeInt = Integer.parseInt(contSize);

					System.out.println("Header from client ContLength: " + contSizeInt);

					byte[] headerPayload = new byte[contSizeInt];

					byte[] buffer = new byte[1024];

					int sum = 0;
					int read;

					while (sum < contSizeInt) {
						read = dis.read(buffer);
						System.arraycopy(buffer, 0, headerPayload, sum, read);
						sum += read;
					}

					handleProxy(methodName, modHeader, headerPayload, domain, path);
				}
				

			} else {

				String html =	"<html>\r\n" +
									"<head>\r\n" +
										"<title>405 Method Not Allowed</title>\r\n" +
									"</head>\r\n" +
									"<body>\r\n" +
										"<h1>405 Method Not Allowed</h1>\r\n" +
										"<h1>The HTTP Method " + methodName + " is not allowed</h1>\r\n" +
									"</body>\r\n" +
								"</html>\r\n";

				String response =	"HTTP/1.1 405 Method Not Allowed\r\n" +
									"Server: CSE471Proxy\r\n" +
									"Content-Type: text/html\r\n" +
									"Content-Length: " + html.length() + "\r\n" +
									"\r\n" +
									html;
				Main.writeReport(clientSocket.getInetAddress().getHostAddress(),domain+path,methodName,405);
				dos.writeBytes(response);

			}

			System.out.println("HANDLED CLIENT " + clientSocket.getInetAddress().getHostAddress() +
					" FOR ADDRESS " + domain+path);

		} catch (Exception e) {
			
			String html =	"<html>\r\n" +
					"<head>\r\n" +
						"<title>400 Bad Request</title>\r\n" +
					"</head>\r\n" +
					"<body>\r\n" +
						"<h1>400 Bad Request</h1>\r\n" +
					"</body>\r\n" +
				"</html>\r\n";

			String response =	"HTTP/1.1 401 Not Authorized\r\n" +
					"Server: CSE471Proxy\r\n" +
					"Content-Type: text/html\r\n" +
					"Content-Length: " + html.length() + "\r\n" +
					"\r\n" +
					html;

			try {
				dos.writeBytes(response);
			} catch (IOException er) {
			
				er.printStackTrace();
			}
			e.printStackTrace();
		}

	}

	private void handleProxy(String methodName, String restHeader, byte[] headerPayload, String domain,
			String shortpath) throws Exception {

		byte[] ipArr =  Main.findIP(domain);
		
		InetAddress ip = InetAddress.getByAddress(ipArr);
		
		Socket proxiedSocket = new Socket( ip, 80 );

		DataInputStream dis1 = new DataInputStream(proxiedSocket.getInputStream());
		DataOutputStream dos1 = new DataOutputStream(proxiedSocket.getOutputStream());
		
		
		// request sent to web server
		String constructedHeader;
		File f = new File(System.getProperty("user.dir")+"/"+ domain +".txt");
		if (f.exists()) {
			
			FileInputStream fis=new FileInputStream(System.getProperty("user.dir")+"/"+ domain + ".txt");
			byte[] lastM=new byte[29];
			fis.read(lastM);
			
			String lastModified=new String(lastM);
			fis.close();
			constructedHeader = methodName + ' ' + shortpath + " HTTP/1.1\r\n" + "If-Modified-Since: " + lastModified + "\r\n" + restHeader;
		}
		else {
			
			constructedHeader = methodName + ' ' + shortpath + " HTTP/1.1\r\n" + restHeader;
		}
		System.out.println("-------HEADER TO WEBSERVER----");
		System.out.println(constructedHeader);

		dos1.writeBytes(constructedHeader);

		if (methodName.equals("POST") && headerPayload != null) {
			dos1.write(headerPayload);
		}

		

		// NOW READ HTTP RESPONSE FROM WEBSERVER

		// byte array for HTTP Response header
		byte[] reponseHdrArr = new byte[5000];
		int rc = 0;

		// only for response header part
		while (true) {
			byte i = (byte) dis1.read();
			reponseHdrArr[rc++] = i;
			if (reponseHdrArr[rc - 1] == '\n' && reponseHdrArr[rc - 2] == '\r' && reponseHdrArr[rc - 3] == '\n'
					&& reponseHdrArr[rc - 4] == '\r') { // \r\n\r\n
				break;
			}

		}

		System.out.println("-------RESPONSE HEADER FROM WEBSERVER----");
		String responseHdr = new String(reponseHdrArr, 0, rc);
		System.out.println(responseHdr);
		
		int fsp = responseHdr.indexOf(' ');
		int ssp = responseHdr.indexOf(' ', fsp + 1);
		String strStatusCode=responseHdr.substring(fsp + 1, ssp);
		int statusCode=Integer.parseInt(strStatusCode);
		
		System.out.println("Status code:"+statusCode+"\n");
		
		if(statusCode==304) {
			Main.writeReport(clientSocket.getInetAddress().getHostAddress(),domain+shortpath,methodName,statusCode);
			
			FileInputStream fis=new FileInputStream(System.getProperty("user.dir")+"/"+ domain + ".txt");
			byte[] a=new byte[29];
			fis.read(a);
			System.out.println(new String(a));
			a=new byte[4];
			fis.read(a);
			
			int contentLength= ByteBuffer.wrap(a).getInt();
		
			int eol= responseHdr.indexOf("\r\n");
			responseHdr="HTTP/1.1 200 OK\nContent-Length: "+contentLength+responseHdr.substring(eol+1);
			byte[] str=responseHdr.getBytes();
			dos.write(str);
		
			a=new byte[contentLength];
			fis.read(a);
			fis.close();
			// payload part of the response back to client sent by using cache
			dos.write(a);
			
			dos.flush();
			System.out.println(domain+": Responded from cache.");
		}
		else if(statusCode == 200) {
			Main.writeReport(clientSocket.getInetAddress().getHostAddress(),domain+shortpath,methodName,statusCode);
			if ( responseHdr.contains("Content-Length: ") || responseHdr.contains("content-length: ") ){ // if Content-Length header field exists
			
				int contIndex = responseHdr.indexOf("Content-Length: ");
				
				if (contIndex==-1){
					contIndex = responseHdr.indexOf("content-length: ");
				}
				int eol = responseHdr.indexOf("\r\n", contIndex);
				String contSize = responseHdr.substring(contIndex + 16, eol);
				int contSizeInt = Integer.parseInt(contSize);
				
				System.out.println("FOUND DATA SIZE IN RESPONSE: " + contSizeInt);
				
				
				if(contSizeInt <= 524288000) { //contSize <= 500 MB
					
					byte[] payload = new byte[contSizeInt];
	
					byte[] buffer = new byte[1024];
	
					int sum = 0;
					int read;
	
					while (sum < contSizeInt) {
						read = dis1.read(buffer);
						System.arraycopy(buffer, 0, payload, sum, read);
						sum += read;
					}
	
					// header part of response back to client
					
					dos.write(reponseHdrArr, 0, rc);
					
					// payload part of the response back to client
					dos.write(payload);
	
					dos.flush();
					
					if (responseHdr.contains("Last-Modified: ")) {
						
						int lastModDateIndex= responseHdr.indexOf("Last-Modified: ");
						
						eol = responseHdr.indexOf("\r\n", lastModDateIndex);
						String lastModDate = responseHdr.substring(lastModDateIndex + 15, eol);
						System.out.println("FOUND LAST MODIFICATIN DATE IN RESPONSE: " + lastModDate);
						
						f = new File(System.getProperty("user.dir")+"/"+ domain + ".txt");
						System.out.println(System.getProperty("user.dir")+"/"+ domain + ".txt");
						if (!f.exists()) {
							f.createNewFile();
						}
							
						FileOutputStream fos=new FileOutputStream(System.getProperty("user.dir")+"/"+ domain + ".txt");
						System.out.println(lastModDate.length());
						fos.write(lastModDate.getBytes());
						fos.write(intToByteArray(payload.length));
						fos.write(payload);
						fos.close();
						System.out.println(domain +": cached");
						
	
					}
					
				}
				else { //if contSize > 500 MB
					
					dos.write(reponseHdrArr, 0, rc);
					
					byte[] buffer = new byte[10240];
	
					int sum = 0;
					int read=0;
					
					while (sum < contSizeInt) {
						read = dis1.read(buffer);
						dos.write(buffer,0,read);
						sum += read;
					}
				}
			}
			else { //if Content-Length header field does not exist
				
					System.out.println("Content-Length field can not be found");
					byte[] payload = new byte[1048576]; //1 MB
					
					byte[] buffer = new byte[102400];
	
					int sum = 0;
					int read;
	
					while ((read = dis1.read(buffer)) != -1) {
						System.arraycopy(buffer, 0, payload, sum, read);
						sum += read;
					}
					
					int eol= responseHdr.indexOf("\r\n");
					responseHdr="HTTP/1.1 200 OK" + "\r\nContent-Length:"+sum+responseHdr.substring(eol+1);
					
					dos.write(responseHdr.getBytes());
					dos.write(payload);
				
				
			}
		}
		else  {
			
			dos.write(reponseHdrArr, 0, rc);
		}
		

		System.out.println();
		System.out.println("SENT HTTP RESPONSE & DATA BACK TO CLIENT");
		System.out.println();

		proxiedSocket.close();

	}
	
	public static boolean isFiltered(String host)  {
		
		try {
			File f = new File(System.getProperty("user.dir")+"/filteredHosts.txt");
			BufferedReader reader;
			
			if( f.exists() && !f.isDirectory()) {
				reader=new BufferedReader(new FileReader(System.getProperty("user.dir")+"/filteredHosts.txt"));
				
				String line;
				while((line = reader.readLine()) != null) {
					
					if (line.equals(host)) {
						reader.close();
						return true;
					}
				}
			}
		
		}
		catch(Exception e) {
			
		}
		
		return false;
	}
	private byte[] intToByteArray ( int i ) throws IOException {  
		
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeInt(i);
	    dos.flush();
	    return bos.toByteArray();
	}
	
	private void sendNotAuthorized(DataOutputStream dos,String domain) {
		
		
		String html =	"<html>\r\n" +
				"<head>\r\n" +
					"<title>401 Not Authorized</title>\r\n" +
				"</head>\r\n" +
				"<body>\r\n" +
					"<h1>401 Not Authorized</h1>\r\n" +
					"<h1>The domain " + domain + " is forbidden</h1>\r\n" +
				"</body>\r\n" +
			"</html>\r\n";

		String response =	"HTTP/1.1 401 Not Authorized\r\n" +
				"Server: CSE471Proxy\r\n" +
				"Content-Type: text/html\r\n" +
				"Content-Length: " + html.length() + "\r\n" +
				"\r\n" +
				html;

		try {
			dos.writeBytes(response);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}

}