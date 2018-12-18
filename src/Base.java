import java.util.ArrayList;

public class Base extends GameObject {

	public Base(double x, double y, int teamid) {
		super(x, y, teamid, getTeamColor(teamid), "BA");
	}
	public static ArrayList<Base> generateBases(int teams, int numPerTeam){
		ArrayList<Base> bases = new ArrayList<Base>(numPerTeam);
		for(int team=0;team<teams;team++) {
			for(int i=0;i<numPerTeam;i++) {
				double x = (Math.random()-0.5)*50;
				double y = (Math.random()-0.5)*50;
				bases.add(new Base(x,y,team));
			}
		}
		return bases;
	}

}
