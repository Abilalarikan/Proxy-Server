import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.*;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	JMenuBar menuBar;
	
	public String status="";
	
	public MainFrame() {
	
		setTitle("Proxy Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
        setSize(500,600);
        setLocationRelativeTo(null);
		setLayout(null);
		
		setPanel("wait");
		
		
	}
	
	public void setPanel(String panelName) {
		
		getContentPane().removeAll();
		getContentPane().repaint();
		
		Panel panel = null;
		
		if(panelName.equals("wait") || panelName.equals("login")) {
			
			panel= new Panel(this,panelName,null);
	
		}
		
		else if(panelName.equals("app")) {
			
			menuBar = new JMenuBar();
			this.setJMenuBar(menuBar);
			panel=new Panel(this,panelName,menuBar);
			
		}
		
		
		getContentPane().add(panel);
		revalidate();
		repaint();
	}
	
}
