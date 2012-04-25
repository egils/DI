package utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import exceptions.WFFException;

/**
 * File reader object is designed to read from file the production system for 
 * forward chaining and keep the lists of conditions, results and the 
 * destination itself.
 * 
 * @author Egidijus Lukauskas
 */
public class FReader {
	
	// Default values.
	private String filename = "produkcijos.txt";
	private boolean correct = true;
	private String error_message = "";
	private boolean debug = false;
	
	// Production system's data read from file
	private ArrayList<Rule> setOfRules = new ArrayList<Rule>();
	private ArrayList<String> setOfFacts = new ArrayList<String>();
	private String destination;
	
	/*
	 * Constructors
	 */
	public FReader(String filename) throws WFFException {
		if(!filename.equals("")) {
			this.setFilename(filename);
		}
		this.readFileContents();
		if(!correct) {
			throw new WFFException(this.error_message);
		}
	}
	
	public FReader(String filename, boolean debug) throws WFFException {
		if(!filename.equals("")) {
			this.setFilename(filename);
		}
		this.debug = debug;
		this.readFileContents();
		if(!correct) {
			throw new WFFException(this.error_message);
		}
	}
	
	/*
	 * Private methods.
	 */
	
	/**
	 * Prepares file to be read, test if author is correct and reads the data
	 * from file.
	 */
	private void readFileContents() {
		// Preparing file to read.
		DataInputStream fileStream;
        try {
			fileStream = new DataInputStream(
									new FileInputStream(this.filename));
		} catch (FileNotFoundException e) {
			fileStream = null;
			this.error_message = "Failas, pavadinimu „" +
									this.filename + "“, nerastas.";
			this.correct = false;
		}
        // File exists and is ready to be read. Starting reading.
        if(fileStream != null) {
        	BufferedReader file = new BufferedReader(
        							new InputStreamReader(fileStream));
        	try {
        		// Test if file's author is correct.
	        	if((file.readLine()).trim().equals("Egidijus Lukauskas")) {
	        		// Read production system's data from file.
	        		this.readSetOfRules(file);
	        		this.readSetOfFacts(file);
	        		this.readDestination(file);
	        	} else {
	        		// Incorrect file author.
	        		this.error_message = "Failas ne to autoriaus.";
	        		this.correct = false;
	        	}
        	} catch(Exception e) {}
        }
	}
	
	/**
	 * Tests if file structure for set of rules is correct. If so, reads the
	 * data from file.
	 * @param file			BufferedReader object where to read from
	 * @throws IOException	Input-Output Exception.
	 */
	private void readSetOfRules(BufferedReader file) throws IOException {
		int counter = 0; // Rule number counter.
		String line;
		String rule_expression;
		// Test if structure is correct.
		if((line = file.readLine()).trim().equals("1. Taisyklių aibė")) {
			while (!(line = file.readLine()).trim().equals("")) {
				// Remove unnecessary symbols from file.
				rule_expression = line.replaceAll(" ", "").trim();
				rule_expression = rule_expression.replaceAll("\t", "");
				if(rule_expression.contains("#")) {
					// Split before comments.
					rule_expression = rule_expression.split("#")[0];
				}
				// Add newly read rule to set.
				this.setOfRules.add(new Rule("R"+(++counter), rule_expression, this.debug));
			}
		} else {
			// File structure incorrect.
			this.error_message = "Taisykliu aibė nėra nurodytai taisyklingai.";
    		this.correct = false;
		}
	}
	
	/**
	 * Tests if file structure for set of facts is correct. If so, reads the
	 * data from file.
	 * @param file			BufferedReader object where to read from
	 * @throws IOException	Input-Output Exception.
	 */
	private void readSetOfFacts(BufferedReader file) throws IOException {
		String line;
		String facts_line;
		// Test if structure is correct.
		if((line = file.readLine()).trim().equals("2. Faktai")) {
			while (!((line = file.readLine()).trim().equals(""))) {
				// Remove unnecessary symbols from file.
				facts_line = line.replaceAll(" ", "").trim();
				facts_line = facts_line.replaceAll("\t", "").trim();
				// Take every fact one by one.
				for (int i=0;i<facts_line.length();i++) {
					// Add the new fact to set of facts.
					this.setOfFacts.add(facts_line.substring(i, i+1));
					if(debug) {
						System.out.println("Pridėtas naujas faktas: "+
								facts_line.substring(i, i+1));
					}
				}
			}
		} else {
			// File structure incorrect.
			this.error_message = "Faktų aibė nėra nurodytai taisyklingai.";
    		this.correct = false;
		}
	}
	
	/**
	 * Tests if file structure for destination is correct. If so, reads the
	 * data from file.
	 * @param file			BufferedReader object where to read from
	 * @throws IOException	Input-Output Exception.
	 */
	private void readDestination(BufferedReader file) throws IOException {
		String line;
		String dest;
		// Test if structure is correct.
		if((line = file.readLine()).trim().equals("3. Tikslas")) {
			// Test if there is a destination.
			if((line = file.readLine()).length() > 0) {
				// Get the destination symbol.
				dest = line.substring(0,1);
				this.destination = dest;
				if(debug) {
					System.out.println("Nustatytas tikslas: "+this.destination);
				}
			} else {
				// There is no destination. Wrong structure.
				this.error_message = "Tikslas nėra nurodytai taisyklingai.";
	    		this.correct = false;
			}
		} else {
			// File structure incorrect
			this.error_message = "Tikslas nėra nurodytai taisyklingai.";
    		this.correct = false;
		}
	}

	/*
	 * Setters and getters.
	 */
	public ArrayList<Rule> getSetOfRules() {
		return this.setOfRules;
	}
	
	public ArrayList<String> getSetOfFacts() {
		return this.setOfFacts;
	}
	
	public String getDestination() {
		return this.destination;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
		this.readFileContents();
	}
	
}
