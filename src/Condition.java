
public class Condition {
	
	//Searches condition and returns true or false
	public static int evaluate(String condition,Bot requestor) {
		//System.out.println("conditional:\t"+condition+" is not equal to isEnemyAtShortRangeOfSelf");;
		if(condition.equals("isEnemyAtShortRangeOfSelf")) {
			return returnInt(requestor.isEnemyAtShortRange(requestor));
		}else if(condition.equals("isEnemyAtMidRangeOfSelf")) {
			return returnInt(requestor.isEnemyAtMidRange(requestor));
		}else if(condition.equals("isEnemyAtLongRangeOfSelf")) {
			return returnInt(requestor.isEnemyAtLongRange(requestor));
		}else if(condition.equals("isEnemyOutOfRangeOfSelf")) {
			return returnInt(requestor.isEnemyOutOfRange(requestor));
		}else if(condition.equals("isCarryingResource")) {
			return returnInt(requestor.hasResource());
		}else if(condition.equals("isSelfUnderAttack")) {
			return returnInt(requestor.isUnderAttack());
		}else if(condition.equals("selfShield1to25percent")) {
			return returnInt(((double)requestor.shield / (double)requestor.maxShield) <=0.25);
		}else if(condition.equals("selfShield26to50percent")) {
			return returnInt(((double)requestor.shield / (double)requestor.maxShield) > 0.25 && ((double)requestor.shield / (double)requestor.maxShield) <= 0.5);
		}else if(condition.equals("selfShield51to75percent")) {
			return returnInt(((double)requestor.shield / (double)requestor.maxShield) > 0.5 && ((double)requestor.shield / (double)requestor.maxShield) <= 0.75);
		}else if(condition.equals("selfShield76to99percent")) {
			return returnInt(((double)requestor.shield / (double)requestor.maxShield) > 0.75 && ((double)requestor.shield / (double)requestor.maxShield) < 1);
		}else if(condition.equals("selfShieldFull")) {
			return returnInt(requestor.shield == requestor.maxShield);
		}else if(condition.equals("selfHealth1to25percent")) {
			return returnInt(((double)requestor.health / (double)requestor.maxHealth) <=0.25);
		}else if(condition.equals("selfHealth26to50percent")) {
			return returnInt(((double)requestor.health / (double)requestor.maxHealth) > 0.25 && ((double)requestor.health / (double)requestor.maxHealth) <= 0.5);
		}else if(condition.equals("selfHealth51to75percent")) {
			return returnInt(((double)requestor.health / (double)requestor.maxHealth) > 0.5 && ((double)requestor.health / (double)requestor.maxHealth) <= 0.75);
		}else if(condition.equals("selfHealth76to99percent")) {
			return returnInt(((double)requestor.health / (double)requestor.maxHealth) > 0.75 && ((double)requestor.health / (double)requestor.maxHealth) < 1);
		}else if(condition.equals("selfHealthFull")) {
			return returnInt(requestor.health == requestor.maxHealth);
		}
		System.err.println("Condition.evaluate:\tCould not evaluate variable '"+condition+"'");
		return -1;
	}
	private static int returnInt(boolean b) {
		return (b) ? 1 : 0;
	}
}
