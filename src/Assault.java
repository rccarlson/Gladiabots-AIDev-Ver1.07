
public class Assault extends Bot {

	public Assault(double x, double y, int teamid) {
		super(x, y, teamid, "AS");
		//config
		health = 5000;	shield = 3000;	attkPwr = 300;
		maxHealth = 5000;	maxShield = 3000;
		bulletCount = 6;	bulletsPerShot = 2;
		regenDelay = 3;	regenDuration = 3;
		moveSpeed = 1.2;	resourceSpeed = 0.5;
		aimingDuration = 1.0;	attackDuration = 0.2;
		shortPrecision = 0.95;	midPrecision = 0.55;	longPrecision = 0.15;
	}

}
