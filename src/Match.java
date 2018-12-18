import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

//Match is the executed part of the game

public class Match extends Thread {
	private static int matchesMade=0;
	public int matchid=matchesMade++;
	public int matchNumInCompetition;
	public static int matchesActive=-1;
	
	//CONFIG
	public static double timeResolution=0.01, aiTick=0.25,
						shortRadius = 3.0, midRadius = 8.0, longRadius = 15.0;
	public static int numTeams=2,botsPerTeam=4;
	public static double timeLimitSeconds=60*5;
	
	//GameObjects
	protected ArrayList<Bot> bots = Bot.generateBots(numTeams, botsPerTeam);
	protected ArrayList<Resource> resources = Resource.generateResources(7);
	protected ArrayList<Base> bases = Base.generateBases(numTeams, 2);
	protected ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	//instance vars
	public double time=0;
	public ArrayList<Integer> scores = generateScoreboard();
	public Display d = new Display(matchid,scores,bots,resources,bases,bullets,this);
	public ArrayList<Program> programs = new ArrayList<Program>();
	public boolean run=true;
	public int winningTeam=-1;
	
	public boolean isRunning=true;
	public static boolean showInRealTime=Driver.showInRealTime;
	public static double speedMult=Driver.speedMult;
	
	public Match() {
		initialize();
		//load all teams with sample
		for(int i=0;i<numTeams;i++)
			programs.add(new Program("sample"));
	}
	public Match(ArrayList<Program> programs) {
		initialize();
		this.programs=programs;
	}
	private void initialize() {
		try {
			matchNumInCompetition=Driver.matchesMadeForCurrentCompetition++;
		}catch(Exception e) {
			matchNumInCompetition=matchid;
		}
		updateMatchCount();
	}
	
	public void run() {
		String threadName = programs.get(0).getName()+"-"+programs.get(1).getName()+"["+matchNumInCompetition+"]";
		//System.out.println("setting name to: '"+threadName+"'");
		Thread.currentThread().setName(threadName);
		
		programBots();	//load all programs into bot
		loadObjectCaches();	//load all object addresses into bots, resources, and bases

		while(time<=timeLimitSeconds && !(!run || isGameOver())) {	//while inside the time limit && no end condition is true
			tick();
		}
		close();
		isRunning=false;
		return;

	}
	
	public void close() {
		matchesActive--;
		winningTeam=getWinningTeam();
		//System.out.println("end match. Winner: team "+getWinningTeam());
		d.frame.dispatchEvent(new WindowEvent(d.frame,WindowEvent.WINDOW_CLOSING));
	}
	
	public void tick() {
		//System.out.println(isGameOver());
		if(!run)
			return;
		try {
			for(int i=0;i<bots.size();i++) {
				//System.out.println("passing time "+time);
				Bot b = bots.get(i);
				b.evaluate();
				b.tick(time);
				if(!b.isAlive) {
					b.dropResource();
					bots.remove(b);
				}
			}
		}catch(ConcurrentModificationException e) {
			System.err.println("Match.tick:\tconcurrent mod error:\tbots");
		}
		try {
			for(int i=0;i<bullets.size();i++) {
				Bullet b = bullets.get(i);
				b.tick(time);
				if(!b.isAlive)
					bullets.remove(b);
			}
		}catch(ConcurrentModificationException e) {
			System.err.println("Match.tick:\tconcurrent mod error:\tbullets");
		}
		try {
			for(int i=0;i<resources.size();i++) {
				Resource r = resources.get(i);
				if(r.getTeamid()!=-1) {
					scores.set(r.getTeamid(),scores.get(r.getTeamid()) + 1);
					resources.remove(r);
				}
			}
		}catch(ConcurrentModificationException e) {
			System.err.println("Match.tick:\tconcurrent mod error:\tresources");
		}
			d.displayAll();
			if(showInRealTime) Driver.delay(timeResolution/speedMult);
		cleanBotPositions();
		time+=timeResolution;
	}
	public void loadObjectCaches() {
		for(Bot b:bots)
			b.updateCache(bots, resources, bases, bullets);
		for(Resource r:resources)
			r.updateCache(bots, resources, bases, bullets);
		for(Base b:bases)
			b.updateCache(bots, resources, bases, bullets);
	}
	public ArrayList<Integer> generateScoreboard(){
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for(int i=0;i<numTeams;i++) {
			scores.add(new Integer(0));
		}
		return scores;
	}
	public void programBots() {
		boolean debug = false;
		for(Bot b:bots) {
			if(debug) System.out.println("team: "+b.teamid);
			if(debug) System.out.println("prog list size: "+programs.size());
			if(debug) System.out.println("program: "+programs.get(b.teamid));
			b.program=programs.get(b.teamid);
		}
	}
	public void updateMatchCount() {
		if(matchesActive==-1)
			matchesActive=1;
		else
			matchesActive++;
	}
	public int unscoredResources() {
		if(bots == null) System.out.println("Match.unscoredResources:\tnull bot list");
		//if(bots.size()==0) System.out.println("empty bot list");
		int count=0;
		try {
			for(Bot b:bots)
				if(b!=null)
					if(b.carriedResource != null)
						count++;
			count+=resources.size();
		} catch (NoSuchElementException e) {
			System.out.println(bots.size());
		}
		return count;
	}
	public int getWinningTeam() {
		//build caches
		int unscored = unscoredResources();
		int[] teamPop = new int[numTeams];
		int highscore=0;
		for(int i=0;i<numTeams;i++) {
			teamPop[i]=Bot.getNumOnTeam(i, bots);
			int score = scores.get(i);
			highscore = (score > highscore) ? score : highscore;
		}
		
		//evaluate for win condition
		for(int team = 0;team<2;team++) {
			boolean canBeBeaten=false;
			boolean enemiesExist=false;
			boolean hasHighScore=(scores.get(team)==highscore);
			for(int otherTeam = 0;otherTeam<numTeams;otherTeam++) if(team!=otherTeam) {
				//search for winning score
				if (scores.get(team) < scores.get(otherTeam)+unscored) {
					//if otherTeam scores all remaining resources, they will have a higher score than team
					canBeBeaten=true;
				}
				enemiesExist |= teamPop[otherTeam]>0;	//if enemies exist on otherTeam, set enemiesExist to true
			}
			if(!canBeBeaten || (!enemiesExist && hasHighScore)) return team;	//if team cannot be beaten, return this team as winner
		}
		//no winner could be found by score or population
		return -1;
	}
	public boolean isGameOver() {
		return getWinningTeam() != -1;
	}
	public void cleanBotPositions() {
		boolean changed=true;
		while(changed) {
			changed = false;
			for(Bot b:bots) {
				changed |= b.cleanPosition();
			}
		}
	}

	public static ArrayList<Program> choosePrograms(int botNum0,int botNum1){
		System.out.println("bot"+Program.getBotNumString(botNum0));
		ArrayList<Program> progs = new ArrayList<Program>(2);
		progs.add(new Program("bot"+Program.getBotNumString(botNum0)));
		progs.add(new Program("bot"+Program.getBotNumString(botNum1)));
		return progs;
	}
}
