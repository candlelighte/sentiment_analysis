# sentiment_analysis

feelinganalysis_preprocessing :: allows to perform a numbers of function on string to adapt it to be analysed in other time. 
feelinganalyses_features :: allows to give polarities ( positive or negative ) to a list of words, it is using also some deep learning models to set polarityes if the native class cant give a result.
feelinanalyses_implementation :: this is the main project that use _preprocessing and _features to analyse a sentiment from a given sentence. 

This work is not finished yet, it needs more work but it gives good polarityes on some basic sentence. 



Eg. : 
    /**
		 * This is some example sentence to test with : 
		 * 
		 * Casablanca is a busy, bustling city.    :: return NegPol
		 * Paris is a very expensive place to live. :: return NegPol
		 * Istanbul is a huge city, with over thirteen million inhabitants. :: return PosPol
		 * Thailand is an inexpensive travel destinations.  :: return PosPol
		 * hoceima can be a bit touristy and crowded during summer. :: return PosPol
		 * Hoceima is very beautiful in the spring. :: return PosPol
		 * 
		 */

