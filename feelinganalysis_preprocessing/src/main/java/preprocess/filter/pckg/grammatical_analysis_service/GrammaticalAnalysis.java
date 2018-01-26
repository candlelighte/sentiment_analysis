package preprocess.filter.pckg.grammatical_analysis_service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface GrammaticalAnalysis {

	/**
	 * build all pattern needed
	 */
	public void createPatterns() throws IOException;

	/**
	 * load pattern from file
	 */
	public Map<String, Set<String>> loadPattern() throws IOException;

	/**
	 * check if sentence match patterns
	 */
	public String matchPatterns(String sentence);

}
