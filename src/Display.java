import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Display{
	//instance vars
	public mapFrame frame;
	private int gameid;

	//objects
	private ArrayList<Integer> scores;
	private ArrayList<Bot> bots;
	private ArrayList<Resource> resources;
	private ArrayList<Base> bases;
	private ArrayList<Bullet> bullets;
	private Match owner;

	public Display(int gameid,ArrayList<Integer> scores,ArrayList<Bot> bots,ArrayList<Resource> resources,ArrayList<Base> bases,ArrayList<Bullet> bullets,Match owner) {
		this.frame=new mapFrame(owner);
		this.gameid=gameid;
		this.scores=scores;
		this.owner=owner;
		frame.setSize(600,600);
		frame.setTitle("Game #"+this.gameid);
		frame.setVisible(Driver.showGame);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.bots=bots;
		this.resources=resources;
		this.bases=bases;
		this.bullets=bullets;
		displayAll();
	}
	public void displayAll() {
		//System.out.println(bots.size());
		String title = "";
			title += "Game #" + this.gameid;
			title += " time: " + getTimeString();
			title += " score: " + getScoreString();
			if(owner.programs != null) {
				title += " "+owner.programs.get(0).getName();
				title += " vs "+owner.programs.get(1).getName();
			}
		frame.add(new dispComp(bots,resources,bases,bullets,frame));
		//frame.setTitle("Game #"+this.gameid+" score: "+getScoreString());
		frame.setTitle(title);
		frame.repaint();
	}
	public String getScoreString() {
		String scoreString="";
		for(int i=0;i<scores.size();i++) {
			if(i>0) scoreString+="-";
			scoreString+=scores.get(i);
		}
		return scoreString;
	}
	public String getTimeString() {
		int time = (int)owner.time;
		String timeString = "";
		timeString += time/60;
		timeString += ":";
		if(time%60<10)	timeString += "0";
		timeString += time%60;
		return timeString;
	}
}

class mapFrame extends JFrame implements WindowListener{
	private static final long serialVersionUID = 1L;
	public Match owner;
	public mapFrame(Match owner) {
		super();
		this.owner=owner;
		addWindowListener(this);
	}
	public void windowOpened(WindowEvent e) {
	}
	public void windowClosing(WindowEvent e) {
		//System.out.println("closing");
		owner.run=false;
	}
	public void windowClosed(WindowEvent e) {
		System.err.println("Display.mapFrame windowClosed. No case known to reach");
	}
	public void windowIconified(WindowEvent e) {
	}
	public void windowDeiconified(WindowEvent e) {
	}
	public void windowActivated(WindowEvent e) {
	}
	public void windowDeactivated(WindowEvent e) {
	}
	
}

class dispComp extends JComponent{
	private static final long serialVersionUID = 1L;
	protected ArrayList<Bot> bots;
	protected ArrayList<Resource> resources;
	protected ArrayList<Base> bases;
	protected ArrayList<Bullet> bullets;
	
	protected ArrayList<Bot> botsCache;
	protected ArrayList<Resource> resourcesCache = new ArrayList<Resource>();
	protected ArrayList<Base> basesCache;
	protected ArrayList<Bullet> bulletsCache;
	
	private JFrame frame;
	public dispComp(ArrayList<Bot> bots,ArrayList<Resource> resources,ArrayList<Base> bases,ArrayList<Bullet> bullets,JFrame frame) {
		this.frame=frame;
		this.bots=bots;
		this.resources=resources;
		this.bases=bases;
		this.bullets=bullets;
		objectsToCache();
	}
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		objectsToCache();
		try {
			for(Bot b:bots)
				(new dispObj(b,frame)).draw(g2);
			for(Resource r:resourcesCache)
				(new dispObj(r,frame)).draw(g2);
			for(Base b:bases)
				(new dispObj(b,frame)).draw(g2);
			for(Bullet b:bullets)
				(new dispObj(b,frame)).draw(g2);
		} catch(ConcurrentModificationException e) {
		}
		//System.out.println(resources.size());
	}
	public void objectsToCache() {
		ArrayList<Resource> newResourcesCache = new ArrayList<Resource>();
		try {
			for(Resource r:resources)
				newResourcesCache.add(r);
			resourcesCache = newResourcesCache;
		}catch(ConcurrentModificationException e){
			System.err.println("draw crash");
		}
	}
}

class dispObj{
	private int x,y,w,h;
	private Color c;
	private String s;
	/*public dispComp(int x,int y,int w,int h,Color c,String s){
		this.x=x;this.y=y;
		this.w=w;this.h=h;
		this.c=c;this.s=s;
	}*/
	public dispObj(GameObject o,JFrame frame){
		this.w=frame.getWidth()-30;
		this.h=frame.getHeight()-50;
		this.x=(int) (((o.getx()+25)-GameObject.minBotRadius/2.0)*(w/50.0));
		this.y=(int) (((25-o.gety())-GameObject.minBotRadius/2.0)*(h/50.0));
		this.c=o.getColor();
		this.s=o.getDispString();
		if(o.showAsterisk) {
			this.s+="*";
		}
	}
	public void draw(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(c);
		g2.fill(new Ellipse2D.Double(x, y, (w/50.0)*GameObject.minBotRadius, (h/50.0)*GameObject.minBotRadius));
		g2.setColor(Color.BLACK);
		g2.drawString(s, x, y);

		g2.setColor(Color.BLACK);
		g2.drawLine(0, h, w, h);
		g2.drawLine(w, 0, w, h);
		g2.drawLine(0, h/2, w, h/2);
		g2.drawLine(w/2, 0, w/2, h);
	}
}