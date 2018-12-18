import java.awt.Color;

public class Bullet extends GameObject {
	public static final double bulletVelocity = 40.0;
	private double shotDistance;
	private Bot shooter,target;
	
	public Bullet(Bot shooter, Bot target/*, ArrayList<Bullet> currentBullets*/) {
		super(shooter.getx(),shooter.y, shooter.teamid, Color.GRAY, "BU");
		this.shooter=shooter;	this.target=target;
		//this.bullets=currentBullets;
		this.shotDistance=shooter.getDistance(target);
		isAlive=true;
		speed=bulletVelocity;
	}
	
	public void tick(double time) {
		moveTo(target);
		if(getDistance(target)<GameObject.minBotRadius) {
			//hit
			isAlive=false;
			target.attemptDamage(shotDistance, shooter.attkPwr);
		}
	}
	

}
