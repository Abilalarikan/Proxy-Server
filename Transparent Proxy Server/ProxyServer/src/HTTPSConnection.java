import java.io.*;
import java.nio.ByteBuffer;

public class HTTPSConnection  extends Thread{
	
	DataOutputStream dos;
	DataInputStream dis;
	String s;
	public HTTPSConnection(DataOutputStream dos,DataInputStream dis,String s) {
		
		this.dos=dos;
		this.dis=dis;
		this.s=s;
	}
	
	public void run() {
		
		try {
			byte a;
			while(( a=(byte) dis.read())  != -1 ) {
				dos.write(readFromDIS(a));
				dos.flush();
				
			}
			dos.close();
		} catch (Exception e) {
			System.out.println(s);
			e.printStackTrace();
			
		}
		
	}
	
	public byte[] readFromDIS(byte a) throws Exception {
		
		ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
		outputStream.write(a);
		
		byte[] arr=new byte[2];
		dis.read(arr);
		outputStream.write(arr);
		
		arr=new byte[2];
		dis.read(arr);
		outputStream.write(arr);
		int length=ByteBuffer.wrap(arr).getShort();
		
		arr=new byte[length];
		dis.read(arr);
		outputStream.write(arr);	
	
		return outputStream.toByteArray();
	}
}
