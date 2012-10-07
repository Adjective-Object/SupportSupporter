
/*
 * Scrapped together in an hour with materials from Oracle's Swing Tutorials, and the JNativeHook jars
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * This application made by Maxwell Huang-Hobbs of EnigmaSM Softworks.
 */

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
 
/* TopLevelDemo.java requires no other files. */
public class Runner implements NativeKeyListener , ActionListener, WindowListener {
	
	ArrayList<Image> rawBuffImages;
	ArrayList<Long> rawBuffTimes;
	ArrayList<Integer> buffKeybindings;

	ArrayList<Object[]> tempBuffs;//Image(JLabel), text(JtextPane), time(double)
	
	Color CHILLCOLOR  = Color.blue;
	Color WARNINGCOLOR= Color.yellow;
	Color DANGERCOLOR = Color.red;
	Color NULLCOLOR   = Color.black;
	

	Color FRIENDCOLOR  = new Color(200,200,250);
	Color ENEMYCOLOR   = new Color(250,200,200);
	
	JPanel mainPanel;
	JFrame frame;
	boolean isShifted = false;
	
	int timeResolution=50;
	ArrayList<Integer> lastPressed = new ArrayList<Integer>(0);
	ArrayList<NativeKeyEvent> timerKeys;
	Font font;
	
	GridBagConstraints gridConstraints;
	
	int winWidth = 120;
	int winHeightMin = 55;
	int winHeightEach = 26;
	int gridy=0;
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("Lol Timer");
        frame.setPreferredSize(new Dimension(winWidth,winHeightMin));
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(this);
        
        rawBuffImages = new ArrayList<Image>(0);
        
        rawBuffTimes = new ArrayList<Long>(10);
        buffKeybindings = new ArrayList<Integer>(10);
        
        rawBuffImages.add(getImage("Baron_Nashor.gif"));
        rawBuffTimes.add(new Long(7*60000));
        buffKeybindings.add(new Integer(NativeKeyEvent.VK_INSERT));
        
        rawBuffImages.add(getImage("Dragon.gif"));
        rawBuffTimes.add(new Long(6*60000));
        buffKeybindings.add(new Integer(NativeKeyEvent.VK_HOME));
        
        rawBuffImages.add(getImage("Blue_Buff.gif"));
        rawBuffTimes.add(new Long(5*60000));
        buffKeybindings.add(new Integer(NativeKeyEvent.VK_PAGE_UP));
        
        rawBuffImages.add(getImage("Red_Buff.gif"));
        rawBuffTimes.add(new Long(5*60000));
        buffKeybindings.add(new Integer(NativeKeyEvent.VK_DELETE));
        
        rawBuffImages.add(getImage("Sight_Ward.gif"));
        rawBuffTimes.add(new Long(3*60000));
        buffKeybindings.add(new Integer(NativeKeyEvent.VK_END));
        
        rawBuffImages.add(getImage("Vision_Ward.gif"));
        rawBuffTimes.add(new Long(3*60000));
        buffKeybindings.add(new Integer(NativeKeyEvent.VK_PAGE_DOWN));

        tempBuffs = new ArrayList<Object[]>(0);
        timerKeys = new ArrayList<NativeKeyEvent>(0);
        mainPanel = new JPanel();
        
        mainPanel.setLayout(new GridBagLayout());
        
        gridConstraints = new GridBagConstraints();
	    gridConstraints.weightx = 1;
	    gridConstraints.weighty = 1;

        font = new Font("Sans-Serif", Font.BOLD, 12);
        
        Timer display = new Timer(timeResolution, this);
        display.setActionCommand("Update_Display");
        display.start();
        
        mainPanel.setSize(new Dimension(120,400));
        
        //Display the window
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }
    
    protected static String getTimeFormattedString(long time){
    	SimpleDateFormat df = new SimpleDateFormat("mm:ss.SSS");
    	
    	if(time<=0){
    		time=0L;
    	}
    	
    	Date d = new Date(0,0,0,0,0,(int)(time/1000F));
    	return df.format(d);
    }
    
    protected static Image getImage(String path) {
    	return new ImageIcon( Runner.class.getResource(path)).getImage().getScaledInstance(24, 24, Image.SCALE_AREA_AVERAGING);   
    }

	@Override
	public void nativeKeyPressed(NativeKeyEvent ke) {
		for(int i=0; i<buffKeybindings.size(); i++){
			if(ke.getKeyCode()==buffKeybindings.get(i)){
				lastPressed.add(i);
			}
		}
		if(ke.getKeyCode()==NativeKeyEvent.VK_SHIFT){
			this.isShifted=true;
		}
	}
	
	@Override
	public void nativeKeyReleased(NativeKeyEvent ke) {
		if(ke.getKeyCode()==NativeKeyEvent.VK_SHIFT){
			this.isShifted=false;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		while (lastPressed.size()>0){
			if(lastPressed.get(0)!=-1){
				Object[] p = makeNewTimer(lastPressed.get(0),!this.isShifted);
				tempBuffs.add(p);
				
				System.out.println(this.gridy);

				gridConstraints.fill = GridBagConstraints.NONE;
				gridConstraints.gridy=this.gridy;
				gridConstraints.gridx=0;
				mainPanel.add((JLabel)p[0],gridConstraints);
				gridConstraints.gridx=1;
				gridConstraints.fill = GridBagConstraints.REMAINDER;
				mainPanel.add((JTextPane)p[1],gridConstraints);
				
				mainPanel.revalidate();
				mainPanel.repaint();
				this.gridy++;
			}
			else{
				killTimer(tempBuffs.size()-1);
			}
			lastPressed.remove(0);
		}
		
		if (e.getActionCommand().equals("Update_Display")){
			updateDisplays(System.currentTimeMillis());
		} 
	}
	
	protected Object[] makeNewTimer(int timerType, boolean enemy){
		Object[] timerBlock = new Object[3];
		
    	JLabel l = new JLabel( new ImageIcon(rawBuffImages.get(timerType)), JLabel.LEFT);
    	timerBlock[0]=l;
    	
    	JTextPane t = new JTextPane();
    	t.setFont(font);
    	t.setText("NEW_TIMER");
        t.setForeground(this.CHILLCOLOR);
        timerBlock[1]=t;
    	
    	if(enemy){
    		t.setBackground(ENEMYCOLOR);
    	}
    	else{
    		t.setBackground(FRIENDCOLOR);
    	}
        
        timerBlock[2]=new Long(System.currentTimeMillis()+rawBuffTimes.get(timerType));
        
        frame.setBounds(frame.getX(), frame.getY(), winWidth, winHeightMin+winHeightEach*tempBuffs.size());
    	return timerBlock;
	}
	
	protected void killTimer(int i){
		JTextPane a = (JTextPane)(tempBuffs.get(i)[1]);
		a.getParent().remove(a);
		JLabel lab =(JLabel)(tempBuffs.get(i)[0]);
		lab.getParent().remove(lab);
		tempBuffs.remove(i);
		i--;
		frame.setBounds(frame.getX(), frame.getY(), winWidth, winHeightMin+winHeightEach*tempBuffs.size());
		gridy--;
	}
	
	public void updateDisplays(long nowFired){
		for (int i=0 ; i<tempBuffs.size(); i++){
			long l = (Long)(tempBuffs.get(i)[2])-(nowFired);
			JTextPane a = (JTextPane)(tempBuffs.get(i)[1]);
			a.setText(getTimeFormattedString(l));
			
			if(l>30000 && a.getForeground()!=this.CHILLCOLOR){
				a.setForeground(this.CHILLCOLOR);
			}
			if(l<=30000 && a.getForeground()!=this.WARNINGCOLOR){
				a.setForeground(this.WARNINGCOLOR);
			}
			if(l<=5000 && a.getForeground()!=this.DANGERCOLOR){
				a.setForeground(this.DANGERCOLOR);
			}
			if(l<=-2000 && a.getForeground()!=this.NULLCOLOR){
				a.setForeground(this.NULLCOLOR);
			}
			if(l<=-5000 && a.getForeground()==this.NULLCOLOR){
				killTimer(i);
			}
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {}
	
	public static void main(String[] args) {
		 //Schedule a job for the event-dispatching thread:
	     //creating and showing this application's GUI.
	    
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    	public void run() {
	    		Runner r = new Runner();
	    		r.createAndShowGUI();
	    		
	    		
	    		try {
		   	         GlobalScreen.registerNativeHook();
			   		}
			   	catch (NativeHookException ex) {
			   		System.err.println("There was a problem registering the native hook.");
			   	    System.err.println(ex.getMessage());
			   	    System.exit(1);
			   	}
			   	
			   	//Construct the example object and initialze native hook.
			   	GlobalScreen.getInstance().addNativeKeyListener(r);
			   	
	    	}
	    });
	 }

	@Override
	public void windowClosing(WindowEvent e) {
        //Clean up the native hook.
        GlobalScreen.unregisterNativeHook();
        System.runFinalization();
        System.exit(0);
    }
	
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}
}

