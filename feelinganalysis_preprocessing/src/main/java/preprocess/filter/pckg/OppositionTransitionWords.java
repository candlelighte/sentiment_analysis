package preprocess.filter.pckg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Repository;

import preprocess.filter.pckg.grammatical_analysis_service.GrammaticalAnalysis;

/**
 * 
 * OppositionTransitionWords
 * 
 * this class is specialized to detect any transition words of opposition, why ?
 * 
 * I think this class is very important, to detected either a sentence express a
 * positive polarity or negative one.
 * 
 * Transition words of opposition is used to express two contrast, two opinion
 * that gives opposite polarity in the same time.
 * 
 * So, in this case, it would be difficult to say if a sentence is positive or
 * not just with counting number of positive polarity words, it has.
 * 
 * 
 * 
 */

@Repository("oppositionTransitionWords")
public class OppositionTransitionWords implements GrammaticalAnalysis {

	private static Map<String, Set<String>> listofpatterns;
	private static Set<String> listofwords;

	private static final String POS_KEY = "POS_PATTERN";
	private static final String NEG_KEY = "NEG_PATTERN";

	private static final String FILE_OPPOSITION_WORD = "OppositionTransitionWords";

	// this file contain RegEx patterns that return a positive polarity
	private static final String PATTERNS_FILE_POS = "patterns_regex/positive";

	// this file contain RegEx patterns that return a negative polarity
	private static final String PATTERNS_FILE_NEG = "patterns_regex/negative";

	
	
	
	/**
	 * build patterns 
	 * */
	public OppositionTransitionWords() throws IOException {

		createPatterns();

	}
	
	

	/**
	 * build dictionary
	 */
	@Override
	public void createPatterns() throws IOException {

		listofwords = loadresources(FILE_OPPOSITION_WORD);
		listofpatterns = loadPattern();

	}

	/**
	 * load all patterns in a Map
	 */
	@Override
	public Map<String, Set<String>> loadPattern() throws IOException {

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		map.put(NEG_KEY, loadresources(PATTERNS_FILE_NEG));

		map.put(POS_KEY, loadresources(PATTERNS_FILE_POS));

		return map;
	}

	/**
	 * test if the sentence match any pattern in the dictionary
	 */
	@Override
	public String matchPatterns(String sentence) {

		String pre_compiled_pattern;
		Pattern pattern = null;
		Matcher matcher = null;
		boolean found = false;

		for (String word : listofwords) {

			for (String regex : listofpatterns.get(NEG_KEY)) {
				pre_compiled_pattern = String.format(regex, word);
				pattern = Pattern.compile(pre_compiled_pattern);
				matcher = pattern.matcher(sentence);
				if (matcher.find()) {
					found = true;
					return NEG_KEY;
				}
			}

			if (found == false) {
				for (String regex : listofpatterns.get(POS_KEY)) {
					pre_compiled_pattern = String.format(regex, word);
					pattern = Pattern.compile(pre_compiled_pattern);
					matcher = pattern.matcher(sentence);
					if (matcher.find()) {
						System.out.println(pattern);
						found = true;
						return POS_KEY;
					}
				}
			}

		}

		return "NOT_FOUND";
	}

	public static Set<String> getListofwords() {
		return listofwords;
	}
	
	public static Map<String, Set<String>> getListofpatterns() {
		return listofpatterns;
	}
	
	/**
	 * ------------------------- Internal Utilities Methods
	 * -------------------------
	 **/

	// load resources from a given file
	private Set<String> loadresources(String path) throws IOException {

		String file = getClass().getClassLoader().getResource(path).getFile();

		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		Set<String> setlist = new HashSet<String>();
		String line = "";

		while ((line = bufferedReader.readLine()) != null) {
			setlist.add(line);
		}

		if (bufferedReader != null)
			bufferedReader.close();

		if (fileReader != null)
			fileReader.close();

		return setlist;
	}
	
	
	/**
	 * ------------------------- External Utilities Methods
	 * -------------------------
	 **/
	
	public static boolean checkIfWordExist( String search ) {
		
		for (String word : listofwords) {
			if ( search.equalsIgnoreCase( word )) return true;
		}
		return false;
	}

	// -------------------------------- For Test Purpose
	// ---------------------------------//
	// public static void main(String[] args) throws IOException {
	// OppositionTransitionWords op = new OppositionTransitionWords();
	// System.out.println(op.matchPatterns("PosPol PosPol But NegPol NegPol "));
	// }

}
