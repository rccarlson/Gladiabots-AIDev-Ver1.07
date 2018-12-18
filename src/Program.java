import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class Program {
	private String formattedProg="",prog="";
	private static double generationProbability=0.5;
	private static boolean allowDebug = true;
	private static final String fileType=Driver.fileType;
	private String name;
	
	public Program() {
		formattedProg=createProgram();
		this.name="GeneratedAI";
	}
	public Program(String progName) {
		retrieveProgram(progName);
		this.name=progName;
	}
	public Program(int progNum) {
		//System.out.println(getBotNumString(progNum));
		this.name="bot"+getBotNumString(progNum);
		retrieveProgram(this.name);
	}
	public void retrieveProgram(String progName) {
		try{
			Scanner s = new Scanner(new FileReader("bots/"+progName+fileType));
			while(s.hasNext()) {
				formattedProg+=s.nextLine()+"\n";
				//System.out.println(s.next());
			}
			s.close();
			Scanner s2 = new Scanner(formattedProg);
			while(s2.hasNext()) {
				prog+=s2.next();
			}
			s2.close();
		}catch (IOException e){
			System.err.println("Failed to retrieve program "+progName);
		}
	}
	
	//generate AI
	public static String createProgram() {
		String tempProg = createProgram(0);
		if(tempProg.length()<100000) {
			return tempProg;
		}else {
			return createProgram();
		}
	}
	private static String createProgram(int indentations) {
		String prog="";
		if(Math.random()<generationProbability ||  indentations>15) {
			//perform an action
			prog += indent(indentations)+AIObject.getRandomAction().getName()+";\n";
		}else {
			//run a conditional if statement
			AIObject obj = AIObject.getRandomCondition();
			String conditions = obj.getName()+"=="+obj.getRandomTag();
			while(generationProbability > Math.random()) {
				if(Math.random()>0.5)
					conditions+="&&";
				else
					conditions+="||";
				conditions+=obj.getName()+"=="+obj.getRandomTag();
			}
			prog += indent(indentations)+"if("+conditions+"){\n"+
					createProgram(indentations+1)+
					indent(indentations)+"}\n";
		}
		if(Math.random()<generationProbability && indentations<20) {
			//add more to program
			prog += createProgram(indentations);
		}
		return prog;
	}
	private static String indent(int indentations) {
		String indentation="";
		for(int i=0;i<indentations;i++)
			indentation+="\t";
		return indentation;
	}
	public static ArrayList<Program> generateNewPrograms(int numTeams){
		ArrayList<Program> progs = new ArrayList<Program>();
		for(int i=0;i<numTeams;i++) {
			progs.add(new Program());
		}
		return progs;
	}
	public static void createFullProgramCache() {	//makes sure a file exists for all bots up to maxCodeStorage
		//System.out.println(botFileExists(0));
		for(int i=0;i<Driver.maxCodeStorage;i++)
			checkSingleProgramCache(i);
	}
	private static void checkSingleProgramCache(int progNum) {	//if progNum does not exist, generates a program and stores it
		//System.out.println("check single");
		String directory = "bots/bot"+getBotNumString(progNum,Driver.digitsInName)+Driver.fileType;
		if(!botFileExists(progNum)) {	//file does not exist
			generateAndStoreNewProgram(directory);
		}
	}
	public static void generateAndStoreNewProgram(String directory) {
		try {
			//System.out.println("make writer");
			PrintWriter writer = new PrintWriter(directory,"UTF-8");
			String prog = createProgram();
			Scanner progScanner = new Scanner(prog);
			while(progScanner.hasNext()) {
				writer.println(progScanner.nextLine());
				//System.out.println(progScanner.nextLine());
			}
			progScanner.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	//reading supporting functions
	public static boolean isLetter(char c) {
		int ascii = (int)c;
		return (ascii>=65 && ascii<=90) || (ascii>=97 && ascii<=122);
	}
	public static boolean isNumber(char c) {
		int ascii = (int)c;
		return (ascii>=48 && ascii<=57);
	}
	public static String firstWord(String input) {
		String word = "";
		for(int i=0;i<input.length();i++) {
			if(isLetter(input.charAt(i))) {
				word+=input.charAt(i);
			}else {
				break;
			}
		}
		return word;
	}
	public static String afterFirstWord(String input) {
		if(input.length()==0) return "";
		//if(Math.abs(firstWord(input).length()-input.length())<2) return "";
		String out="";
		try{
			out=input.substring(firstWord(input).length()+1, input.length());
		}catch(StringIndexOutOfBoundsException e) {
			System.err.println("Program.afterFirstWord:\treceived input '"+input+"'");
		}
		return out;
	}
	public boolean evaluateFullCondition(String fullConditional,Bot requestor) {
		ArrayList<String> conditions = new ArrayList<String>();
		ArrayList<String> comparators = new ArrayList<String>();
		String newCondition="";
		for(int i=0;i<fullConditional.length();i++) {
			char c = fullConditional.charAt(i);
			if(isLetter(c) || c=='=' || isNumber(c)) {
				newCondition+=fullConditional.charAt(i);
			}else {
				conditions.add(newCondition.trim());
				newCondition = "";
				while(!(isLetter(c) || c=='=' || isNumber(c))) {
					c = fullConditional.charAt(i);
					i++;
				}
				i-=2;
				comparators.add(fullConditional.substring(i-1, i+1));
			}
		}
		conditions.add(newCondition.trim());
		/*for(String s:conditions) {
			//System.out.println("condition:\t"+s);
			//System.out.println(evaluateSingleCondition(s,requestor));
		}
		for(String s:comparators) {
			//System.out.println("comparator:\t"+s);
		}*/
		boolean result=true;
		for(int i = 0;i<conditions.size();i++) {
			if(i==0) {
				result=evaluateSingleCondition(conditions.get(i),requestor);
			}else {
				String comparator = comparators.get(i-1);
				if(comparator.compareTo("&&")==0) {	//use and comparison
					result &= evaluateSingleCondition(conditions.get(i),requestor);
				}else if(comparator.compareTo("||")==0) {	//use or comparison
					result |= evaluateSingleCondition(conditions.get(i),requestor);
				}else {
					System.err.println("Unknown comparison type '"+comparator+"'");
				}
			}
		}
		return result;
	}
	public boolean evaluateSingleCondition(String condition,Bot requestor) {
		String var = "",numberString="";
		for(int i=0;i<condition.length();i++) {
			char c = condition.charAt(i);
			if(isLetter(c)) {
				var+=c;
			}else if(isNumber(c)) {
				numberString+=c;
			}
		}
		int num = Integer.parseInt(numberString);
		//System.out.println("variable:\t"+var);
		//System.out.println("evaluates to:\t"+num);
		//System.out.println("returned: "+Condition.evaluateCondition(var, requestor));
		return Condition.evaluate(var, requestor) == num;
	}
	public boolean isAllowableAction(String action, Bot requestor) {
		if(!AIObject.isAnAction(action)) {
			System.err.println(action+" is not an allowable action becasuse it does not exist");
			return false;
		}
		AIObject a = AIObject.getActionFor(action);
		if(a.hasTag("resourceCollect") && requestor.carriedResource!=null) {
			//trying to run a command to collect a resource while carrying a resource. command is illegal
			return false;
		}
		if(a.hasTag("resourceCollect") && requestor.resources.size()<1) {
			//trying to run a command to collect a resource while no resources are available to collect. command is illegal
			return false;
		}
		if(a.hasTag("scoreResource") && requestor.carriedResource==null) {
			//trying to score a resource while not carrying one
			return false;
		}
		if(a.hasTag("allyUnderAttack") && requestor.getClosestAllyUnderAttack()==null) {
			//trying to find ally under attack, but there is none
			return false;
		}
		if(a.hasTag("enemyUnderAttack") && requestor.getClosestEnemyUnderAttack()==null){
			//trying to find enemy under attack, but there is none
			return false;
		}
		if(a.hasTag("underAttack") && !requestor.isUnderAttack()) {
			//trying to find attacking enemies, but there are none
			return false;
		}
		if(a.hasTag("enemy") && requestor.numEnemies()==0) {
			//trying to find enemy, but there are none
			return false;
		}
		if(a.hasTag("ally") && requestor.numAllies()==0) {
			//trying to find enemy, but there are none
			return false;
		}
		return true;
	}
	public static String getConditionalFromIf(String input) {
		if(!hasIfStatement(input)) {
			System.err.println("tried to get conditional from string that does not lead with an if");
			return null;
		}
		String upToBracket = input.substring(0, input.indexOf("{"));
		String conditionalString = upToBracket.substring(upToBracket.indexOf("(")+1,upToBracket.indexOf(")"));
		return conditionalString;
	}
	public static int indexOfEndIf(String code) {
		int layer=1;
		int index=-1;
		for(int i=code.indexOf("{")+1;layer>0;i++) {
			index=i;
			if(code.charAt(i)=='{') {
				layer++;
			}else if(code.charAt(i)=='}') {
				layer--;
			}
		}
		return index;
	}
	public static boolean hasIfStatement(String input) {
		int indexOfFirstBracket = input.indexOf("{");
		int indexOfFirstIf = input.indexOf("if");
		if(indexOfFirstBracket==-1 || indexOfFirstIf>indexOfFirstBracket)	//no brackets, so no if statement
			return false;
		String upToBracket = input.substring(0, indexOfFirstBracket);
		return upToBracket.indexOf("if(")!=-1;
	}
	
	//evaluation
	public void evaluate(Bot bot) {
		boolean debug = false;
		String temp = prog;
		if(debug && allowDebug) System.out.println("Starting evaluation of: "+temp);
		if(!evaluate(bot,temp + "idle;")) {
			System.err.println("Program.evaluate: Failed to evaluate bot "+bot.getid()+" with program '"+temp+"'");
		}
		if(debug && allowDebug) System.out.println("end evaluation\n");
	}
	private boolean evaluate(Bot bot,String code) {
		boolean debug=false;
		boolean debugReturnDefiniteResult=false;
		boolean hasExecuted=false;
		if(code.trim().length()==0) {
			if(debug) System.out.println("null code");
			return true;
		}
		String word = firstWord(code);
		String afterWord = afterFirstWord(code);
		if(debug && allowDebug) System.out.println("First word: '"+word+"'");
		//System.out.println("After word: "+afterWord);
		
		//try to execute the first statement as an action
		if(debug && allowDebug) System.out.println("First word is an action: "+AIObject.isAnAction(word));
		if(AIObject.isAnAction(word)) {
			if(debug && allowDebug) System.out.println(word + " is an action. Check for permission to execute");
			if(isAllowableAction(word,bot)) {
				if(debugReturnDefiniteResult && allowDebug) System.out.println(word + " is allowable. Attempt to execute");
				bot.setAction(AIObject.getActionFor(word));	//set action on bot
				return true;	//report set as successful. D not set hasExecuted because this function is over and var is irrelevant
			}else {
				if(debugReturnDefiniteResult && allowDebug) System.out.println(word + " is NOT allowable. Cannot execute");
				//statement is an action, but is not allowable at this time
				//evaluate after this failed statement
				hasExecuted |= evaluate(bot,afterWord);
			}
			if(hasExecuted) return true;
		}
		//first statement is either not an action or is not an allowable action
		//try to evaluate first statement as an if statement
		if(debug && allowDebug) System.out.println("failed to evaluate as an action. attempting to evaluate as an if statement");
		if(word.compareTo("if")==0) {
			//first word is an if statement
			if(debug && allowDebug) System.out.println("statement is an if statement");
			String fullConditional = getConditionalFromIf(code);	//pull conditional statement from the code
			if(evaluateFullCondition(fullConditional,bot)) {
				//conditional statement was true
				//evaluate code inside
				String insideCode = code.substring(code.indexOf("{")+1, indexOfEndIf(code));
				if(debug && allowDebug) System.out.println("Code from inside if statement: "+insideCode);
				hasExecuted |= evaluate(bot,insideCode);
			}else {
				//conditional statement was false
				//evaluate code after if statement
				String outsideCode = code.substring(indexOfEndIf(code)+1,code.length());
				if(debug && allowDebug) System.out.println("Code from outside if statement: "+outsideCode);
				if(debug && allowDebug) System.out.println("Starting recursion for outside code");
				hasExecuted |= evaluate(bot,outsideCode);
				if(debug && allowDebug) System.out.println("Ending recursion");
			}
			if(hasExecuted) return true;
		}
		//first statement is either an non-allowed action or an if statement with no effect
		return false;
	}
	
	//Static functions
	private static boolean botFileExists(int botNum) {
		String directory = "bots/bot"+getBotNumString(botNum)+Driver.fileType;
		//System.out.println(directory);
		File f = new File(directory);
		return f.exists() && !f.isDirectory();
	}
	public static String getBotNumString(int botNum) {
		return getBotNumString(botNum,Driver.digitsInName);
	}
	public static String getBotNumString(int botNum,int maxDigits) {
		String s = "";
		s+=botNum/(int)Math.pow(10, maxDigits-1);
		if(maxDigits>1)
			s+=getBotNumString(botNum%(int)Math.pow(10, maxDigits-1),maxDigits-1);
		return s;
	}
	public static boolean isValidProgram(Program prog) {
		return isValidProgram(prog.toString());
	}
	public static boolean isValidProgram(String code) {
		boolean debug = false;
		code=code.trim();
		if(code.trim().length()==0)
			return true;
		String word = firstWord(code);
		String afterWord = afterFirstWord(code).trim();

		if(debug) System.out.println("Program.isValidProgram:\tword: '"+word+"'");
		if(debug) System.out.println("Program.isValidProgram:\taftwerWord: '"+afterWord+"'");
		//categorize the first item
		if(AIObject.isAnAction(word)) {
			//first item is an action
			if(debug) System.out.println("Program.isValidProgram:\t"+word+" is an action");
			//if there is more to the program, analyze it
			return isValidProgram(afterWord);
		}else if(word.equals("if")) {
			//first item is an if statement
			String fullConditional = getConditionalFromIf(code);	//pull conditional statement from the code
			if(debug) System.out.println("Program.isValidProgram:\tfound if statement. need to analyze '"+fullConditional+"' as valid condition");
			if(isValidCondition(fullConditional)) {
				//conditional statement is valid
				if(debug) System.out.println("Program.isValidProgram:\tconditions valid");
				String insideCode = code.substring(code.indexOf("{")+1, indexOfEndIf(code));
				String outsideCode = code.substring(indexOfEndIf(code)+1,code.length());
				if(debug) System.out.println("Code from inside if statement: '"+insideCode.trim()+"'");
				if(debug) System.out.println("Code from outside if statement: '"+outsideCode.trim()+"'");
				return isValidProgram(insideCode) && isValidProgram(outsideCode);	//return the status of the inside and outside code
			}else {
				return false;
			}
		}else {
			System.err.println("Program.isValidProgram:\tcannot analyze '"+word.trim()+"'");
			return false;
		}
	}
	private static boolean isValidCondition(String code) {
		String tempCode="";
		for(int i=0;i<code.length();i++) {
			if(code.charAt(i)!=' ') {
				tempCode+=code.charAt(i);
			}
		}
		return isValidCondition(tempCode,true);
	}
	private static boolean isValidCondition(String code,boolean isRoot) {
		boolean debug = false;
		if(code.length()==0 && !isRoot) return true;
		if(code.length()==0 && isRoot) return false;
		String condition="";
		String operator="";
		String after="";
		boolean onFirst=true,onFirstOp=true;
		for(int i=0;i<code.length();i++) {
			char c = code.charAt(i);
			if(onFirst && isLetter(c)) {
				condition+=c;
			}else if(onFirst && c=='=') {
				operator+=code.charAt(i+2);
				i+=2;
				onFirst=false;
			}else if(onFirstOp) {
				i+=1;
				onFirstOp=false;
			}else {
				after+=c;
			}
		}
		if(debug) System.out.println("condition:\t'"+condition+"'\noperator:\t'"+operator+"'\nafter\t'"+after+"'");
		if(AIObject.isACondition(condition)) {
			if(debug) System.out.println(condition+" is a valid condition");
			AIObject cond = AIObject.getConditionFor(condition);
			if(cond.hasTag(operator)) {
				if(debug) System.out.println(condition+" does have tag "+operator);
				return isValidCondition(after,false);
			}else {
				if(debug) System.out.println(condition+" does NOT have tag "+operator);
				return false;
			}
		}else {
			if(debug) System.out.println(condition+" is NOT a valid condition");
			return false;
		}
		//System.out.println("isValid:\t'"+validName+"'\noperator:\t'"+operator+"'\nafter\t'"+after+"'");
	}
	public static void swapFiles(int prog0num,int prog1num) {
		//fetch programs
		String aDirectory ="bots/bot"+getBotNumString(prog0num)+fileType;
		String bDirectory="bots/bot"+getBotNumString(prog1num)+fileType;
		Program a = new Program(prog0num);
		Program b = new Program(prog1num);
		
		//store prog0
		try {
			PrintWriter writerB = new PrintWriter(bDirectory,"UTF-8");	//create file writer
			Scanner progAScanner = new Scanner(a.toString());	//create reader for program a
			while(progAScanner.hasNext()) {		//while there are more lines to copy
				writerB.println(progAScanner.nextLine());	//copy line into file b
			}
			progAScanner.close();	//close scanners
			writerB.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		//store prog1
		try {
			PrintWriter writerA = new PrintWriter(aDirectory,"UTF-8");	//create file writer
			Scanner progBScanner = new Scanner(b.toString());	//create reader for program b
			while(progBScanner.hasNext()) {		//while there are more lines to copy
				writerA.println(progBScanner.nextLine());	//copy line into file a
			}
			progBScanner.close();	//close scanners
			writerA.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return formattedProg;
	}
	public String getName() {
		return name;
	}
}
