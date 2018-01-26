package wordsembeddings.neuralimpl;

/**
 * 
 * @author root : candlelighte.cl@gmail.com 
 * 
 * 
 * **/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;
import org.springframework.stereotype.Service;

import wordsembeddings.service.TrainingModel;
import wordsembeddings.tools.LabelSeeker;
import wordsembeddings.tools.MeansBuilder;

/**
 * * To Perform this class i used dl4j api : https://deeplearning4j.org/
 * 
 * 
 * Paragraph2Vec
 * 
 * this model will try to categorize a text to a given, categories. as an
 * example if i have a with an undefined topic, this model will inform me if
 * this text is an economic writing, politics or medical or what ever .... For
 * that purpose i should give the model as a training text, examples of what can
 * be a medical writing, or economic one. For my purpose i use it to know if the
 * paragraph, text or sentence are positive or not...
 * 
 * As a result, this is not always good, and it's surly due to my training data,
 * i think to obtain more sufficient result, i have to precise the context of my
 * training text examples.
 * 
 * As an example, if the purpose is to declare a destination is good or not from
 * a text or a post or comment, i have to give an idea of what is a good
 * destination, and bad one.
 * 
 * 
 **/

@Service("paragraphe2Vec")
public class Paragraph2Vec implements TrainingModel<ParagraphVectors, LabelAwareIterator> {

	private static ParagraphVectors paragraphVectors;

	protected final Logger TRACER = Logger.getLogger(getClass());

	private static final String PATH_TRAINING_FILE = "paravec/labeled";
	private static final String PATH_WRITE_MODEL = "paragraph2vec_dl4j.ser";
	private static final String PATH_LOAD_MODEL = "paragraph2vec_dl4j.ser";

	public Paragraph2Vec() {
		BasicConfigurator.configure();
		TRACER.info("Paragraph2Vec has been initialzide to train a model");
	}

	public LabelAwareIterator loadData(String file_path) {

		ClassPathResource resource = new ClassPathResource(file_path);

		LabelAwareIterator iterator = null;
		try {
			iterator = new FileLabelAwareIterator.Builder().addSourceFolder(resource.getFile()).build();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return iterator;
	}

	public ParagraphVectors trainModel() {
		if (paragraphVectors == null) {

			synchronized (Paragraph2Vec.class) {

				if (paragraphVectors == null) {

					TRACER.info("No Model has been found, process for trying to load model will start..... ");

					File ser_model = new File(PATH_LOAD_MODEL);

					if (ser_model.exists() && !ser_model.isDirectory()) {
						TRACER.info("Serialized model found.");
						reloadModel(PATH_LOAD_MODEL);
						return paragraphVectors;
					}

					TRACER.info(
							"No serialized model has been found, process for trying to train model will start..... ");

					TokenizerFactory tokenizerFactory = createTokenizer();
					LabelAwareIterator labelAwareSentenceIterator = loadData(PATH_TRAINING_FILE);

					paragraphVectors = new ParagraphVectors.Builder().minWordFrequency(1).layerSize(100)
							.learningRate(0.025).minLearningRate(0.001).batchSize(1000).epochs(20).windowSize(5)
							.iterate(labelAwareSentenceIterator).trainWordVectors(true)
							.tokenizerFactory(tokenizerFactory).build();

					TRACER.info("Fitting Paragraph2Vec model......");
					paragraphVectors.fit();

					saveModel(paragraphVectors, PATH_WRITE_MODEL);

				}
			}
		}

		return paragraphVectors;
	}

	public void saveModel(ParagraphVectors modelType, String path) {
		if (paragraphVectors != null) {
			TRACER.info("Save model in : " + path);
			WordVectorSerializer.writeParagraphVectors(paragraphVectors, path);

		}
	}

	public void reloadModel(String path) {
		if (paragraphVectors == null) {

			synchronized (Word2VecSetup.class) {

				if (paragraphVectors == null) {
					TRACER.info("Reload model from file : " + path);
					try {
						paragraphVectors = WordVectorSerializer.readParagraphVectors(path);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public void importModel(String path) {
		// TODO Auto-generated method stub

	}

	public void evaluateModel() {

		ClassPathResource unClassifiedResource = new ClassPathResource("paravec/unlabeled");
		FileLabelAwareIterator unClassifiedIterator = null;
		try {
			unClassifiedIterator = new FileLabelAwareIterator.Builder().addSourceFolder(unClassifiedResource.getFile())
					.build();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		TokenizerFactory tokenizerFactory = createTokenizer();
		LabelAwareIterator iterator = loadData(PATH_TRAINING_FILE);

		MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
				tokenizerFactory);

		LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
				(InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());

		while (unClassifiedIterator.hasNextDocument()) {
			LabelledDocument document = unClassifiedIterator.nextDocument();

			INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
			List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);

			TRACER.info("Document '" + document.getLabels() + "' falls into the following categories: ");
			for (Pair<String, Double> score : scores) {
				TRACER.info("        " + score.getFirst() + ": " + score.getSecond());
			}
		}
	}

	public void visualizingModel() {
		// TODO Auto-generated method stub

	}

	private static TokenizerFactory createTokenizer() {
		TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
		return tokenizerFactory;
	}

}
