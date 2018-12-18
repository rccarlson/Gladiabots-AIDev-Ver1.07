import java.util.ArrayList;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

public class Driver {
	public static boolean showGame=true;
	public static boolean showInRealTime=false;
	public static double speedMult=1;	//[1..5]
	public static int maxThreads=2;
	public static int numTests=10;
	public static int testsPerUpdate=numTests;
	public static double maxDrawRatio=0.05;	//maximum number of 0-0 scores
	public static double percentTransitive = 0.25;
	public static double swapThreshold = 0.05;
	
	//Code config
	public static int maxCodeStorage=20,
							numTransitivePrograms=(int) (maxCodeStorage*percentTransitive),
							digitsInName=2;
	public static BitSet codeIsAvailable = new BitSet(maxCodeStorage);
	public static String fileType = ".txt";

	private static BitSet isFunctional = new BitSet(maxCodeStorage);

	public static ArrayList<Match> matches;
	public static Scanner in = new Scanner(System.in);
	public static int matchesMadeForCurrentCompetition;
	
	public static void main(String[] args) {
		Program.createFullProgramCache();	//ensure all files exist
		
		
		for(int i=0;i<1000;i++) {
			System.out.println("iteration #"+i);
			sortPrograms();
			replaceTransitive();
		}
		
		
		System.exit(0);
	}
	public static double compete(int prog0,int prog1) {
		return compete(prog0,prog1,false);
	}
	public static double compete(int prog0,int prog1,boolean updateDuringMatches) {
		//System.out.println("competing "+prog0+" and "+prog1);
		matches = new ArrayList<Match>();	//holds active matches
		matchesMadeForCurrentCompetition=0;	//reset counter
		ArrayList<Program> programs = new ArrayList<Program>();	//holds 
		programs.add(new Program(prog0)); 
		programs.add(new Program(prog1));
		int[] wins = {0,0};
		int totalWins=0;
		int draws=0;
		boolean mercyRule = false;
		do {
			totalWins=wins[0]+wins[1];
			try {
				while(matches.size()<maxThreads && totalWins/*+matches.size()*/<numTests) {
					//System.out.println("match claimed threads: "+Match.matchesActive);
					Match newMatch = new Match(programs);
					newMatch.start();
					matches.add(newMatch);
				}
			}catch(ConcurrentModificationException e){
				System.err.println("ConcurrentModificationException in Driver.compete while making matches");
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//destroy finished matches
			for(int i=0;i<matches.size();i++) {
				Match m = matches.get(i);
				//System.out.println(m.isAlive());
				if(!m.isAlive()) {
					int winner = m.getWinningTeam();
					if(winner==0 || winner==1) {
						wins[winner]++;
					}else {
						draws++;
					}
					matches.remove(i);
					//System.out.println(programs.get(0).getName()+" vs "+programs.get(1).getName()+" score: "+score(wins));
					if((wins[0]+wins[1])%testsPerUpdate==0 && updateDuringMatches)
						System.out.println(programs.get(0).getName()+" vs "+programs.get(1).getName()
								+"\t"+animatedGoalposts(wins,50)
								+"\t("+score(wins)+")"
								+"\ttests run:"+(wins[0]+wins[1])
								+"\tdraws:"+(draws));
				}
			}
			mercyRule = (wins[0]>7 && wins[1]==0) || (wins[1]>7 && wins[0]==0);
		}while(totalWins<numTests  && (draws<(int)((double)numTests*maxDrawRatio)+1) && !mercyRule);	//continue to operate while wins are under the target and draws are at acceptable levels
		System.out.println(programs.get(0).getName()+" vs "+programs.get(1).getName()+": "+wins[0]+"-"+wins[1]+"\tdraws: "+draws);
		//signal all running threads to stop
		for(int i=0;i<matches.size();i++) {
			matches.get(i).run=false;
		}
		for(int i=0;i<matches.size();i++) {
			matches.remove(i);
		}
		double finalScore = ((double)wins[1])/(double)(wins[0]+wins[1]);
		return finalScore;
	}
	public static void sortPrograms() {
		//primitive bubble search
		boolean debug = false;
		int swaps = 1;
		for(int i=0;i<numTransitivePrograms && swaps>0;i++) {	//recheck for [numTransitivePrograms] loops or until no swaps are made
			swaps = 0;
			for(int codeNum=maxCodeStorage-2;codeNum>=0;codeNum--) {
				double comparison = compete(codeNum,codeNum+1);	//compete the programs and save the comparison score
				matchesMadeForCurrentCompetition=0;
				
				if(debug) System.out.println("comparison of "+codeNum+" and "+(codeNum+1)+":\t"+comparison);
				if(comparison < 1)	//first program won something
					isFunctional.set(codeNum);
				if(comparison > 0) //second program won something
					isFunctional.set(codeNum+1);
				
				if(comparison>0.5) {
					System.out.print("Swapping "+codeNum+" and "+(codeNum+1)+"...");
					boolean a=isFunctional.get(codeNum),
							b=isFunctional.get(codeNum+1);
					Program.swapFiles(codeNum, codeNum+1);
					isFunctional.set(codeNum+1, a);
					isFunctional.set(codeNum, b);
					System.out.println("\tSwap complete");
					swaps++;
				}
				if(debug) System.out.print("functional update:");
				if(debug) printAfter(codeNum);
			}
			System.out.println("On pass #"+i+" made "+swaps+" swaps");
			printFunctional();
			System.out.println(getFunctionalBounds() + " persistent winners");
			if(swaps<2)
				return;
		}
	}
	
	public static void delay(double s) {
		//System.out.println("delay");
		long startTime=System.currentTimeMillis();
		while((System.currentTimeMillis()-startTime) < s*1000.0) {
			
		}
	}
	public static void loopUntilNoMatches() {
		while(Match.matchesActive != 0) {
			//System.out.println(Match.matchesActive);
			delay(0.01);
		}
	}
	public static void loopUntilEndWithFailsafe(double s) {
		long startTime=System.currentTimeMillis();
		while((System.currentTimeMillis()-startTime) < s*1000.0 && Match.matchesActive != 0) {
			delay(0.01);
		}
	}
	public static double score(int t0,int t1) {
		return ((double)t1)/(double)(t0+t1);
	}
	public static double score(int[] scores) {
		return Math.round(((double)scores[1])/(double)(scores[0]+scores[1])*100.0)/100.0;
	}
	public static String animatedGoalposts(int[] scores,int width) {
		String s = "|";
		for(int i=0;i<width*score(scores);i++)
			s+=".";
		s+="^";
		for(int i=s.length()-2;i<width;i++)
			s+=".";
		return s+"|";
	}
	public static void replaceTransitive() {
		if(maxCodeStorage-numTransitivePrograms<2) {
			System.err.println("No bots will be saved. Aborting");
			System.exit(0);
		}
		for(int i=maxCodeStorage-numTransitivePrograms;i<maxCodeStorage;i++) {
			String directory = "bots/bot"+Program.getBotNumString(i)+".txt";
			//System.out.println("storing new program in '"+directory+"'");
			Program.generateAndStoreNewProgram(directory);
		}
	}
	
	public static void printFunctional() {
		printFunctionalBetween(0,maxCodeStorage);
	}
	public static void printFunctionalBetween(int min, int max) {
		int operatingMax = (max>maxCodeStorage) ? maxCodeStorage : max;
		for(int i=min;i<operatingMax;i++)
			System.out.print((isFunctional.get(i)) ? "1" : "0");
		System.out.println("");
	}
	public static void printAfter(int afterNum) {
		String out = "";
		for(int i=0;i<afterNum;i++) {
			out+=" ";
		}
		System.out.print(out);
		printFunctionalBetween(afterNum,maxCodeStorage);
	}
	public static double getFunctionalRatioUpTo(int minBot,int maxBot) {
		if(maxBot-minBot<1) return 0;
		int functional=0;
		for(int i=minBot;i<maxBot;i++) {
			if(isFunctional.get(i)) functional++;
		}
		return (double)functional / (double)(maxBot-minBot);
	}
	public static int getFunctionalBounds() {
		boolean debug=false;
		String testOutput="";
		double lastRatio = 0;
		for(int i=0;i<maxCodeStorage;i++) {
			double ratio = getFunctionalRatioUpTo(0,i);
			if(ratio > 0.95) {
				testOutput+="^";
				lastRatio=ratio;
			}else{
				testOutput+="_";
				if(lastRatio>ratio) {
					if(debug) System.out.println(testOutput);
					if(debug) System.out.println("recommend bot #"+(i-1)+"as boundary");
					return i;
				}
			}
		}
		return -1;
		//System.exit(0);
	}
}
