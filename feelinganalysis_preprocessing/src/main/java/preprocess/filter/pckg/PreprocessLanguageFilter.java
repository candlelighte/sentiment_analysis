package preprocess.filter.pckg;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import corenlpimp.pckg.NormalizeWords;
import utilities.pckg.UtilitiesActions;

@Service("languageFilter")
public class PreprocessLanguageFilter {

	private NormalizeWords normalizeWords;

	public PreprocessLanguageFilter() {

		normalizeWords = new NormalizeWords();

	}

	// performAllPreprocessingLanguageOnPost can perform all Pre-process actions on
	// a given post like facebook's ones or others.
	// please be careful, this function perform removePunctuations method
	// and this will delete all punctuation from sentence,
	// so the meaning of a given sentence can be lost, so on future practice, i will
	// not use
	// performAllPreprocessingLanguageOnPost neither her implementation here.
	// it can be useful on some context.
	public List<String> perform_AllPreprocessingLanguage_OnPost(String post) {

		try {
			post = UtilitiesActions.expendAbbreviations(post);
			post = UtilitiesActions.removeStopwords(post);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> racines = getNormalizeWords().lemmatiseText(post);

		try {

			UtilitiesActions.removePunctuations(racines);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return racines;
	}

	// perform expend abbreviation and remove stop word from post 
	public List<String> perform_remove_stpwrds_abrv_OnPost(String post) {

		try {
			post = UtilitiesActions.expendAbbreviations(post);
			post = UtilitiesActions.removeStopwords(post);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> racines = getNormalizeWords().lemmatiseText(post);

		return racines;
	}


	//
	// For test purpose
	//
	// public static void main(String[] args) {
	//
	// System.out.println(new
	// PreprocessLanguageFilter().performAllPreprocessingLanguageOnPost("I am unable
	// to load file ! i'm non adhesive ").toString());
	//
	// }

	public NormalizeWords getNormalizeWords() {
		return normalizeWords;
	}

}
