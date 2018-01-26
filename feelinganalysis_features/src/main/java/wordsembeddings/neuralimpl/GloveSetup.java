package wordsembeddings.neuralimpl;

/**
 * 
 * @author root : candlelighte.cl@gmail.com 
 * 
 * 
 * **/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.glove.Glove;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.springframework.stereotype.Service;

import wordsembeddings.service.TrainingModel;

/**
 * * To Perform this class i used dl4j api : https://deeplearning4j.org/
 * 
 * GloveSetup
 * 
 * Perform a training step of glove model, this is an implementation of Stanford
 * Natural Language Models that can give the nearest word to a given one.
 * 
 * for example : if i give as entry the word : day the out put will be [night,
 * week, year, game, season, during, office, until] so we can perform many
 * things with glove and one of theme is : if i try to retrieve the polarity of
 * a given word with NativeWordsEmbeddings class and the as a result i obtain
 * noting, i can try to get the polarity of the nearest word to that i want to
 * work with.
 * 
 * Attention --> results given by Glove Model are more precise that word2vec
 * 
 * For my own test, this model gives some times good result but not always, this
 * is due to my training data, in my opinion i should change it to obtain more
 * sufficient result.
 * 
 **/

@Service("gloveSetUp")
public class GloveSetup implements TrainingModel<Glove, SentenceIterator> {

	private static final String PATH_TRAINING_FILE = "raw_sentences_file";
	private static final String PATH_WRITE_MODEL = "glove_dl4j2.ser";
	private static final String PATH_LOAD_MODEL = "glove_dl4j2.ser";

	protected final Logger TRACER = Logger.getLogger(getClass());

	private static Glove glove;
	private static WordVectors wordVectors;

	public GloveSetup() {
		BasicConfigurator.configure();
		TRACER.info("GloveSetup has been initialzide to train a model");

	}

	public SentenceIterator loadData(String file_path) {
		TRACER.info("Start loading data from " + file_path);

		SentenceIterator sentenceIterator = new LineSentenceIterator(new File(file_path));

		sentenceIterator.setPreProcessor(new SentencePreProcessor() {
			private static final long serialVersionUID = 1L;

			public String preProcess(String sentence) {
				return sentence.toLowerCase();
			}
		});

		return sentenceIterator;

	}

	public Glove trainModel() {

		if (glove == null) {

			synchronized (Word2VecSetup.class) {

				if (glove == null) {

					TRACER.info("No Model has been found, process for trying to load model will start..... ");

					File ser_model = new File(PATH_LOAD_MODEL);

					if (ser_model.exists() && !ser_model.isDirectory()) {
						TRACER.info("Serialized model found.");
						reloadModel(PATH_LOAD_MODEL);
						return glove;
					}

					TRACER.info(
							"No serialized model has been found, process for trying to train model will start..... ");

					TokenizerFactory tokenizerFactory = createTokenizer();
					SentenceIterator sentenceIterator = loadData(PATH_TRAINING_FILE);

					glove = new Glove.Builder().iterate(sentenceIterator).tokenizerFactory(tokenizerFactory).alpha(0.75)
							.learningRate(0.1).epochs(25).xMax(100).batchSize(1000).shuffle(true).symmetric(true)
							.build();

					TRACER.info("Fitting Glove model......");

					glove.fit();
					glove.wordsNearestSum("good", 10);

					saveModel(glove, PATH_WRITE_MODEL);

					reloadModel(PATH_LOAD_MODEL);
				}
			}
		}
		return glove;
	}

	public void saveModel(Glove glove, String path) {
		if (glove != null) {
			TRACER.info("Save model in : " + path);
			WordVectorSerializer.writeWordVectors(glove, path);

		}

	}

	@SuppressWarnings("deprecation")
	public void reloadModel(String path) {
		if (wordVectors == null) {

			synchronized (Word2VecSetup.class) {

				if (wordVectors == null) {
					TRACER.info("Reload model from file : " + path);

					try {

						setWordVectors(WordVectorSerializer.loadTxtVectors(new File(path)));

					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public void importModel(String path) {

		reloadModel(path);

	}

	public void evaluateModel() {
		if (wordVectors != null) {
			TRACER.info("Evaluating model....");
			System.out.println(" ----------------- similiaraty :: " + wordVectors.similarity("bad", "cancer"));
			Collection<String> list = wordVectors.wordsNearest("cancer", 10);
			System.out.println("----------------" + list.toString());
		}
	}

	public void visualizingModel() {
		// TODO :: This function is not implemented YET !! should contain a code to
		// visualize the model !
	}

	public static WordVectors getWordVectors() {
		if (wordVectors != null)
			return wordVectors;
		return wordVectors;
	}

	public static void setWordVectors(WordVectors wordVectors) {
		GloveSetup.wordVectors = wordVectors;
	}

	/**
	 * ------------------------- Internal Utilities Methods
	 * -------------------------
	 **/

	// used to load file of currency, mathematical, punctuation and typographic on
	// lists

	private static TokenizerFactory createTokenizer() {
		TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
		return tokenizerFactory;
	}

	protected Object readResolve() {
		return trainModel();
	}

}
