import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class AIObject {
	private static boolean allowDebug=true;
	private static String AIDirectory = "AIObjects/";
	private String name;
	private ArrayList<String> tags;
	public static ArrayList<AIObject> actions=getObjectsFrom(AIDirectory+"actions.txt"),
									conditions=getObjectsFrom(AIDirectory+"conditions.txt");
	public AIObject(String name,ArrayList<String> tags) {
		this.name=name;
		this.tags=tags;
	}
	
	//Get info on object
	public String getName() {
		return name;
	}
	public ArrayList<String> getTags(){
		return tags;
	}
	public String getRandomTag() {
		//int i = (int) (Math.random()*tags.size());
		return tags.get((int) (Math.random()*tags.size()));
	}
	public boolean hasTag(String tag) {
		for(String s:tags)
			if(s.compareTo(tag)==0)
				return true;
		return false;
	}
	public boolean isEqual(AIObject o) {
		return o.getName().compareTo(this.getName())==0;
	}
	
	//Static helper classes
	protected static ArrayList<AIObject> getObjectsFrom(String fileName){
		boolean debug=false;
		ArrayList<AIObject> objects = new ArrayList<AIObject>();
		try {
			Scanner file = new Scanner(new FileReader(fileName));	//create scanner file to parse file
			while(file.hasNext()) {
				Scanner line = new Scanner(file.nextLine());	//create scanner line to parse line
				tryStatement:try {
					//attempt to act on line
					
					//identify name
					String name = line.next();
					if(name.substring(0, 2).equals("--") || name.substring(0, 2).equals("//"))
						break tryStatement;	//this is a commented line. Ignore it
					
					//identify tags
					ArrayList<String> tagStrings = new ArrayList<String>();
					if(debug && allowDebug) System.out.println(name);
					Scanner tagTemp = new Scanner(line.next());
					Scanner tags = tagTemp.useDelimiter(",");
					while(tags.hasNext()) {
						//get all tags
						String tag = tags.next();
						if(debug && allowDebug) System.out.println("tag:\t"+tag);
						tagStrings.add(tag);
					}
					tagTemp.close();
					tags.close();
					AIObject newObject = new AIObject(name,tagStrings);
					objects.add(newObject);
					if(debug && allowDebug) System.out.println(newObject);
				}catch(NoSuchElementException ne) {
					//line does not fit expected format. Abort line
				}
				line.close();
			}
			file.close();
		}catch(IOException e) {
			System.err.println("Error retrieving object from "+fileName+"\n"+e);
		}
		return objects;
	}
	public static void printAllObjects() {
		System.out.println("\nALL ACTIONS:");
		for(AIObject i:actions)
			System.out.println(i);
		System.out.println("\nALL CONDITIONS:");
		for(AIObject i:conditions)
			System.out.println(i);
	}
	public String toString() {
		String tagString = "";
		for(int i=0;i<tags.size();i++) {
			if(i>0)
				tagString+=",";
			tagString+=tags.get(i);
		}
		return name+"\ttags: "+tagString;
	}
	public static AIObject getRandomAction() {
		AIObject obj = actions.get((int) (Math.random()*actions.size()));
		if(obj.hasTag("idle"))
			return getRandomAction();
		else
			return obj;
	}
	public static AIObject getRandomCondition() {
		return conditions.get((int) (Math.random()*conditions.size()));
	}
	public static AIObject getActionFor(String action) {
		for(AIObject o:actions)
			if(o.getName().equals(action))
				return o;
		return null;
	}
	public static AIObject getConditionFor(String action) {
		for(AIObject o:conditions)
			if(o.getName().equals(action))
				return o;
		return null;
	}
	public static boolean isAnAction(String word) {
		for(AIObject o:actions)
			if(o.getName().equals(word))
				return true;
		return false;
	}
	public static boolean isACondition(String word) {
		for(AIObject o:conditions)
			if(o.getName().equals(word))
				return true;
		return false;
	}
}
