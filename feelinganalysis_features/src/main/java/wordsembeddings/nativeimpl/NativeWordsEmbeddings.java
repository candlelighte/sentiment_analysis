package wordsembeddings.nativeimpl;

/**
 * 
 * @author root : candlelighte.cl@gmail.com 
 * 
 * 
 * **/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import wordsembeddings.service.TrainingModel;
import wordsembeddings.tools.StringComparator;

/**
 * 
 * NativeWordsEmbeddings : This is the mean class to use to analyze polarity of
 * a given word !!
 * 
 * 
 * Use native methods to say if a given words can have positive or negative
 * polarity, those words are all saved on files, the mean idea is to load all
 * given files on two sorted tree list -negative list and positive list - , i
 * choose sorted tree list , to guarantee that no duplicated value will exist on
 * the serialized model and i will always have a sorted list, this will reduce
 * time of research. One time the files are loaded on lists, i serialize theme
 * to not have to reload theme a second time.
 * 
 * Ones of the negative side of this architecture : 1 - memory :: sorted tree
 * list are expensive and i have a lots of string to perform. 2 - CRUD ::
 * inserting new values will cost time because of sorting new entries.
 * 
 */

@Repository("nativeWordsEmbeddings")
public class NativeWordsEmbeddings implements TrainingModel<SortedSet<String>, Map<String, List<String>>> {

	private static final String FILE_TRAIN_WORDS = "training/";
	private static final String FILE_SAVE_MODELN = "sortedSetNeg.ser";
	private static final String FILE_SAVE_MODELP = "sortedSetPos.ser";

	private static final String FILE_READ_MODEL = "";

	private static final String POS_KEY = "positive";
	private static final String NEG_KEY = "negative";

	protected final Logger TRACER = Logger.getLogger(getClass());

	private static SortedSet<String> sortedSetPosDic;
	private static SortedSet<String> sortedSetNegDic;

	public NativeWordsEmbeddings() {
		BasicConfigurator.configure();
		TRACER.info("NativeWordsEmbeddings has been initialzide to train a model");
	}

	/**
	 * loadData :: can load any files under src/main/resources/training/ once the
	 * training is performed, you have to delete generated serialized models to add
	 * new files
	 **/
	public Map<String, List<String>> loadData(String file_path) {

		List<String> listPos = new ArrayList<String>();
		List<String> listNeg = new ArrayList<String>();

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		String file = this.getClass().getClassLoader().getResource(file_path).getFile();

		File dirN = new File(file + "negative");

		if (dirN.isDirectory()) {
			
			for (File files : dirN.listFiles()) {

				try {

					listNeg.addAll(loadArrayFromFile(files.getAbsolutePath()));

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
		
		map.put(NEG_KEY, listNeg);

		File dirP = new File(file + "positive");
		if (dirP.isDirectory()) {
			for (File files : dirP.listFiles()) {

				try {
					listPos.addAll(loadArrayFromFile(files.getAbsolutePath()));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		
		map.put(POS_KEY, listPos);

		return map;
	}

	/**
	 * trainModel :: will automatically load any files on this folder :
	 * 'src/main/resources/training/' , if you have a file containing
	 * positive/negative words, you can add it on .../positive or .../negative
	 * (choose the right folder ) folders situated under
	 * 'src/main/resources/training/', this function will automatically read news
	 * files. Make sure to add new files before performing the training, is not
	 * delete sortedSetNeg.ser and sortedSetPos.ser ( serialized models ) and re-run
	 * the training. PLEASE ::::::::: any files any files added here should respect
	 * those criteria : 1 - one value per line 2 - do not give attention to
	 * duplicate value
	 **/
	public SortedSet<String> trainModel() throws FileNotFoundException, IOException, ClassNotFoundException {

		if (sortedSetNegDic == null && sortedSetPosDic == null) {

			synchronized (NativeWordsEmbeddings.class) {

				if (sortedSetNegDic == null && sortedSetPosDic == null) {

					TRACER.info("No Model has been found, process for trying to load model will start..... ");

					File ser_modelP = new File(FILE_SAVE_MODELP);
					File ser_modelN = new File(FILE_SAVE_MODELN);

					if (ser_modelP.exists() && !ser_modelP.isDirectory() && ser_modelN.exists()
							&& !ser_modelN.isDirectory()) {
						TRACER.info("Serialized model found.");
						reloadModel(FILE_READ_MODEL);
						TRACER.info("Models Loaded");
						return null;
					}

					TRACER.info(
							"No serialized model has been found, process for trying to train model will start..... ");

					sortedSetNegDic = new TreeSet<String>(getComparator());
					sortedSetPosDic = new TreeSet<String>(getComparator());

					TRACER.info("Fitting NativeWordsEmbeddings model......");

					Map<String, List<String>> map = loadData(FILE_TRAIN_WORDS);

					for (String string : map.get(NEG_KEY)) {
						sortedSetNegDic.add(string);
					}

					for (String string : map.get(POS_KEY)) {
						sortedSetPosDic.add(string);
					}

					saveModel(NativeWordsEmbeddings.sortedSetNegDic, FILE_SAVE_MODELN);
					saveModel(NativeWordsEmbeddings.sortedSetPosDic, FILE_SAVE_MODELP);

				}
			}
		}

		evaluateModel();

		return null;
	}

	/**
	 * saveModel :: The main object of the idea of serializing the list, is to
	 * always have a list containing not duplicate value of negative or positive
	 * list. The second think is, i guarantee that even if we add others words's
	 * files, we will always have a main file ( list serialized ) containing
	 * performed ( not duplicate ) values.
	 */

	public void saveModel(SortedSet<String> sortedlist, String path) throws FileNotFoundException, IOException {

		if (sortedlist != null) {
			FileOutputStream fileOutputStream = new FileOutputStream(path);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(sortedlist);

			if (objectOutputStream != null)
				objectOutputStream.close();
			if (fileOutputStream != null)
				fileOutputStream.close();

		}

	}

	/**
	 * */

	@SuppressWarnings("unchecked")
	public void reloadModel(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
System.out.println("Heeeeeeeeeeeeeeeeeeeeeeeeeeere");
		if (sortedSetNegDic == null && sortedSetPosDic == null) {

			FileInputStream fileInputStream = new FileInputStream(path + "sortedSetNeg.ser");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			sortedSetNegDic = (SortedSet<String>) objectInputStream.readObject();

			if (objectInputStream != null)
				objectInputStream.close();

			if (fileInputStream != null)
				fileInputStream.close();

			FileInputStream fileInputStream2 = new FileInputStream(path + "sortedSetPos.ser");
			ObjectInputStream objectInputStream2 = new ObjectInputStream(fileInputStream2);
			sortedSetPosDic = (SortedSet<String>) objectInputStream2.readObject();

			if (objectInputStream2 != null)
				objectInputStream2.close();

			if (fileInputStream2 != null)
				fileInputStream2.close();

		}

	}

	public void importModel(String path) {
		// TODO :: This function is not implemented YET !! should contain a code to
		// import another file ( containing negative or positive value )
		// and serialize the list, because of i am working with a Sorted Tree List,
		// i am pretty sure there is no multiple value in the list

	}

	public void evaluateModel() {

		if (sortedSetNegDic != null && sortedSetPosDic != null) {

			System.out.println("work_out is negative : " + sortedSetNegDic.contains("work_out"));
			System.out.println("deflagrate is positive : " + sortedSetPosDic.contains("deflagrate"));

		}

	}

	public void visualizingModel() {
		// TODO :: This function is not implemented YET !! should contain a code to
		// visualize the model !
		// can maybe list content of the sorted lists.

	}

	public static SortedSet<String> getSortedSetPos() {
		if (sortedSetPosDic != null)
			return sortedSetPosDic;
		return null;
	}

	// public static void setSortedSetPos(SortedSet<String> sortedSetPosDic) {
	// NativeWordsEmbeddings.sortedSetPosDic = sortedSetPosDic;
	// }

	public static SortedSet<String> getSortedSetNeg() {
		if (sortedSetNegDic != null)
			return sortedSetNegDic;
		else
			return null;
	}

	// public static void setSortedSetNeg(SortedSet<String> sortedSetNegDic) {
	// NativeWordsEmbeddings.sortedSetNegDic = sortedSetNegDic;
	// }

	/**
	 * ------------------------- Internal Utilities Methods
	 * -------------------------
	 **/

	// used to load file of currency, mathematical, punctuation and typographic on
	// lists
	private static List<String> loadArrayFromFile(String filePath) throws IOException {

		BufferedReader bInputStream = new BufferedReader(new FileReader(new File(filePath)));
		List<String> arrayOfWords = new ArrayList<String>();

		String line = "";

		while ((line = bInputStream.readLine()) != null) {
			arrayOfWords.add(line);
		}

		if (bInputStream != null) {
			bInputStream.close();
		}

		return arrayOfWords;
	}

	// Initialize to work with on a given list
	private Comparator<String> getComparator() {

		return new StringComparator();
	}

}
