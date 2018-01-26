package sentimentanalyses;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;


import configuration.AppConfiguration;
import preprocess.filter.pckg.OppositionTransitionWords;
import preprocess.filter.pckg.PreprocessLanguageFilter;
import preprocess.filter.pckg.grammatical_analysis_service.GrammaticalAnalysis;
import wordsembeddings.nativeimpl.NativeWordsEmbeddings;
import wordsembeddings.neuralimpl.GloveSetup;
import wordsembeddings.service.TrainingModel;

@Service("sentimentAnalysis")
public class SentimentAnalysis {

	private static ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
			AppConfiguration.class);

	protected final Logger TRACER = Logger.getLogger(getClass());

	@Autowired
	private static TrainingModel<?, ?> nativeWordsEmbeddings;

	@Autowired
	private static TrainingModel<?, ?> gloveSetUp;
	
//  Not used in this example 
//	@Autowired
//	private static TrainingModel<?, ?> paragraphe2Vec;

	@Autowired
	private static PreprocessLanguageFilter languageFilter;

	@Autowired
	private GrammaticalAnalysis oppositionTransitionWords;

	private static final String NEG_POL = "NegPol";
	private static final String POS_POL = "PosPol";
	private static final String No_POL = "NOT_FOUND";

	private String text;

	private Map<String, String> map_Textnormalized;

	private Map<String, String> map_Textpolarity;

	/** Initialize all variable **/
	public SentimentAnalysis(String text) {

		BasicConfigurator.configure();
		TRACER.info("SentimentAnalysis has been initialzed.");

		this.text = text;

		languageFilter = applicationContext.getBean("languageFilter", PreprocessLanguageFilter.class);

		nativeWordsEmbeddings = applicationContext.getBean("nativeWordsEmbeddings", NativeWordsEmbeddings.class);

		oppositionTransitionWords = applicationContext.getBean("oppositionTransitionWords",
				OppositionTransitionWords.class);
		
		gloveSetUp = applicationContext.getBean("gloveSetUp", GloveSetup.class);


	}
	
	/** Normalize a list of word  **/
	public void normalize() {

		List<String> text_normalized = languageFilter.perform_remove_stpwrds_abrv_OnPost(getText());

		map_Textnormalized = languageFilter.getNormalizeWords().annotateSentence(text_normalized);

	}

	/** give to each word of the map a polarity if it exist  **/
	public String polaryse() throws FileNotFoundException, ClassNotFoundException, IOException {

		normalize();

		nativeWordsEmbeddings.trainModel();

		map_Textpolarity = gen_polarities(map_Textnormalized);
		
		String pattern = getPatternFromMap(map_Textpolarity);
		
		String result = oppositionTransitionWords.matchPatterns( pattern ) ;
			
		if( !result.equals( "NOT_FOUND" )) {
			
			return result;
			
		}else {
				
			return getPolarityFromMap(map_Textpolarity);
		}
				
	}
	
	
	
	

	/**
	 * ------------------------- Internal Utilities Methods
	 * -------------------------
	 **/

	// this function grant adequate polarity to word in list 
	private Map<String, String> gen_polarities(Map<String, String> list)
			throws FileNotFoundException, ClassNotFoundException, IOException {

		Map<String, String> map = new LinkedHashMap<String, String>();

		for (String word : list.keySet()) {

			if (OppositionTransitionWords.checkIfWordExist(word)) {

				map.put(word, word);

			} else {
				if (NativeWordsEmbeddings.getSortedSetNeg().contains(word)) {
					map.put(word, NEG_POL);
				} else if (NativeWordsEmbeddings.getSortedSetPos().contains(word)) {
					map.put(word, POS_POL);
				} else {

					map.put(word, useGloveModel(word));

				}
			}

		}

		return map;
	}

	
	// this function is used when the polarity of an adjective is not found 
	private static String useGloveModel(String word) throws FileNotFoundException, ClassNotFoundException, IOException {

		gloveSetUp.trainModel();
		Collection<String> list = GloveSetup.getWordVectors().wordsNearest(word, 8);

		int pospol = 0, negpol = 0;

		for (String element : list) {

			if (languageFilter.getNormalizeWords().annotateSentence(element).containsKey("JJ")) {

				if (NativeWordsEmbeddings.getSortedSetNeg().contains(element)) {
					negpol++;
				} else if (NativeWordsEmbeddings.getSortedSetPos().contains(element)) {
					pospol++;
				}

			}

		}

		if (negpol == pospol) {
			return No_POL;
		}

		return pospol > negpol ? POS_POL : NEG_POL;
	}
	
	// get a sentence from objects map 
	private String getPatternFromMap( Map<String, String> map_pol ) {
		String pattern = ""; 
		String content = "";
		for( String key_polarity : map_pol.keySet() ) {
			content = map_pol.get(key_polarity);
			if( !content.equals(No_POL)) {
				pattern += content + " ";
			}
		}
		
		return pattern;
	}
	
	// calculate polarities  
	private String getPolarityFromMap( Map<String, String> map_pol ) {
		int pos_pol = 0, neg_pol = 0;
		String content = "";
		for( String key_polarity : map_pol.keySet() ) {
			content = map_pol.get(key_polarity);
			if( content.equals( POS_POL )) {
				pos_pol++;
			}
			if( content.equals( NEG_POL )) {
				neg_pol++;
			}
		}
		
		if( pos_pol == neg_pol) return "ZERO";
		
		return (pos_pol>neg_pol)?POS_POL:NEG_POL;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * ------------------------- For Test Purpose -------------------------
	 **/

	public static void main(String[] args) {
		/**
		 * This is some example sentence to test with : 
		 * 
		 * Casablanca is a busy, bustling city. 
		 * Paris is a very expensive place to live.
		 * Istanbul is a huge city, with over thirteen million inhabitants. 
		 * Thailand is an inexpensive travel destinations. 
		 * hoceima can be a bit touristy and crowded during summer. 
		 * Hoceima is very beautiful in the spring.
		 * 
		 */

		try {
		System.out.println(
				new SentimentAnalysis("Hoceima is very beautiful in the spring")
				.polaryse()
				
				);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
