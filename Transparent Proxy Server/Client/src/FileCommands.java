import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FileCommands {
	
	
	Socket clientSocket;
	DataInputStream dis;
	DataOutputStream dos;
	ByteArrayOutputStream baos; 
	DataOutputStream dos2;
	
	public FileCommands() throws Exception {
		
		clientSocket = new Socket(Client.getServerIp(), 2121);
		
		dis = new DataInputStream(clientSocket.getInputStream());
		dos = new DataOutputStream(clientSocket.getOutputStream());
		baos = new ByteArrayOutputStream();
		dos2 = new DataOutputStream(baos);
	}
	

	
	public  void sendCommandToServer(String command) throws IOException {
		
		String temp="";
		
		if (command.equals("start")) {
			temp="strt";
		}
		
		else if (command.equals("stop")) {
			temp="stop";
		}
		dos.write(temp.getBytes());
	}
	
	public  String sendCommandToServer(String command,String str) throws IOException {
		
		byte[] arr;
		int len;

		if (command.equals("report")) {
			
			dos2.writeBytes("rprt");
			dos2.writeInt(str.length());
			dos2.writeBytes(str);
			dos.write(baos.toByteArray());
			
			arr= new byte[4];
			dis.read(arr);
			len = ByteBuffer.wrap(arr).getInt();

			if(len==0) {
				return "";
			}
			else {
				arr=new byte[len];
				dis.read(arr);
				return new String(arr);
			}
			
		}
		
		else if (command.equals("display")) {
			
			dos.writeBytes("disp");
			
			arr= new byte[4];
			dis.read(arr);
			len = ByteBuffer.wrap(arr).getInt();
			
			if(len==0) {
				return "";
			}
			else {
				arr=new byte[len];
				dis.read(arr);
				return new String(arr);
			}
			
		}
		else if (command.equals("add")) {
			
			dos2.writeBytes("addd");
			dos2.writeInt(str.length());
			dos2.writeBytes(str);
			dos.write(baos.toByteArray());
		}
		else if(command.equals("status")) {
			
			dos.writeBytes("stat");
			arr= new byte[4];
			dis.read(arr);
			
			return new String(arr);
		}
		else {
			dos2.writeBytes("auth");
			dos2.writeInt(command.length());
			dos2.writeBytes(command);
			dos.write(baos.toByteArray());
			arr= new byte[16];
			dis.read(arr);
			byte pass[]=new byte[16];
			
			if(str.equals("")) {
				str="abcd";
			}
			
			System.arraycopy(str.getBytes(),0,pass,0,str.getBytes().length);
			
			String user=decrypt(arr,pass);
			return  user;
		}
		
		return null;
	}
	
	public String decrypt(byte[] arr,byte[] password) {
		
		try {
			SecretKey key= new SecretKeySpec(password,"AES");
			
			
			byte ivArr[] = "1234567812345678".getBytes();
			 
			IvParameterSpec iv = new IvParameterSpec(ivArr);
			
			Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ci.init(Cipher.DECRYPT_MODE, key, iv);	
			byte[] userArr=ci.doFinal(arr);
			
			return new String(userArr);
		}
		catch(Exception e){
			return "wrong";
		}
	}
	
}
