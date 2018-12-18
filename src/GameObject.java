import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

public class GameObject {
	//configurables
	protected static double minBotRadius=0.8;
	
	//class vars
	private static int GameObjectsMade=0;
	
	//instance vars
	protected final int id=GameObjectsMade++;
	protected int teamid;
	protected double x,y;
	protected double speed=0;
	public boolean isAlive=true;
	public double gameTime=0;

	//display info
	protected Color dispColor=Color.BLACK;
	protected String dispStr = "ERR";
	public boolean renderable=true;
	public boolean showAsterisk = false;

	//MEMORY
	protected ArrayList<Bot> bots;
	protected ArrayList<Resource> resources;
	protected ArrayList<Base> bases;
	protected ArrayList<Bullet> bullets;
	public void updateCache(ArrayList<Bot> bots,ArrayList<Resource> resources,ArrayList<Base> bases,ArrayList<Bullet> bullets) {
		this.bots=bots;	this.resources=resources;	this.bases=bases;	this.bullets=bullets;
	}
	
	public GameObject(double x,double y,int teamid,Color dispColor,String dispStr) {
		this.x=x;this.y=y;
		this.teamid=teamid;
		this.dispColor=dispColor;	this.dispStr=dispStr;
	}
	
	//public static helper methods
	public static Color getTeamColor(int team) {
		switch(team) {
		case 0:return Color.BLUE;
		case 1:return Color.RED;
		case 2:return Color.GREEN;
		case 3:return Color.CYAN;
		default:return Color.GRAY;
		}
	}
	
	//query this object
	public double getx() {
		return x;
	}
	public double gety() {
		return y;
	}
	public int getid() {
		return id;
	}
	public int getTeamid() {
		return teamid;
	}
	public Color getColor() {
		return dispColor;
	}
	public String getDispString() {
		return dispStr;
	}
	
	//basic sensors
	private double getDistance(double x,double y) {
		return Math.sqrt(Math.pow(this.x-x, 2.0)+Math.pow(this.y-y, 2.0));
	}
	public double getDistance(GameObject target) {
		if(target==null) return -1;
		if(this.getid()==target.getid()) System.err.println("measuring distance to self. Likely to cause error");
		return getDistance(target.x,target.y);
	}
	public int getDistanceLevel(GameObject target) {
		return getDistanceLevel(getDistance(target));
	}
	public static int getDistanceLevel(double distance) {
		if(distance<=Match.shortRadius)
			return 0;
		else if(distance<=Match.midRadius)
			return 1;
		else if(distance<=Match.longRadius)
			return 2;
		else
			return 3;
	}
	public boolean isClone(GameObject target) {
		if(target == null) return false;
		return (this.getid() == target.getid());
	}
	public boolean isAlly(GameObject target) {
		return (this.getTeamid() == target.getTeamid());
	}
	public int countBots(Predicate<Bot> filter) {
		return (int) bots.stream().filter(filter).count();
	}
	public int numAllies() {
		return countBots(b -> isAlly(b) && !isClone(b));
	}
	public int numEnemies() {
		return countBots(b -> !isAlly(b));
	}
	
	//bot distance queries
	public boolean isEnemyAtShortRange(Bot requestor) {
		return isEnemyAtRange(0,requestor);
	}
	public boolean isEnemyAtMidRange(Bot requestor) {
		return isEnemyAtRange(1,requestor);
	}
	public boolean isEnemyAtLongRange(Bot requestor) {
		return isEnemyAtRange(2,requestor);
	}
	public boolean isEnemyOutOfRange(Bot requestor) {
		return isEnemyAtRange(3,requestor);
	}
	public boolean isAllyAtShortRange(Bot requestor) {
		return isAllyAtRange(0,requestor);
	}
	public boolean isAllyAtMidRange(Bot requestor) {
		return isAllyAtRange(1,requestor);
	}
	public boolean isAllyAtLongRange(Bot requestor) {
		return isAllyAtRange(2,requestor);
	}
	public boolean isAllyOutOfRange(Bot requestor) {
		return isAllyAtRange(3,requestor);
	}
	private boolean isEnemyAtRange(int range,Bot requestor) {
		Optional<Bot> b = bots.stream()
					.filter(p -> (p.getid()!=requestor.getid()))
					.filter(p -> (p.getDistanceLevel(this)==range))
					.filter(p -> (p.getTeamid()!=requestor.getTeamid()))
					.min(Comparator.comparing(p -> this.getDistance(p)));
		if(b.isPresent())
			return true;
		return false;
	}
	private boolean isAllyAtRange(int range,Bot requestor) {
		Optional<Bot> b = bots.stream()
					.filter(p -> (p.getid()!=requestor.getid()))
					.filter(p -> (p.getDistanceLevel(this)==range))
					.filter(p -> (p.getTeamid()!=requestor.getTeamid()))
					.min(Comparator.comparing(p -> this.getDistance(p)));
		if(b.isPresent())
			return true;
		return false;
	}
	
	//actions
	protected void moveTo(double x,double y) {
		//System.out.println("move at speed "+speed);
		double dx = x - this.x;
		double dy = y - this.y;
		double newx,newy;
		if(this.getDistance(x,y) > speed*Match.timeResolution) {
			//cannot reach coordinates this tick
			newx=dx*((speed*Match.timeResolution)/this.getDistance(x,y));
			newy=dy*((speed*Match.timeResolution)/this.getDistance(x,y));
		}else {
			newx=(x-this.x)*0.9d;
			newy=(y-this.y)*0.9d;
		}
		this.x+=newx;
		this.y+=newy;
	}
	protected void moveTo(GameObject target) {
		if(isClone(target)){
			System.err.println("trying to move to self");
			return;
		}/*else if(target==null) {
			System.err.println("trying to move to null");
			return;
		}*/
		moveTo(target.x,target.y);
	}
	
	//base target queries
	private Bot getClosestBot(Predicate<Bot> filter) {
		try{
			return bots.stream()
				.filter(filter)			//filter by alliance
				.filter(p -> !isClone(p))
				.min(Comparator.comparing(p -> this.getDistance(p))).get();	//find closest
		}catch(NoSuchElementException e) {
			return null;
		}
	}
	private Bot getFurthestBot(Predicate<Bot> filter) {
		if(numEnemies()==0)
			return null;
		return bots.stream()
				.filter(filter)			//filter by alliance
				.filter(p -> !isClone(p))	//remove self, if present
				.max(Comparator.comparing(p -> this.getDistance(p))).get();	//find furthest
	}
	private Base getClosestBase(Predicate<Base> filter) {
		return bases.stream()
				.filter(filter)			//filter by alliance
				.filter(p -> !isClone(p))	//remove self, if present
				.min(Comparator.comparing(p -> this.getDistance(p))).get();	//find closest
	}
	private Base getFurthestBase(Predicate<Base> filter) {
		return bases.stream()
				.filter(filter)			//filter by alliance
				.filter(p -> !isClone(p))	//remove self, if present
				.max(Comparator.comparing(p -> this.getDistance(p))).get();	//find furthest
	}
	public Resource getClosestResource() {
		if(resources.size()>0)
			return resources.stream().min(Comparator.comparing(p -> this.getDistance(p))).get();
		else
			return null;
	}
	public Resource getFurthestResource() {
		if(resources.size()>0)
			return resources.stream().max(Comparator.comparing(p -> this.getDistance(p))).get();
		else
			return null;
	}
	
	//advanced target queries
		//Bots
	public Bot getClosestAllyBot() {
		if(numAllies()==0)
			return null;
		return getClosestBot(b -> b.isAlly(this));
	}
	public Bot getClosestEnemyBot() {
		if(numEnemies()==0)
			return null;
		return getClosestBot(b -> !b.isAlly(this));
	}
	public Bot getFurthestAllyBot() {
		if(numAllies()==0)
			return null;
		return getFurthestBot(b -> b.isAlly(this));
	}
	public Bot getFurthestEnemyBot() {
		if(numEnemies()==0)
			return null;
		return getFurthestBot(b -> !b.isAlly(this));
	}
	public Bot getClosestAllyUnderAttack() {
		if(numAllies()<1)
			return null;
		return getClosestBot(b -> b.isAlly(this) && b.isUnderAttack());
	}
	public Bot getFurthestAllyUnderAttack() {
		if(numAllies()==0)
			return null;
		return getFurthestBot(b -> b.isAlly(this) && b.isUnderAttack());
	}
	public Bot getClosestEnemyUnderAttack() {
		if(numEnemies()==0)
			return null;
		return getClosestBot(b -> !b.isAlly(this) && b.isUnderAttack());
	}
	public Bot getFurthestEnemyUnderAttack() {
		if(numEnemies()==0)
			return null;
		return getFurthestBot(b -> !b.isAlly(this) && b.isUnderAttack());
	}
	public Bot getClosestEnemyCarryingResource() {
		if(numEnemies()==0)
			return null;
		return getClosestBot(b -> !b.isAlly(this) && b.hasResource());
	}
	public Bot getFurthestEnemyCarryingResource() {
		if(numEnemies()==0)
			return null;
		return getFurthestBot(b -> !b.isAlly(this) && b.hasResource());
	}
		//Bases
	public Base getClosestAllyBase() {
		return getClosestBase(b -> b.isAlly(this));
	}
	public Base getClosestEnemyBase() {
		return getClosestBase(b -> !b.isAlly(this));
	}
	public Base getFurthestAllyBase() {
		return getFurthestBase(b -> b.isAlly(this));
	}
	public Base getFurthestEnemyBase() {
		return getFurthestBase(b -> !b.isAlly(this));
	}
		//Locations
	public double[] getAllAttackingEnemies() {
		int count=0;
		double[] pos = {0,0};
		for(Bot b:bots) {
			if(!isClone(b) && !isAlly(b)) {
				pos[0]+=b.x;
				pos[1]+=b.y;
				count++;
			}
		}
		pos[0]/=(double)count;
		pos[1]/=(double)count;
		return pos;
	}
}
