package wordsembeddings.service;

/**
 * 
 * @author root : candlelighte.cl@gmail.com 
 * 
 * 
 * **/

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * All Performed Model should implement this interface
 * 
 * 
 */

public interface TrainingModel<ModelType, DataType> {

	/***
	 * NOTICE :: Very Important
	 * 
	 * It ' s important to know that all those prototypes
	 * 
	 * was created, performed and formalized to be implemented on
	 * 
	 * singleton patter architecture.
	 * 
	 * 
	 ***/

	/**
	 * loadData :: String file : Data type load any file containing useful data to
	 * train a model.
	 */
	DataType loadData(String file_path);

	/**
	 * trainModel :: : ModelType train a model and return it, trainModel can also
	 * try to load automatically a model from a file if the model is not
	 * initialized.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public ModelType trainModel() throws FileNotFoundException, IOException, ClassNotFoundException;

	/**
	 * saveModel :: ModelType modeType, String path : void serialize a model
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void saveModel(ModelType modelType, String path) throws FileNotFoundException, IOException;

	/**
	 * reloadModel :: String path, ModelType : void try to reload a serialized model
	 * from a file, we can reload a model if there is no instance of it in java
	 * virtual machine.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 **/
	public void reloadModel(String path) throws FileNotFoundException, IOException, ClassNotFoundException;

	/**
	 * importModel :: String path : void useful if the model was trained with Glove
	 * or others tools.
	 */
	public void importModel(String path);

	/**
	 * evaluateModel :: : void evaluate current trained model.
	 */
	public void evaluateModel();

	/**
	 * visualizingModel :: : void print information about current trained model
	 **/
	public void visualizingModel();

}
