import java.io.*;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CommandHandler extends Thread {

	Socket clientSocket;
	DataInputStream dis;
	DataOutputStream dos;
	String command;
	ProxyServer proxy;
	BufferedReader reader;
	BufferedWriter writer;
	FileOutputStream fos;
	FileInputStream fis;
	ByteArrayOutputStream baos; 
	DataOutputStream dos2;
	
	
	public CommandHandler(Socket clientSocket) throws Exception {
		
		this.clientSocket = clientSocket;
		dis = new DataInputStream(clientSocket.getInputStream());
		dos = new DataOutputStream(clientSocket.getOutputStream());
		baos = new ByteArrayOutputStream();
		dos2 = new DataOutputStream(baos);
	}
	
	public void run() {
		
		try {
			
			byte[] arr=new byte[4];
			dis.read(arr);
			command=new String(arr);
			
			if(command.equals("strt")) {
				System.out.println("Proxy started.");
				
				Main.getBroadcast().setServerStatus("open");
				
				proxy=new ProxyServer(80);
				Main.getBroadcast().setProxyHTTP(proxy);
				proxy.start();
				
				proxy=new ProxyServer(443);
				Main.getBroadcast().setProxyHTTP(proxy);
				proxy.start();
	
			}
			else if(command.equals("stop")) {
				System.out.println("Proxy stopped.");
				
				Main.getBroadcast().setServerStatus("clsd");
				Main.getBroadcast().getProxyHTTP().serverSocket.close();
				Main.getBroadcast().getProxyHTTPS().serverSocket.close();
				
			}
			else if(command.equals("rprt")) {
				int flag;
				arr=new byte[4];
				String ip="";
				dis.read(arr);
				flag=ByteBuffer.wrap(arr).getInt();
				
				if(flag!=0) {
					arr=new byte[flag];
					dis.read(arr);
					ip=new String(arr);
				}
				
				String report="";
				File f = new File(System.getProperty("user.dir")+"/report.txt");
				
				if( f.exists() && !f.isDirectory()) {
					
					if (flag==0) {
							FileInputStream fis=new FileInputStream(System.getProperty("user.dir")+"/report.txt");
							byte[] rep= fis.readAllBytes();
							report= new String(rep);
							System.out.println("Report sent regarding the IP address: all");
					}
					else {
						reader=new BufferedReader(new FileReader(System.getProperty("user.dir")+"/report.txt"));
						String line;
						
						while((line = reader.readLine()) != null) {
							int sp = line.indexOf(':');
							String ip2 = line.substring(0,sp);
							if (ip2.equals(ip)) {
								report+=line+"\n";
							}
							
						}
						reader.close();
						System.out.println("Report sent regarding the IP address:"+ip);
					}
					
					
					int len=report.length();
					dos2.writeInt(len);
					dos2.writeBytes(report);
					dos.write(baos.toByteArray());
					
					
				}
				else {
					int temp=0;
					dos2.writeInt(temp);
					dos.write(baos.toByteArray());
				}
				
				dos2.close();
				dos.close();
				
			}
			else if(command.equals("disp")) {
				File f = new File(System.getProperty("user.dir")+"/filteredHosts.txt");
				
				if( f.exists() && !f.isDirectory()) {
					
					fis=new FileInputStream(System.getProperty("user.dir")+"/filteredHosts.txt");
					arr=new byte[(int) f.length()];
					fis.read(arr);
					dos2.writeInt((int)f.length());
					dos2.write(arr);
					dos.write(baos.toByteArray());
		
					fis.close();
					System.out.println("Filtered Hosts are sent.");
				}
				else {
					int temp=0;
					dos2.writeInt(temp);
					dos.write(baos.toByteArray());
				}
				dos2.close();
				dos.close();
				
			}
			else if(command.equals("addd")) {
				
				arr=new byte[4];
				dis.read(arr);
				arr=new byte[ByteBuffer.wrap(arr).getInt()];
				dis.read(arr);
				String host=new String(arr);
				File f = new File(System.getProperty("user.dir")+"/filteredHosts.txt");
				
				if( f.exists() && !f.isDirectory()) {
					reader=new BufferedReader(new FileReader(System.getProperty("user.dir")+"/filteredHosts.txt"));
					
					int flag=0;
					String line;
					while((line = reader.readLine()) != null) {
						
						if (line.equals(host)) {
							flag=1;
							break;
						}
					}
					if(flag == 0) {
						fos=new FileOutputStream(System.getProperty("user.dir")+"/filteredHosts.txt",true);
						fos.write(arr);
						fos.write('\n');
						fos.close();
						System.out.println("New host added to filtered list: "+ host);
					}
					else if(flag == 1 ) {
						System.out.println("Host is already in the filtered list: "+ host);
					}
				}
				else {
					f.createNewFile();
					System.out.println("filteredHosts.txt file created.");
					
					fos=new FileOutputStream(System.getProperty("user.dir")+"/filteredHosts.txt",true);
					fos.write(arr);
					fos.write('\n');
					fos.close();
					System.out.println("New host added to filtered list: "+ host);
				}
				
			}
			
			else if (command.equals("auth")) {
				
				System.out.println("Login request received.");
				
				arr=new byte[4];
				dis.read(arr);
				arr=new byte[ByteBuffer.wrap(arr).getInt()];
				dis.read(arr);
				String user=new String(arr);
				String type="";
				String password="";
				
				if(Main.getAdminInfo().containsKey(user)){
					type="admin";
					password=Main.getAdminInfo().get(user);
					
					
				}
				else if(Main.getUserInfo().containsKey(user)) {
					type="client";
					password=Main.getUserInfo().get(user);
				}
				else {
					type="guest";
					password="abcd";
					
				}
				if(NetworkInterface.getByInetAddress(clientSocket.getInetAddress()) != null)  
					Main.getipInfo().put("127.0.0.1",type);
				
				else
					Main.getipInfo().put(clientSocket.getInetAddress().getHostAddress(),type);
				
				byte pass[]=new byte[16];
				System.arraycopy(password.getBytes(),0,pass,0,password.getBytes().length);
				byte [] x=encrypt(type,pass);
				dos.write(x);
				
				
			}
			else if(command.equals("stat")) {
				
				dos.write(Main.getBroadcast().getServerStatus().getBytes());
			}
	
			 clientSocket.close();
			 dos.close();
			 dos2.close();
			 dis.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	public byte[] encrypt(String data,byte[] password) {
		
		try {
			SecretKey key= new SecretKeySpec(password,"AES");
			
			
			byte ivArr[] = "1234567812345678".getBytes();
			 
			IvParameterSpec iv = new IvParameterSpec(ivArr);
			
			Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ci.init(Cipher.ENCRYPT_MODE, key, iv);	
			byte[] a=ci.doFinal(data.getBytes());
			
			return  a;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
