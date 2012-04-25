import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import exceptions.WFFException;

import utils.FReader;
import utils.Rule;

/**
 * FChaining is class, which simulates reading production system for forward
 * chaining ant it's algorithm itself.
 * 
 * @author Egidijus Lukauskas
 */
public class FChaining {

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
	private static ArrayList<String> setOfFacts = new ArrayList<String>();
	private static String destination;
	private static ArrayList<Rule> resultSetOfRules = new ArrayList<Rule>();
	
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
			int solution = routeFCDestination();
			
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
					for(Rule result : resultSetOfRules) {
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
	
	/**
	 * Gets a rule to apply, applies it for facts and result array. 
	 * Depending on results decides what is the result status and returns it.
	 * 
	 * @return restinationReached	number for destination status: 1 - reached, 
	 * 								-2 & 0 - not reachable, -1 - already in facts.
	 */
	private static int routeFCDestination() {
		Rule rule;
		// Test if destination already in facts.
		int destinationReached = testForDestination();
		while((destinationReached == 0) && !isAllChecked()) {
			// Get next rule to apply.
			rule = takeNextUsableRule();
			if(rule != null) {
				applyForFacts(rule);		// Add new applied facts.
				resultSetOfRules.add(rule);	// Add new Rule to the set of results.
				if(testForDestination() == -1)
					destinationReached = 1; // Destination Reached.
			} else {
				// Destination not reachable.
				destinationReached = -2;
			}
		}
		return destinationReached;
	}

	/**
	 * Add rules results as new facts and sets the rule as already used.
	 * If protocol enabled prints the stack trace.
	 * 
	 * @param rule	Rule object which should be applied for facts.
	 */
	private static void applyForFacts(Rule rule) {
		if(protocol){
			String factString = "pridedamas naujas faktas";
			if(rule.getResults().size()>1) factString = "pridedami nauji faktai";
			System.out.println((protocolStep++)+". Taikoma taisyklė "+rule+
					". Prie faktų "+factString+": "+
					rule.resultsString()+".");
		}
		// Add new fact to the current database.
		setOfFacts.addAll(rule.getResults());
		if(protocol)
			System.out.println("Faktų aibė: {"+printFacts()+"}.");
		rule.setUsed(true);
	}

	/**
	 * Iterates the set of rules from beginning searching for rule that 
	 * can be applied and is not used yet.
	 * 
	 * @return Rule object to be processed as applicable rule.
	 */
	private static Rule takeNextUsableRule() {
		int i = 0;
		// Iterate the set of rules from first object to last, unless one found.
		while(setOfRules.size() > i) {
			// Test if isn't used, conditions are met and results arn't in facts.
			if(!setOfRules.get(i).isUsed() 
					&& setOfRules.get(i).checkConditions(setOfFacts)
						&& !(setOfRules.get(i).isResultsInFacts(setOfFacts))) {
				return setOfRules.get(i);
			}
			i++;
		}
		return null;
	}

	/**
	 * Looks in set of facts to see if there is a destination in it already.
	 * 
	 * @return Number with destination being if facts status: -1 - in facts, 
	 * 														  0 - not in facts.
	 */
	private static int testForDestination() {
		int i = 0;
		int result = 0;
		while((result == 0) && (setOfFacts.size() > i)) {
			if(setOfFacts.get(i++).equals(destination)){
				// Destination already in facts.
				result = -1;
			}
		}
		return result;
	}

	/**
	 * Iterates the set of rules to find out if all the rules are used.
	 * 
	 * @return boolean if all the rules are used.
	 */
	private static boolean isAllChecked() {
		boolean allChecked = true;
		int i = 0;
		while(allChecked && (setOfRules.size() > i)){
			// Until we find not used rule.
			allChecked = allChecked && setOfRules.get(i++).isUsed();
		}
		return allChecked;
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