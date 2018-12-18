import java.awt.Color;
import java.util.ArrayList;

public class Resource extends GameObject {

	public Resource(double x, double y) {
		super(x, y, -1, Color.orange, "RE");
	}

	public static ArrayList<Resource> generateResources(int num) {
		ArrayList<Resource> r = new ArrayList<Resource>(num);
		for(int i=0;i<num;i++) {
			double x = (Math.random()-0.5)*50.0;
			double y = (Math.random()-0.5)*50.0;
			r.add(new Resource(x,y));
		}
		return r;
	}
	public void setScoringTeamid(int teamid) {
		this.teamid=teamid;
	}
	
}
