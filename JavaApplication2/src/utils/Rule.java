package utils;

import java.util.ArrayList;

/**
 * Rule object is designed to store the lists of conditions and results.
 * It represents the rule being used in production system for forward chaining.
 * 
 * @author Egidijus Lukauskas
 */
public class Rule {
	/*
	 * Private items
	 */
	// Rule data.
	private ArrayList<String> conditions = new ArrayList<String>();
	private ArrayList<String> results = new ArrayList<String>();
	// Default values.
	private boolean debug = false; 
	private String name = "";
	private boolean used = false;
	
	/*
	 * Constructors
	 */
	public Rule(String name, String rule) {
		this.parseRuleString(rule);
		this.name = name;
		if(debug)
			System.out.println("Pridėta nauja taisyklė: "+this);
	}
	
	public Rule(String name, String rule, boolean debug) {
		this.parseRuleString(rule);
		this.name = name;
		this.debug = debug;
		if(this.debug)
			System.out.println("Pridėta nauja taisyklė: "+this);
	}
	
	public Rule(String name, String conditions, String results) {
		this.fillResults(results);
		this.fillConditions(conditions);
		this.name = name;
		if(debug)
			System.out.println("Pridėta nauja taisyklė: "+this);
	}
	
	/*
	 * Public methods
	 */
	
	/**
	 * Change current rule's data with other.
	 * @param rule	Rule object from which to read new data
	 */
	public void changeRule(String rule) {
		this.conditions = new ArrayList<String>();
		this.results = new ArrayList<String>();
		this.parseRuleString(rule);
	}
	
	/**
	 * Change current rule's data with other.
	 * @param conditions	String for conditions to be parsed
	 * @param results		String for results to be parsed
	 */
	public void changeRule(String conditions, String results) {
		this.conditions = new ArrayList<String>();
		this.results = new ArrayList<String>();
		this.fillResults(results);
		this.fillConditions(conditions);
	}
	
	/**
	 * Iterates all the conditions of this rule and test if rule can be applied
	 * by looking for these conditions in set of facts given.
	 * @param setOfFacts	Set of facts to test if the conditions are met
	 * @return	boolean if rule can be applied
	 */
	public boolean checkConditions(ArrayList<String> setOfFacts) {
		boolean ruleApplies = true;
		boolean foundForCondition; int i;
		for(String cond : this.conditions) {
			// Take every condition form list and look for it in set of facts.
			foundForCondition = false;
			i = 0;
			while(!foundForCondition && (setOfFacts.size() > i)) {
				if(setOfFacts.get(i++).equals(cond)) {
					// Condition is found in facts.
					foundForCondition = true;
				}
			}
			// If all conditions are found rule applies.
			ruleApplies = ruleApplies && foundForCondition;
		}
		return ruleApplies;
	}
	
    /**
     * Checks results for given destination.
     * @param destination   String with destination
     * @return boolean
     */
	public boolean checkResultsForDest(String destination) {
		boolean ruleApplies = false;
		for(String result : this.results) {
			if(result.equals(destination)){
				ruleApplies = true;
			}
		}
		return ruleApplies;
	}
	
	/**
	 * Test if rule's results are already in set of facts given.
	 * @param setOfFacts	Set of facts to rest if results are in it
	 * @return	boolean if all the results are in set of facts
	 */
	public boolean isResultsInFacts(ArrayList<String> setOfFacts) {
		boolean isInFacts = true;
		boolean foundForResult; int i;
		// Take every result from list and look for it in set of facts.
		for(String result : this.results) {
			foundForResult = false;
			i = 0;
			while(!foundForResult && (setOfFacts.size() > i)) {
				if(setOfFacts.get(i++).equals(result)) {
					// Result is found in facts.
					foundForResult = true;
				}
			}
			// If all the results are found rule is in facts.
			isInFacts = isInFacts && foundForResult;
		}
		return isInFacts;
	}
	
	/*
	 * Setters and getters.
	 */
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isUsed() {
		return this.used;
	}
	
	public void setUsed(boolean used) {
		this.used = used;
	}
	
    @Override
	public String toString() {
		String retVal = this.name+": ";
		String delimiter = "";
		for(String cond : this.conditions) {
			retVal += delimiter+cond;
			delimiter = ", ";
		}
		
		retVal += " -> ";
		
		delimiter = "";
		for(String result : this.results) {
			retVal += delimiter+result;
			delimiter = ", ";
		}
		return retVal; 
	}
	
	public String resultsString() {
		String retVal = "";
		String delimiter = "";
		for(String result : this.results) {
			retVal += delimiter+result;
			delimiter = ", ";
		}
		return retVal;
	}
	
	public String conditionsString() {
		String retVal = "";
		String delimiter = "";
		for(String result : this.conditions) {
			retVal += delimiter+result;
			delimiter = ", ";
		}
		return retVal;
	}

	public ArrayList<String> getResults() {
		return this.results;
	}
	
	public ArrayList<String> getConditions() {
		return this.conditions;
	}

	/*
	 * Wrappers
	 */
	private void parseRuleString(String rule) {
		this.fillResults(rule.substring(0, 1));
		this.fillConditions(rule.substring(1));
	}
	
	/*
	 * Utility methods
	 */
	private void fillConditions(String conditions) {
		for (int i=0;i<conditions.length();i++) {
			this.conditions.add(conditions.substring(i, i+1));
		}
	}

	private void fillResults(String results) {
		for (int i=0;i<results.length();i++) {
			this.results.add(results.substring(i, i+1));
		}
	}

}
