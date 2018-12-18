import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Bot extends ActionUtil{
	//Bot config
	protected int health,shield,attkPwr,
					maxHealth,maxShield,
					bulletCount,bulletsPerShot;
	protected double regenDelay,regenDuration,
					moveSpeed,resourceSpeed,
					aimingDuration,attackDuration,
					shortPrecision,midPrecision,longPrecision;
	//End bot config

	//gun parameters
	private int clip=0;
	private double timeOfNextShot=0,expectedNextAttackTime=0;
	private Bot currentTarget=null;
	
	//health parameters
	public double timeOfLastDamage=0;
	
	//action parameters
	public AIObject action = AIObject.getActionFor("idle");
	private double nextActionCheckTime=0;
	private AIObject nextAction=action;
	
	//Other
	public Resource carriedResource = null;
	public Program program;
	public Bot(double x, double y, int teamid, String dispStr, Program program) {
		super(x, y, teamid, getTeamColor(teamid), dispStr);
		this.program=program;
	}
	public Bot(double x, double y, int teamid, String dispStr) {
		super(x, y, teamid, getTeamColor(teamid), dispStr);
	}
	
	//maintenance
	public void tick(double gameTime) {	//must be called once per tick to keep bot updated
		//System.out.println("new time: "+gameTime);
		this.gameTime=gameTime;	//update local game time
		attemptToSetAction();	//if sufficient time has passed, the stored nextAction will be moved to the current action slot
		heal();		//if sufficient time has passed, heal the bot up to the max health;
		ActionUtil.act(this);	//performs an action for the bot based on the stored action
	}
	public void heal() {
		boolean debug = false;
		if(debug) System.out.println(this.gameTime + " > " + (this.timeOfLastDamage+regenDelay));
		if(gameTime>(this.timeOfLastDamage+regenDelay) && shield<maxShield) {
			shield+=(maxShield/regenDuration)*Match.timeResolution;
		}
		//shield = (shield>maxShield) ? maxShield : shield;
	}
	public boolean cleanPosition() {
		boolean changed = false;
		ArrayList<Bot> altered = new ArrayList<Bot>(0);
		for(Bot b:bots) {
			if(b.getid()!=this.getid())
			if(getDistance(b)<GameObject.minBotRadius) {
				double correctionRatio = ((GameObject.minBotRadius/getDistance(b))+1)/2;
				double mx=(this.x+b.x)/2;
				double my=(this.y+b.y)/2;
				double noiseFieldX = (float) ((Math.random()-0.5f)*0.01);
				double noiseFieldY = (float) ((Math.random()-0.5f)*0.01);
				double cMult = 1.03;
				this.x=(this.x-mx)*correctionRatio*cMult+mx+noiseFieldX;
				b.x=(b.x-mx)*correctionRatio*cMult	+mx;
				this.y=(this.y-my)*correctionRatio*cMult+my+noiseFieldY;
				b.y=(b.y-my)*correctionRatio*cMult	+my;
				changed=true;
				altered.add(b);
			}
			if(this.x<-25) this.x= -25;
			if(this.x>25) this.x=25;
			if(this.y<-25) this.y= -25;
			if(this.y>25) this.y=25;
		}
		return changed;
	}
	
	//action controls
	private void attemptToSetAction(){
		if(gameTime>nextActionCheckTime) {
			nextActionCheckTime+=Match.aiTick;
			action=nextAction;
		}
	}
	public void setAction(AIObject newAction) {
		nextAction=newAction;
	}
	public void forceActionUpdate() {
		action=nextAction;
	}
	
	//actions
	public void walkTo(double x,double y) {
		speed = (carriedResource==null) ? moveSpeed : resourceSpeed;
		moveTo(x,y);
	}
	public void walkTo(GameObject o) {
		if(o==null) {
			//System.err.println("tried to walk to null object. current action: "+this.action.toString());
			return;
		}
		walkTo(o.x,o.y);
	}
	public void fleeFrom(double x,double y) {
		//TODO: untested
		double dx = x-this.x;
		double dy = y-this.y;
		double targetx = this.x - dx;
		double targety = this.y - dy;
		walkTo(targetx,targety);
	}
	public void fleeFrom(GameObject o) {
		fleeFrom(o.x,o.y);
	}
	public void attemptDamage(double distance,int damage) {
		if(doesShotHit(getDistanceLevel(distance))) {
			applyDamage(damage);
		}
	}
	public boolean doesShotHit(int distLevel) {
		double random=Math.random();
		//System.out.println(random);
		if(distLevel==0 && shortPrecision>random) {
			return true;
		}else if(distLevel==1 && midPrecision>random) {
			return true;
		}else if(distLevel==2 && longPrecision>random) {
			return true;
		}
		return false;
	}
	public void applyDamage(int damage) {
		//System.out.println("apply "+damage+" damage");
		this.timeOfLastDamage=gameTime;
		if(shield>=damage) {	//shield can absorb full damage
			shield-=damage;
			return;
		}else {
			damage-=shield;shield=0;	//shield is destroyed by taking damage
		}
		//shield has taken damage and is down to 0. apply remaining damage to health
		health-=damage;
		if(health<=0) {
			isAlive=false;
		}
	}
	public void attack(Bot target) {
		if(target==null) {
			//System.err.println("tried to attack null target");
			//System.out.println(this.numEnemies());
			this.evaluate();
			//Driver.delay(2);
			return;
		}
		if(!target.isAlive) return;
		//System.out.println("attacking. nextTime:"+timeOfNextShot+" current time:"+gameTime);
		if(expectedNextAttackTime-gameTime!=0 || target.getid()!=currentTarget.getid()) {
			//System.out.println("reset aim");
			timeOfNextShot=gameTime+aimingDuration;
			clip=bulletCount;
		}
		if(timeOfNextShot<=gameTime) {
			//sufficient time has passed and another shot can be fired
			for(int i=0;i<bulletsPerShot && clip >0;i++) {
				bullets.add(new Bullet(this,target));
				clip--;
				//System.out.println("Fired bullet at ("+target.x+","+target.y+"). Target shield is "+target.shield);
			}
			timeOfNextShot=gameTime+(attackDuration/(double)(bulletCount/bulletsPerShot));
			//System.out.println(attackDuration);
		}
		if(clip<1) {	//clip is empty
			timeOfNextShot=gameTime+aimingDuration;
			clip=bulletCount;
			//System.out.println("reloading");
		}
		expectedNextAttackTime=gameTime+Match.timeResolution;
		currentTarget=target;
	}
	public void collectResource(Resource target) {
		if(carriedResource!=null) return;
		//am not carrying resource
		if(getDistance(target)<moveSpeed*Match.timeResolution || getDistance(target)<GameObject.minBotRadius) {
			//can reach the target this tick
			for(int i=0;i<resources.size();i++) {
				if(resources.get(i).getid()==target.getid()) {
					//positive match
					this.carriedResource=target;	//add resource to self
					resources.remove(i);	//remove resource from world
					showAsterisk=true;
					return;
				}
			}
		}else {
			//cannot reach this tick
			walkTo(target);
		}
	}
	public void scoreResource(Base target) {
		if(!canScoreResource()) {
			//System.err.println("tried to score resource, but did not have one");
			return;
		}
		if(getDistance(target)<moveSpeed*Match.timeResolution) {
			//can reach the target this tick
			carriedResource.setScoringTeamid(this.getTeamid());
			dropResource();
		}else {
			walkTo(target);
		}
	}
	public void dropResource() {
		if(carriedResource==null)
			return;
		carriedResource.x=this.x;
		carriedResource.y=this.y;
		resources.add(carriedResource);
		carriedResource=null;
	}
	public void evaluate() {
		program.evaluate(this);
	}
	
	//sensors
	public Bot getCurrentTarget() {
		if(expectedNextAttackTime-gameTime==0) {
			//am currently attacking
			return currentTarget;
		}
		return null;
	}
	public boolean isUnderAttack() {
		for(Bot b:bots)
			if(isClone(b.getCurrentTarget()))
				return true;
		return false;
	}
	public boolean canScoreResource() {
		return hasResource();
	}
	public boolean hasResource() {
		return carriedResource!=null;
	}
	public boolean hasAllyUnderAttack() {
		for(Bot b:bots)
			if(isAlly(b) && b.isUnderAttack())
				return true;
		return false;
	}
	
	//static helpers
	public static ArrayList<Bot> generateBots(int numTeams,int botsPerTeam){
		ArrayList<Bot> bots = new ArrayList<Bot>(numTeams*botsPerTeam);
		for(int i=0;i<numTeams;i++) for(int j=0;j<botsPerTeam;j++) {
			double botx=-23.5;
			if(numTeams!=1)
				botx = -23.5+((double)i*(47/((double)numTeams-1)));
			double boty = 0.8-((double)botsPerTeam*0.8) + ((double)j*1.6);
			bots.add(new Assault(botx,boty,i));
			//System.out.println("made bot at "+botx+","+boty);
		}
		return bots;
	}
	public static int getNumOnTeam(int team,ArrayList<Bot> bots) {
		int count=0;
		try {
			for(Bot b:bots)
				if(b!=null)
					if(b.getTeamid()==team)
						count++;
		}catch(NoSuchElementException e) {
		}
		return count;
	}
	public String toString() {
		return "("+(Math.round(this.x*100)/100.0)+","+(Math.round(this.y*100)/100.0)+")\thealth: "+health+"\tshield: "+shield;
	}
}
