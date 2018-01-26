package wordsembeddings.tools;

/**
 * 
 * @author root : candlelighte.cl@gmail.com 
 * 
 * 
 * **/

import java.io.Serializable;
import java.util.Comparator;

/**
 * 
 * Get a Serialized string comparator to demander naturally Comparator is not
 * serializable, but white this string comparator is.
 *
 **/
public class StringComparator implements Serializable, Comparator<String> {

	private static final long serialVersionUID = 363095823468592096L;

	public int compare(String word1, String word2) {
		return word1.compareToIgnoreCase(word2);
	}

}
