import java.io.FileNotFoundException;
import java.io.IOException;

import wordsembeddings.nativeimpl.NativeWordsEmbeddings;

public class Application {

	public static void main(String[] args) {

		/*
		 * Word2VecSetup word2VecSetup = new Word2VecSetup();
		 * word2VecSetup.trainModel(); word2VecSetup.evaluateModel();
		 * 
		 * 
		 * GloveSetup gloveSetup = new GloveSetup(); gloveSetup.trainModel();
		 * gloveSetup.evaluateModel();
		 */

		/*
		 * Paragraph2Vec paragraph2Vec = new Paragraph2Vec();
		 * paragraph2Vec.trainModel(); paragraph2Vec.evaluateModel();
		 */

		NativeWordsEmbeddings nativeWordsEmbeddings = new NativeWordsEmbeddings();
		
		try {
			nativeWordsEmbeddings.trainModel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		nativeWordsEmbeddings.evaluateModel();

	}

}
