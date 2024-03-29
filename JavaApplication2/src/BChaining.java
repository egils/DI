import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import utils.FReader;
import utils.Rule;
import exceptions.WFFException;
import java.util.ArrayList;

/**
 * BChaining is class, which simulates reading production system for backward
 * chaining and it's algorithm itself.
 * @author Egidijus Lukauskas
 */
public class BChaining {

	private static FReader reader;
	private static BufferedReader input = 
				new BufferedReader(new InputStreamReader(System.in));
	private static String data = "";
	
	// Printing purpose variables.
	private static boolean debug = false;
	private static boolean protocol = true;
	
	// Program's input-output data.
	private static ArrayList<Rule> setOfRules = new ArrayList<Rule>();
	private static ArrayList<String> setOfFacts = new ArrayList<String>();
	private static ArrayList<String> setOfNewFacts = new ArrayList<String>();
	private static String destination;
	private static ArrayList<Rule> answer = new ArrayList<Rule>();
	
	/**
	 * Main simulation function to control all the functional steps.
	 * @param args	command line arguments
	 */
	public static void main(String[] args) {
		System.out.println("*** Egidijaus Lukausko BChaining"
                + " programa pradeda darbą.");
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

			routeBCDestination(destination,"",1);
            System.out.println("[Programos darbo eigos pabaiga]\n");
			
			if(protocol){
				if(answer.isEmpty() && !setOfNewFacts.isEmpty()) {
					// Destination already in facts.
					System.out.println("Galutinis tikslas jau pateiktas"
                            + " tarp faktų.");
					System.out.println("Uždavinio sprendimas: {}.");
				} else if(answer.isEmpty() && setOfNewFacts.isEmpty()) {
					// Destination not reachable.
					System.out.println("Galutinio tikslo pasiekti"
                            + " neįmanoma.");
					System.out.println("Uždavinio sprendimas duotoje produkcijų"
                            + " sistemoje neegzistuoja.");
				} else if(!answer.isEmpty() && !setOfNewFacts.isEmpty()) {
					// Destination reached.
					System.out.println("Tikslas su duota produkcijų"
                            + " sistema pasiektas!");
                   
                    System.out.print("Uždavinio sprendimas: {");
                    String delimiter = "";
                    // Print set of results.
                    for(Rule result : answer) {
                        System.out.print(delimiter+result.getName());
                        delimiter = "; ";
                    }
                    System.out.print("}.\n");
                }
			}
		}
		
		// System has finished it's calculations.
		System.out.println("\n*** Programa baigia darbą.");  
	}
	
	private static ArrayList<String> testedDestinations = new ArrayList<String>();

    /*
     * Backward Chaining algorythm function. Does not return anything, but 
     * changes the elements of static global variable ArrayList<Rule> answer.
     */
    private static void routeBCDestination(String destination, String spaces, int level) { 
        ArrayList<Rule> rulesToApply = new ArrayList<Rule>();
		int sublevel = 0;
        /* Test if the given destination is not in original facts */
		if(!testInFacts(destination)){
			
                /* Get all the rules with given destination */
                Rule ruleWithDestination;
                while((ruleWithDestination = getDestinationRule(destination)) != null)
                    rulesToApply.add(ruleWithDestination);
                
                /*  Test for DeadEnd */
                if(rulesToApply.isEmpty()) 
                    System.out.println(spaces+level+
                            ". Aklavietė. Nėra taisyklių šio fakto išvedimui.");
                
                /* Test for loop */
                if(testedDestinations.contains(destination)) {
                    System.out.println(spaces+"   Ciklas su tikslu: "+
                            destination+" ------------------------");
                    rulesToApply.clear();
                }
                
                /* Try to apply every rule with given destination */
                for(Rule rule : rulesToApply) {
                    if(!isInNewFacts(destination)){
                        sublevel++;
                        System.out.println(spaces+level+". Einamas tikslas: "+
                                destination+". Rasta taisyklė "+rule+
                                ". Nauji tikslai: "+rule.conditionsString());

                        /* 
                         * Save destinations we already checked to 
                         * beware of loops 
                         */
                        testedDestinations.add(rule.resultsString());

                        /* Test every condition for the rule */
                        for(String condition : rule.getConditions()) {
                            /* If not have this as a fact already */
                            if(!isInNewFacts(condition)) {
                                /* Recoursive call with new destination */
                                routeBCDestination(condition,spaces+"    ",
                                                    level+1);
                            } else {
                                /* Already in facts */
                                System.out.println(spaces+"    "+(level+1)+
                                        ". Tikslas: "+condition+
                                        " jau išvestas anksčiau.");
                            }
                        }
                        
                        /* 
                         * If all conditions are applied add new rule to the
                         * results array
                         */
                        if(rule.checkConditions(setOfNewFacts)) {
                            setOfNewFacts.add(rule.resultsString());
                            answer.add(rule);
                        }
                        
                        /* Restore the rule we just checked */
                        rule.setUsed(false);
                        testedDestinations.remove(rule.resultsString());
                    } 
                }	
		} else { 
            /* Already in original facts */
            System.out.println(spaces+level+". Tikslas "+destination+
                    " yra tarp faktų.");
            setOfNewFacts.add(destination);
        }
	}
	
    /**
     * Checks if given destination is in new facts list.
     * @param destination   String with destination to check
     * @return boolean
     */
    private static boolean isInNewFacts(String destination) {
		for(String fact: setOfNewFacts){
			if(destination.equals(fact)){
				return true;
			}
		}
		return false;
	}

    /**
     * Checks if given destination is in original facts list.
     * @param dest  String with destination to check
     * @return boolean
     */
	private static boolean testInFacts(String dest) {
		for(String fact : setOfFacts) {
			if(fact.equals(dest)) {
				return true;
			}
		}
		return false;
	}
    
    /**
     * Finds 1st unused rule with given destination.
     * @param destination   String with rule restination we need.
     * @return Rule object with given destination and isUsed() == false.
     */
	private static Rule getDestinationRule(String destination) {
		for(Rule rule : setOfRules) {
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
             System.out.println(e.getMessage());
            
		}
		// Create new file reader object with read filename.
		reader = new FReader(data, debug);
	}
}
