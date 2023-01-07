import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.*;

public class Panel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

	JLabel wait;
	
	JLabel title;
	JLabel username;
	JTextField userField;
	JLabel password;
	JPasswordField passwordField;
	JButton button;
	
	JMenuBar menuBar;
	JMenu file;
	JMenu help;
	JMenuItem start;
	JMenuItem stop;
	JMenuItem report;
	JMenuItem add;
	JMenuItem display;
	JMenuItem exit;
	JMenuItem about;
	JLabel ipText;
	
	MainFrame main;
	String pswrd;
	
	public  Panel(MainFrame main,String panelName,JMenuBar menuBar) {
		
		this.main=main;
		setBounds(0, 0, 500, 600);
		setLayout(null);
		if(panelName.equals("wait")) {
			
			wait=new JLabel("Waiting For Server to be Found...");
			wait.setFont(new Font("Sans-serif", Font.BOLD, 22));
			wait.setHorizontalTextPosition(JLabel.CENTER);
			wait.setVerticalTextPosition(JLabel.CENTER);
			wait.setBounds(50, 50, 500, 400);
			add(wait);
			
		}
		else if (panelName.equals("login")) {
			
			title=new JLabel("<html>Proxy<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Client</html>");
			title.setBounds(70,10,400,200);
			title.setFont(new Font("Sans-serif", Font.BOLD, 50));
			
			
			username=new JLabel("User");
			username.setBounds(40, 240, 100, 30);
			username.setFont(new Font("Sans-serif", Font.BOLD, 18));
			userField=new JTextField();
			userField.setBounds(210,240,185,30);
			userField.setFont(new Font("Sans-serif", Font.BOLD, 20));
			
			password=new JLabel("Password");
			password.setBounds(40,290,100,30);
			password.setFont(new Font("Sans-serif", Font.BOLD, 18));
			passwordField=new JPasswordField();
			passwordField.setFont(new Font("Sans-serif", Font.BOLD, 25));
			passwordField.setEchoChar('*');
			passwordField.setBounds(210,290,185,30);
			
			button = new JButton("Login");
			button.setBounds(310,340,85,30);
			button.addActionListener(this);
			
			add(username);
			add(userField);
			add(password);
			add(passwordField);
			add(title);
			add(button);
			
		}
		else if(panelName.equals("app")) {
			
	        file = new JMenu("File");
	        help = new JMenu("Help");
	        
	        //adding Help menu, File menu and it's items  to the bar
	        
	        start = new JMenuItem("Start");
	        stop = new JMenuItem("Stop");
	        add = new JMenuItem("Add host to filter");
	        
	        report = new JMenuItem("Report");
	        display = new JMenuItem("Display current filtered hosts");
	        exit = new JMenuItem("Exit");
	        about = new JMenuItem("About");
	        
	        start.addActionListener(this);
	        stop.addActionListener(this);
	        report.addActionListener(this);
	        add.addActionListener(this);
	        display.addActionListener(this);
	        exit.addActionListener(this);
	        about.addActionListener(this);
	        
	        if(main.status.equals("admin")) {
	        	
	        	file.add(start);
		        file.add(stop);
		        file.add(add);
	        }
	        file.add(report);
	        file.add(display);
	        file.add(exit);
	        help.add(about);
	        
	        menuBar.add(file);
	        menuBar.add(help);
	        
	        try {
	        	 String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
	             SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
	             simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	             String date = simpleDateFormat.format(new Date());
	        	
	        	String template ="<html>Client IP address: %s<br>Server IP Address: %s<br><br><br>"
	        					+ "(Note: Make sure that DNS server address is set to<br> %s on your host before using Proxy Server.)<br><br><br>last successful login: %s </html>";
	        	String text = String.format(template,InetAddress.getLocalHost().getHostAddress() , Client.serverIP,Client.serverIP,date);
	        	
	        	ipText=new JLabel(text);
			    ipText.setFont(new Font("Sans-serif", Font.PLAIN, 18));
			    ipText.setBounds(10, 200, 500, 400);
			    
			    title=new JLabel("<html>Proxy<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Client</html>");
				title.setBounds(70,10,400,200);
				title.setFont(new Font("Sans-serif", Font.BOLD, 50));
				
			    add(title);
			    add(ipText);
				
	        }
	        catch(UnknownHostException e) {
	        	e.printStackTrace();
	        }
	        
	        
	        
		}
	}
	
	public Panel getPanel() {
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		FileCommands fl=null;
		
		if(e.getSource()==about) {
		
			JOptionPane.showMessageDialog(null,"Developer:\n       Ahmet Bilal ARIKAN\n"+ 
					"School Number:\n       20190702002\n "+
					"Contact:\n       ahmetbilal.arikan@std.yeditepe.edu.tr","ABOUT"
					,JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource()==exit) {
			
			System.exit(0);
		}
		try {
			fl = new FileCommands();
			
			if(e.getSource()==button) {
				
				 String rcvd=fl.sendCommandToServer(userField.getText(), String.valueOf(passwordField.getPassword()));
				
				 if(rcvd.equals("client") || rcvd.equals("admin") || rcvd.equals("guest") ){
					 main.status=rcvd;
					 main.setPanel("app");
					 JOptionPane.showMessageDialog(null,"Successfully logged in as " +rcvd,"Login",JOptionPane.INFORMATION_MESSAGE);
					 
				 }
				 else {
					 JOptionPane.showMessageDialog(null,"Wrong Password!","Error",JOptionPane.ERROR_MESSAGE);
				 }
			 
			}
			
			else if(e.getSource()==start) {
				
				String status=new FileCommands().sendCommandToServer("status",null);
				
				if(status.equals("clsd")) {
					fl.sendCommandToServer("start");
					JOptionPane.showMessageDialog(null,"Server is started.","Info",JOptionPane.INFORMATION_MESSAGE);
				}
					
				else
					JOptionPane.showMessageDialog(null,"Server is already running!","Warning",JOptionPane.INFORMATION_MESSAGE);
			}
			
			else if(e.getSource()==stop) {
				
				String status=new FileCommands().sendCommandToServer("status",null);
				
				if(status.equals("open")) {
					fl.sendCommandToServer("stop");
					JOptionPane.showMessageDialog(null,"Server is stopped.","Info",JOptionPane.INFORMATION_MESSAGE);
				}
					
				else
					JOptionPane.showMessageDialog(null,"Server is already closed!","Warning",JOptionPane.INFORMATION_MESSAGE);
				
			}
			
			else if(e.getSource()==report) {
				
				String value=JOptionPane.showInputDialog(this, "Please enter an IP Address");
					
					if (validate("ip",value)==true ) {
						try {
							
							InetAddress.getByName(value);
							
						} catch (UnknownHostException e1) {
							
							e1.printStackTrace();
						}
						
						String report=fl.sendCommandToServer("report",value);
						
						if(report.equals("")) {
							JOptionPane.showMessageDialog(null,"There is no such report!","Report",JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							File f = new File(System.getProperty("user.dir")+"/"+value+".txt");
							if(!f.exists()) {
								f.createNewFile();
							}
							FileOutputStream fos=new FileOutputStream(f);
							
							fos.write(report.getBytes());
							JOptionPane.showMessageDialog(null,"Report is saved.","Report",JOptionPane.INFORMATION_MESSAGE);
							
						}
					}
					else if(value.equals("")) {
						String report=fl.sendCommandToServer("report",value);
						if(report.equals("")) {
							JOptionPane.showMessageDialog(null,"There is no such report!","Report",JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							FileOutputStream fos=new FileOutputStream(System.getProperty("user.dir")+"/all.txt");
							fos.write(report.getBytes());
							
							JOptionPane.showMessageDialog(null,"Report is saved.","Report",JOptionPane.INFORMATION_MESSAGE);
						}
					}
					else 
						JOptionPane.showMessageDialog(null,"Invalid \nIP Adress","Error",JOptionPane.ERROR_MESSAGE);
					
			}
			else if(e.getSource()==add) {
				
				String value=JOptionPane.showInputDialog(this, "Please enter a host to filter");
				
				if(validate("domain",value)==true)
					
					fl.sendCommandToServer("add",value);
				else
					JOptionPane.showMessageDialog(null,"Invalid Domain Name","Error",JOptionPane.ERROR_MESSAGE);
			}
			
			else if(e.getSource()==display) {
				
				String list=fl.sendCommandToServer("display","");
				if (list.equals("")) {
					JOptionPane.showMessageDialog(null,"There is no host filtered!","Filtered Hosts List",JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(null,list,"Filtered Hosts List",JOptionPane.INFORMATION_MESSAGE);
				}
				
			}
			
		} catch (Exception e2) {
			
			//JOptionPane.showMessageDialog(null,"Server Conection Error","Error",JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
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
