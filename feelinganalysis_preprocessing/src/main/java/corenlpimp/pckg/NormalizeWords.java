package corenlpimp.pckg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

public class NormalizeWords {

	private static StanfordCoreNLP coreNLP;

	public NormalizeWords() {

		/**
		 * Building up Core NLP pipeline
		 */

		this.setCoreNLP(buildUpPipeline());

	}

	// can be used to annotated a simple String passed on parameter 
	public Map<String, String> annotateSentence(String sentence) {

		Annotation document = new Annotation(sentence);
		getCoreNLP().annotate(document);
		
		return interpretOutput(document);

	}
	
	// can be used to annotated a simple String passed on parameter 
	public Map<String, String> annotateSentence( List<String> list ) {

		String sentence = "";
		
		for (String word : list) {
			sentence += word + " ";
		}
		
		Annotation document = new Annotation(sentence);
		getCoreNLP().annotate(document);
		
		return interpretOutput(document);

	}

	// can be used to annotated a text from file 
	public Map<String, String> annotateTextFromFile(String FilePath) throws IOException {

		String text = loadFileOnString(FilePath);

		Annotation document = new Annotation(text);

		getCoreNLP().annotate(document);

		return interpretOutput(document);

	}

	// return a list containing normalized word ( root word ) of the text passed on parameter 
	public List<String> lemmatiseText(String text) {

		List<String> racinesWords = new LinkedList<String>();

		Annotation document = new Annotation(text);

		getCoreNLP().annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
								
				racinesWords.add(token.get(LemmaAnnotation.class));

			}

		}

		return racinesWords;
	}

	// return a list containing normalized word ( roots of words ) of the text in file  
	public List<String> lemmatiseTextFromFile( String Path ) throws IOException {

		List<String> racinesWords = new LinkedList<String>();

		String text = loadFileOnString( Path );

		Annotation document = new Annotation(text);

		getCoreNLP().annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

				racinesWords.add(token.get(LemmaAnnotation.class));

			}

		}

		return racinesWords;
	}
	
	/**
	 * ------------------------- Internal Utilities Methods
	 * -------------------------
	 **/

	// load and create a StandfordCoreNlp object with adequate parameters
	private StanfordCoreNLP buildUpPipeline() {
		return new StanfordCoreNLP(PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,lemma,parse,natlog",
				"ssplit.isOneSentence", "true", "parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz",
				"tokenize.language", "en"));
	}

	// used to load all file one a String character
	@SuppressWarnings("resource")
	private String loadFileOnString(String filePath) throws IOException {

		BufferedReader bufferedReader;

		bufferedReader = new BufferedReader(new FileReader(new File(filePath)));

		String line = "";
		String text = "";

		while ((line = bufferedReader.readLine()) != null) {
			text += line;
		}

		return text;
	}

	// used to interpret to output annotation of an annotated sentence or text
	private Map<String, String> interpretOutput(Annotation document) {

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (CoreMap sentence : sentences) {

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

				String word = token.get(TextAnnotation.class);

				String pos = token.get(PartOfSpeechAnnotation.class);
				
				// not used so .... 
				//String ne = token.get(NamedEntityTagAnnotation.class);
				
				map.put(word, pos);
			}

		}
		
		return map;

	}

	/**
	 * ------------------------- Getters and Setters of Class Attributes
	 * -------------------------
	 **/
	public StanfordCoreNLP getCoreNLP() {
		return NormalizeWords.coreNLP;
	}

	public void setCoreNLP(StanfordCoreNLP coreNLP) {
		NormalizeWords.coreNLP = coreNLP;
	}

}
