import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

public class Main {

	private static HashMap<String,String> adminInfo= new HashMap<String,String>();
	private static HashMap<String,String> userInfo= new HashMap<String,String>();
	private static HashMap<String,String> ipInfo = new HashMap<String,String>();
	
	public static Broadcast br=new Broadcast();
	
	public static void main(String[] args)  {
		try {
			adminInfo.put("admin", "admin123");
			adminInfo.put("admin2", "admin2123");
			userInfo.put("client", "client123");
			userInfo.put("client2", "client2123");
			
			ServerSocket commandSocket = new ServerSocket(2121);

			while (true) {
				
				Socket clientSocket = commandSocket.accept();
				new CommandHandler(clientSocket).start();
				
				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static synchronized HashMap<String,String> getUserInfo() {
		return userInfo;
	}
	
	public static synchronized HashMap<String,String> getAdminInfo() {
		return adminInfo;
	}
	
	public static Broadcast getBroadcast() {
		return br;
	}
	
	public static synchronized HashMap<String,String> getipInfo() {
		return ipInfo;
	}
	
	public static byte[] findIP(String hostname) {
		
		try {
			String command= "curl -fs -H 'Accept: application/dns-json' 'https://1.1.1.1/dns-query?name="+hostname +"' | jq -r '.Answer[0].data'";
			File f = new File(System.getProperty("user.dir")+"/getIp.sh");
			
			if( f.exists()==false || f.isDirectory()) {
				
				f.createNewFile();
				execCmd("chmod 777 "+ System.getProperty("user.dir")+"/getIp.sh" );
				
			}
			
			FileOutputStream fos=new FileOutputStream(System.getProperty("user.dir")+"/getIp.sh");
			fos.write(command.getBytes());
			fos.close();
			String ipAddress;
			String ipString;
			String[] ipSplit;
			String newHost;
			while(true) {
				
				ipAddress=execCmd(System.getProperty("user.dir")+"/getIp.sh");
				ipString = ipAddress.substring(0,ipAddress.length()-1);
				
				System.out.println("ip: " + ipString);
				if( validate("ip",ipString))
					break;
				
				newHost=ipString;
				command="curl -fs -H 'Accept: application/dns-json' 'https://1.1.1.1/dns-query?name="+ newHost +"' | jq -r '.Answer[0].data'";
				fos=new FileOutputStream(System.getProperty("user.dir")+"/getIp.sh");
				fos.write(command.getBytes());
				fos.close();
			}
			
			
			
			ipSplit = ipString.split("\\.");
			byte[] ipArr = new byte[4];
			for(int i=0;i<4;i++) {
				int temp=Integer.parseInt(ipSplit[i]);
				ipArr[i]= (byte) temp;
			}
			
			return ipArr;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String execCmd(String cmd) throws java.io.IOException {
		String[] cmnds={"sh",cmd};
	    java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmnds).getInputStream()).useDelimiter("\\A");
	    
	    String ip=s.hasNext() ? s.next() : "";
	    s.close();
	    return ip;
	}
	
	public static void writeReport(String ip,String domain,String method,int code) {
		
		
			try {
				
				String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
	            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
	            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	            String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
	            
				String line=ip+":"+date+"\t"+domain+"\t"+method+"\t"+code+"\n";
				File f = new File(System.getProperty("user.dir")+"/report.txt");
				if(!f.exists()) {
					f.createNewFile();
				}
				FileOutputStream fos=new FileOutputStream(System.getProperty("user.dir")+"/report.txt",true);
				fos.write(line.getBytes());
				fos.close();
				System.out.println(domain+": report saved.");
			} 
			catch (Exception e) {
				
				e.printStackTrace();
			}
		
		
	}
	public static boolean validate(String type,String str) {
		
		String pattern="";
		if (type.equals("ip"))
			
			pattern= "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
		
		else if (type.equals("domain"))
			
			pattern="^((?!-)[A-Za-z0-9-]"+ "{1,63}(?<!-)\\.)"+ "+[A-Za-z]{2,6}";
    	
		return str.matches(pattern);
	}
}
