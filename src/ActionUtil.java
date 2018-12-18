import java.awt.Color;

public class ActionUtil extends GameObject{
	public ActionUtil(double x, double y, int teamid, Color dispColor, String dispStr) {
		super(x, y, teamid, dispColor, dispStr);
	}

	public static void act(Bot b) {
		b.evaluate();
		if(!b.program.isAllowableAction(b.action.getName(), b)) {
			//System.out.println(b.action.getName() + " is an illegal action");
			b.forceActionUpdate();
			if(!b.program.isAllowableAction(b.action.getName(), b)) {
				//System.out.println(b.action.getName() + " is ALSO an illegal action");
			}
		}
		if(b.action==null) return;
		String action = b.action.getName();
		switch(action) {
		case "idle":	break;
		
		//offense
		case "attackClosestEnemyToSelf":	b.attack(b.getClosestEnemyBot());						break;
		
		//movement
		case "moveToClosestAllyToSelfUnderAttack":	b.walkTo(b.getClosestAllyUnderAttack());		break;
		case "moveToClosestEnemyBotToSelf":			b.walkTo(b.getClosestEnemyBot());				break;
		case "moveToClosestEnemyAttackedByAlly":	b.walkTo(b.getClosestEnemyUnderAttack());		break;
		case "fleeFromAllAttackingEnemy":			double pos[] = b.getAllAttackingEnemies();
														b.fleeFrom(pos[0],pos[1]);					break;
		
		//resource
		case "collectClosestResourceToSelf":		b.collectResource(b.getClosestResource());		break;
		case "scoreResourceToClosestBaseToSelf":	b.scoreResource(b.getClosestAllyBase());		break;
		case "dropResource":						b.dropResource();								break;
		
		default:	System.err.println("ActionUtil.act:\tCannot find action for "+action);
		}
	}
}
