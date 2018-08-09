package com.skplanet.nlp.util;

import com.skplanet.nlp.config.Configuration;
import org.apache.log4j.Logger;

import com.skplanet.nlp.NLPAPI;
import com.skplanet.nlp.NLPDoc;
import com.skplanet.nlp.morph.Morphs;

/**
 * NLP-Indexterm based NLP Utilities (Singleton)
 */
public class OMPNLP {
	private static Logger logger = Logger.getLogger(OMPNLP.class.getName());
	private static final String configName = "nlp_api.properties";

	private static OMPNLP instance = null;

	private static NLPAPI nlpApi = null;

	/**
	 * Get Instance
	 * @return instance of NLP Class
	 */
	public static OMPNLP getInstance() {
		if (instance == null) {
			// method synchronized
			synchronized (OMPNLP.class) {
				instance = new OMPNLP();
			}
		}
		return instance;
	}

	// private constructor
	private OMPNLP() {
		logger.info("NLP initializing ....");
		nlpApi = new NLPAPI(configName, Configuration.CLASSPATH_LOAD);
		logger.info("NLP initializing done");
	}

	// ----------- Static Utilities ------------ //

	/**
	 * Get Sentences from given text, possibly composed of multiple sentences
	 *
	 * @param text Text to be splited into single sentences
	 * @return array of sentences
	 */
	public String [] getSentences(String text) {
		return nlpApi.doSegmenting(text);
	}

	/**
	 * Get Morph List from given text
	 *
	 * @param text Text to be nlp-analyzed
	 * @return array of Morphs 
	 */
	public String [] getMorphs(String text) {
		NLPDoc nlpRes = nlpApi.doNLP(text);
		Morphs morphs = nlpRes.getMorphs();
		String [] result = new String[morphs.getCount()];
		for(int i = 0; i < morphs.getCount(); i++) {
			result[i] = morphs.getMorph(i).getTextStr();
		}
		return result;
	}

	/**
	 * Get POS Tag List from given text
	 *
	 * @param text Text to be nlp-analyzed
	 * @return array of POS-Tags
	 */
	public String [] getPOSTags(String text) {
		NLPDoc nlpRes = nlpApi.doNLP(text);
		Morphs morphs = nlpRes.getMorphs();
		String [] result = new String[morphs.getCount()];
		for(int i = 0; i < morphs.getCount(); i++) {
			result[i] = morphs.getMorph(i).getPosStr();
		}
		return result;
	}

}
