package wordsembeddings.neuralimpl;

/**
 * 
 * @author root : candlelighte.cl@gmail.com 
 * 
 * 
 * **/

import java.io.File;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.springframework.stereotype.Service;

import wordsembeddings.service.TrainingModel;

/**
 * 
 * To Perform this class i used dl4j api : https://deeplearning4j.org/
 * 
 * Word2VecSetup
 * 
 * word2vec is a famous model that convert a word to a vector, so we can perform
 * on it a big variety of treatment.
 * 
 * as a basic description of this model, i can say it's an Two Layer Neural
 * model, that can change a word to vector, so we can apply mathematical theory
 * on words.
 * 
 * like Glove Model, word2vec can give nearest word to another, and give
 * similarity between them.
 * 
 * 
 * We can use this model to give the nearest word of a given one if his polarity
 * does not exist on NativeWordsEmbedding
 * 
 **/

@Service("word2VecSetup")
public class Word2VecSetup implements TrainingModel<Word2Vec, SentenceIterator> {

	protected final Logger TRACER = Logger.getLogger(getClass());

	private static final String PATH_TRAINING_FILE = "raw_sentences_file";
	private static final String PATH_WRITE_MODEL = "word2vec_dl4j.ser";
	private static final String PATH_LOAD_MODEL = "word2vec_dl4j.ser";

	private static Word2Vec word2VecSetup;

	public Word2VecSetup() {
		BasicConfigurator.configure();
		TRACER.info("Word2VecSetup has been initialzide to train a model");
	}

	public Word2Vec trainModel() {

		if (word2VecSetup == null) {

			synchronized (Word2VecSetup.class) {

				if (word2VecSetup == null) {

					TRACER.info("No Model has been found, process for trying to load model will start..... ");

					File ser_model = new File(PATH_LOAD_MODEL);

					if (ser_model.exists() && !ser_model.isDirectory()) {
						TRACER.info("Serialized model found.");
						reloadModel(PATH_LOAD_MODEL);
						return word2VecSetup;
					}

					TRACER.info(
							"No serialized model has been found, process for trying to train model will start..... ");

					TokenizerFactory tokenizerFactory = createTokenizer();
					SentenceIterator sentenceIterator = loadData(PATH_TRAINING_FILE);

					word2VecSetup = new Word2Vec.Builder().minWordFrequency(1).iterations(1).layerSize(100).seed(42)
							.windowSize(5).iterate(sentenceIterator).tokenizerFactory(tokenizerFactory).build();

					TRACER.info("Fitting Word2Vec model......");

					word2VecSetup.fit();

					saveModel(word2VecSetup, PATH_WRITE_MODEL);

				}
			}
		}

		return word2VecSetup;
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

	public void saveModel(Word2Vec word2Vec, String path) {
		if (word2Vec != null) {
			TRACER.info("Save model in : " + path);
			WordVectorSerializer.writeWord2VecModel(word2Vec, path);

		}

	}

	public void reloadModel(String path) {

		if (word2VecSetup == null) {

			synchronized (Word2VecSetup.class) {

				if (word2VecSetup == null) {
					TRACER.info("Reload model from file : " + path);
					word2VecSetup = WordVectorSerializer.readWord2VecModel(path);
				}
			}

		}
	}

	public void importModel(String path) {
		TRACER.info("Import model.....");
		File fmodel = new File(path);
		word2VecSetup = WordVectorSerializer.readWord2VecModel(fmodel);
	}

	public void evaluateModel() {
		if (word2VecSetup != null) {
			TRACER.info("Evaluating model....");
			Collection<String> list = word2VecSetup.wordsNearest("good", 10);
			System.out.println(list.toString());

		}

	}

	public void visualizingModel() {
		// TODO :: implement a function to visualize model
	}

	private static TokenizerFactory createTokenizer() {
		TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
		return tokenizerFactory;
	}

}
