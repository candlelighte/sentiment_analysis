package utilities.pckg;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVReader;

public class UtilitiesActions {

	//private static final String PATH_TO_RESOURCE = "src/main/resources/";
	private static final String FILE_CURRENCY_SYMB = "currency-symbol";
	private static final String FILE_MATH_SYMB = "mathematical-symbol";
	private static final String FILE_PUNCTUATION = "punctuations";
	private static final String FILE_TYPO_SYMB = "typographic-symbol";
	private static final String FILE_ABBREV = "listofInternetAcronymsAbbreviation";
	private static final String FILE_STOPWORDS_ENG = "stopwords-eng";

	private static ClassLoader classLoader = UtilitiesActions.class.getClassLoader();

	// remove all punctuation from a text
	public static String removePunctuations(String text) throws IOException {

		return removeSymbol(text, getClassLoader().getResource(FILE_PUNCTUATION).getFile());

	}

	// remove all punctuation from a list
	public static void removePunctuations(List<String> text) throws IOException {

		removeSymbol(text, getClassLoader().getResource(FILE_PUNCTUATION).getFile());
	}

	// remove all math symbols from text
	public static String removeMathSymbol(String text) throws IOException {

		return removeSymbol(text, getClassLoader().getResource(FILE_MATH_SYMB).getFile());

	}

	// remove all math symbols from a list
	public static void removeMathSymbol(List<String> text) throws IOException {

		removeSymbol(text, getClassLoader().getResource(FILE_MATH_SYMB).getFile());

	}

	// remove typographic symbols from a text
	public static String removeTypoSymbol(String text) throws IOException {

		return removeSymbol(text, getClassLoader().getResource(FILE_TYPO_SYMB).getFile());

	}

	// remove typographic symbols from a list
	public static void removeTypoSymbol(List<String> text) throws IOException {

		removeSymbol(text, getClassLoader().getResource(FILE_TYPO_SYMB).getFile());

	}

	// remove currency from text
	public static String removeCurrencySymbol(String text) throws IOException {

		return removeSymbol(text, getClassLoader().getResource(FILE_CURRENCY_SYMB).getFile());

	}

	// remove currency from a list of String
	public static void removeCurrencySymbol(List<String> text) throws IOException {

		removeSymbol(text, getClassLoader().getResource(FILE_CURRENCY_SYMB).getFile());

	}

	// replace net abbreviation on text
	public static String expendAbbreviations(String text) throws IOException {

		Reader reader = Files.newBufferedReader(Paths.get(getClassLoader().getResource(FILE_ABBREV).getFile()));

		@SuppressWarnings("resource")
		CSVReader csvReader = new CSVReader(reader);
		List<String[]> csvContent = csvReader.readAll();

		StringTokenizer tokenizer = new StringTokenizer(text, " ");

		while (tokenizer.hasMoreTokens()) {

			String token = tokenizer.nextToken();
			token = removePunctuations(token);

			for (int i = 0; i < csvContent.size(); i++) {

				if (token.equalsIgnoreCase(csvContent.get(i)[0])) {
					text = text.replace(token, csvContent.get(i)[1].toLowerCase());
				}
			}

		}

		return text;

	}

	// replace net abbreviation on list
	public static void expendAbbreviations(List<String> text) throws IOException {

		Reader reader = Files.newBufferedReader(Paths.get(getClassLoader().getResource(FILE_ABBREV).getFile()));

		@SuppressWarnings("resource")
		CSVReader csvReader = new CSVReader(reader);
		List<String[]> csvContent = csvReader.readAll();

		for (String[] abbreviation : csvContent) {
			for (int i = 0; i < text.size(); i++) {

				if (text.get(i).equalsIgnoreCase(abbreviation[0])) {
					text.set(i, abbreviation[1]);
				}

			}
		}
	}

	// remove all stop words from a text
	public static String removeStopwords(String text) throws IOException {

		List<String> stopwordslist = loadArraySymbole(getClassLoader().getResource(FILE_STOPWORDS_ENG).getFile());

		StringTokenizer tokenizer = new StringTokenizer(text, " ");

		while (tokenizer.hasMoreTokens()) {

			String token = tokenizer.nextToken();
			for (int i = 0; i < stopwordslist.size(); i++) {

				if (token.equalsIgnoreCase(stopwordslist.get(i))) {
					text = text.replaceAll("(\\b" + token + "\\b)", "");
				}
			}

		}

		return text;
	}

	// remove all stop words from a list
	public static void removeStopwords(List<String> text) throws IOException {

		List<String> stopwordslist = loadArraySymbole(getClassLoader().getResource(FILE_STOPWORDS_ENG).getFile());

		for (String token : text) {

			for (int i = 0; i < stopwordslist.size(); i++) {

				if (token.equalsIgnoreCase(stopwordslist.get(i))) {
					text.remove(i);
				}
			}

		}

	}

	/**
	 * ------------------------- Internal Utilities Methods
	 * -------------------------
	 **/

	// used to load file of currency, mathematical, punctuation and typographic on
	// lists
	private static List<String> loadArraySymbole(String filePath) throws IOException {

		@SuppressWarnings("resource")
		Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);
		// this line is not compiling if my jdk is not exactly JavaSE-1.8
		List<String> arrayOfSymbol = stream.collect(Collectors.toList());

		return arrayOfSymbol;
	}

	// test if a word exist on parameters and remove it from text

	/**
	 * this function has for purpose to gain some code, and standardize the action
	 * of removing word from text or list of words, but due to incompatibilities of
	 * this methods with all calling methods thats have her owns skills to remove a
	 * word from a support, development of isWordExistAndRemove shows some
	 * difficulties, so choose to develop this methods later.
	 **/

	@SuppressWarnings("unused")
	private static String isWordExistAndRemove(String text, String filePath) {

		// TODO : adapt this method to remove a word from a support like text string or
		// list of string

		return null;
	}

	// remove a symbol from a given list of string
	private static void removeSymbol(List<String> text, String filePath) throws IOException {

		List<String> symbol = loadArraySymbole(filePath);

		text.removeAll(symbol);

	}

	// remove a symbol from a given string
	private static String removeSymbol(String text, String filePath) throws IOException {

		List<String> symbols = loadArraySymbole(filePath);

		for (String symbol : symbols) {

			if (text.contains(symbol)) {
				text = text.replace(symbol, "");
			}

		}
		return text;
	}

	private static ClassLoader getClassLoader() {
		return classLoader;
	}

}
