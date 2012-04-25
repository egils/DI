import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utils.FReader;
import utils.Rule;
import exceptions.WFFException;


public class BChaining {

	private static FReader reader;
	private static BufferedReader input = 
				new BufferedReader(new InputStreamReader(System.in));
	private static String data = "";
	
	// Printing purpose variables.
	private static boolean debug = false;
	private static boolean protocol = true;
	private static int protocolStep = 1;
	
	// Program's input-output data.
	private static ArrayList<Rule> setOfRules = new ArrayList<Rule>();
	private static ArrayList<Rule> loopedRules = new ArrayList<Rule>();

	private static ArrayList<String> setOfFacts = new ArrayList<String>();
	private static ArrayList<String> setOfNewFacts = new ArrayList<String>();
//	private static Map<Rule, Integer> loopedRules = new HashMap<Rule,Integer>();
	private static String destination;
	private static ArrayList<Rule> answer = new ArrayList<Rule>();
	
	/**
	 * Main simulation function to control all the functional steps.
	 * @param args	command line arguments
	 */
	public static void main(String[] args) {
		System.out.println("*** Egidijaus Lukausko FChaining programa pradeda darbą.");
		try {
			// Initializing reader object.
			if(args.length == 0) {
				setupFReader();
			} else {
				setupFReader(args[0]);
			}
		} catch (WFFException wffe) {
			// Wrong File Format Exception occurred.
			reader = null;
			System.out.println(wffe.getMessage());
		}
		
		if(reader != null) {
			if(debug)
				System.out.println("*** Failo skaitymas baigtas.\n");
			
			// Get data from recently read file to simulator.
			setOfRules = reader.getSetOfRules();
			setOfFacts = reader.getSetOfFacts();
			destination = reader.getDestination();
			
			// Print out production system.
			System.out.println("Produkcijos:\n");
			printRules();
			System.out.println("\nDuomenys:");
			System.out.println(printFacts());
			System.out.println("\nTikslas:");
			printDestination();
			if(protocol)
				System.out.println("\n[Programos darbo eiga]");
			
			// Call solver function to get the result status.
			int solution = 1;
			
			backward(destination,"",1,setOfRules);
			
			if(protocol){
				if(solution == -1) {
					// Destination already in facts.
					System.out.println((protocolStep++)+
							". Galutinis tikslas jau pateiktas tarp faktų.");
					System.out.println(
							"Uždavinio sprendimas: {}.");
				} else if((solution == -2) || (solution == 0)) {
					// Destination not reachable.
					System.out.println((protocolStep++)+
							". Galutinio tikslo pasiekti neįmanoma.");
					System.out.println(
							"Uždavinio sprendimas duotoje produkcijų sistemoje neegzistuoja.");
				} else if(solution == 1) {
					// Destination reached.
					System.out.println((protocolStep++)+
							". Tikslas su duota produkcijų sistema pasiektas!");
					System.out.print("Uždavinio sprendimas: {");
					String delimiter = "";
					// Print set of results.
					for(Rule result : answer) {
						System.out.print(delimiter+result.getName());
						delimiter = "; ";
					}
					System.out.print("}.\n");
				}
				System.out.println("[Programos darbo eigos pabaiga]");
			}
		};
		
		// System has finished it's calculations.
		System.out.println("\n*** Programa baigia darbą.");
	}
	
	private static int lastUsedLevel = 0;
	private static ArrayList<String> testedDestinations = new ArrayList<String>();
	private static boolean end = false;
	private static void backward(String destination, String spaces, int level,
			ArrayList<Rule> rules) {
		Rule rule;
		boolean found = false;
		if(!testInFacts(destination,level)){
			if(isInNewFacts(destination)) {
				end = true;
				System.out.println(spaces+level+". Einamas tikslas " + destination + " išvestas anksčiau.");
				populateNewFacts(destination,level);
				for(int i=0; i<answer.size(); i++ ){
					if(answer.get(i).resultsString().equals(destination)){
						rule = answer.get(i);
						answer.remove(i);
						answer.add(0, rule);
					}
				}
				System.out.print("Uždavinio sprendimas: {");
				String delimiter = "";
				// Print set of results.
				for(Rule result : answer) {
					System.out.print(delimiter+result.getName());
					delimiter = "; ";
				}
				System.out.print("}.\n");
				System.exit(1);
			} else {
				testedDestinations.add(destination);
				while((rule = getDestinationRule(destination, rules)) != null) {
					System.out.println(spaces+level+". Einamas tikslas: "+destination+". Rasta taisykle "+rule+". Nauji tikslai: "+rule.conditionsString());
					answer.add(0,new Rule(rule, level));
					for(String cond : rule.getConditions()) {
						if(testForLoop(cond,rules)){
							answer.remove(0);
							if(lastUsedLevel > level)
								answer.remove(0);
							lastUsedLevel = level;
							System.out.println(spaces+"    "+(level+1)+". Tikslas: "+cond+". <<<<<<<<<<<Ciklas>>>>>>>>>>");
						} else {
//							loopedRules.put(rule, level);
							loopedRules.add(new Rule(rule, level));
							if(testInFacts(cond, level)){
								System.out.println(spaces+"  Tikslas "+cond+" yra tarp faktų.");
								System.out.print("Uždavinio sprendimas: {");
								String delimiter = "";
								// Print set of results.
								for(Rule result : answer) {
									System.out.print(delimiter+result.getName());
									delimiter = "; ";
								}
								System.out.print("}.\n");
							} else {
								backward(cond, spaces+"    ", level+1, cloneList(rules));
								if(testInFacts(cond, level)){
									found = true;
									break;
								}
							}
						}
					}
					if(found)
						break;
				}
				
				if(isDeadEnd(destination)){
					System.out.println(spaces+level+". Pasiekta aklavietė su tikslu: " + destination + ".");
				}
			}
		}
	}
	
	private static void removeLoopsFromLevel(int level) {
		int i = 0;
		while(i<loopedRules.size()) {
			if(loopedRules.get(i).level >= level){
				loopedRules.remove(i);
				System.out.println("removed");
			} else {
				i++;
			}
		}
//		for(Map.Entry<Rule, Integer> lRule2 : loopedRules.entrySet()) {
//			
//		}
	}
	
	private static void removeAnswersFromLevel(int level) {
		int i = 0;
		while(i<answer.size()) {
			if(answer.get(i).level >= level){
				System.out.println("removed answer"+answer.get(i));
				answer.remove(i);
			} else {
				i++;
			}
		}
//		for(Map.Entry<Rule, Integer> lRule2 : loopedRules.entrySet()) {
//			
//		}
	}

	private static boolean isInNewFacts(String destination) {
		for(String fact: setOfNewFacts){
			if(destination.equals(fact)){
				return true;
			}
		}
		return false;
	}

	private static boolean testInFacts(String dest, int level) {
		for(String fact : setOfFacts) {
			if(fact.equals(dest)) {
				populateNewFacts(dest, level);
				return true;
			}
		}
		return false;
	}
	

	private static boolean testInFacts(String dest) {
		for(String fact : setOfFacts) {
			if(fact.equals(dest)) {
				return true;
			}
		}
		return false;
	}
	
	private static void populateNewFacts(String dest, int level) {
		addNewDest(dest);
//		System.out.println(loopedRules.size());
//		for(Map.Entry<Rule, Integer> lRule2 : loopedRules.entrySet()) {
//			System.out.print(lRule2.getKey()+ " ");
//		}
//		System.out.println(loopedRules.entrySet().size());
//		System.out.println();
		for(int i=level;i>0;i--){
//			for(Map.Entry<Rule, Integer> lRule : loopedRules.entrySet()) {
			for(Rule lRule : loopedRules) {
				int count = 0;
//				System.out.println("cia");
				for(String faktas: lRule.getConditions()){
					for(String row: setOfNewFacts){
						if(faktas.equals(row)){
							count++;
						}
					}
				}
				if(count == lRule.getConditions().size()){
					addNewDest(lRule.resultsString());	
				}	
			}
		}
	}

	private static void addNewDest(String dest) {
		boolean toAdd=true;
		for(String d : setOfNewFacts){
			if(d.equals(dest)) 
				toAdd=false;
		}
		if(toAdd)
			setOfNewFacts.add(dest);		
	}

	public static boolean isDeadEnd(String dest){
		boolean test = false;
		
		for(int i=0; i<setOfRules.size(); i++){
			if(setOfRules.get(i).resultsString().equals(dest)){
				test = true;
			}
		}
		if(!test){
			if(answer.size() > 0){	
				answer.remove(0);
				if(answer.size() > 0){	
					answer.remove(0);
				}
			}
			return true;
		}
		return false;
	}

	public static ArrayList<Rule> cloneList(ArrayList<Rule> list) {
	    ArrayList<Rule> clone = new ArrayList<Rule>(list.size());
	    for(Rule rule : list) {
	    	clone.add(new Rule(rule));
	    }
	    return clone;
	}

	private static boolean testForLoop(String cond, ArrayList<Rule> rules) {
		for(Rule rule : rules) {
			if(rule.isUsed() && rule.checkResultsForDest(cond)) {
				return true;
			}
		}
		return false;
	}

	private static Rule getDestinationRule(String destination,
			ArrayList<Rule> rules) {
		for(Rule rule : rules) {
			if(rule.checkResultsForDest(destination) && rule.isUsed() == false) {
				rule.setUsed(true);
				return rule;
			}
		}
		return null;
	}


	/**
	 * Iterates the set of rules and prints every element in it.
	 */
	private static void printRules() {
		for(Rule rule : setOfRules) {
			System.out.println(rule);
		}
	}
	
	/**
	 * Iterates the set of facts and prints names of every element in it.
	 */
	private static String printFacts() {
		String facts = "";
		String delimiter = "";
		for(String cond : setOfFacts) {
			facts += delimiter+cond;
			delimiter = ", ";
		}
		return (facts);
	}
	
	/**
	 * Prints destination.
	 */
	private static void printDestination() {
		System.out.println(destination);
	}

	/**
	 * Gets filename from command line argument, pass it to reader object, which
	 * is created right here.
	 * 
	 * @param filename		String with name of file to read.
	 * @throws WFFException	Wrong File Format Exception.
	 */
	private static void setupFReader(String filename) throws WFFException {
		System.out.println("*** Naudojamas nurodytas failas „"+
				filename+"“.");
		// Create new file reader object with given filename.
		reader = new FReader(filename.trim(), debug);	
	}

	/**
	 * Gets filename from stdin, pass it to reader object, which is created.
	 * @throws WFFException
	 */
	private static void setupFReader() throws WFFException {
		try {
			// Read filename.
			System.out.println("*** Įveskite duomenų failo pavadinimą \n"+
					"*** (arba "+ 
					"spauskite „Enter“, kad naudoti standartinį "+
					"„produkcijos.txt“):");
			data = input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Create new file reader object with read filename.
		reader = new FReader(data, debug);
	}
}
