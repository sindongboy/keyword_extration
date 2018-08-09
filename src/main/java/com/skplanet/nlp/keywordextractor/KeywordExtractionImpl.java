package com.skplanet.nlp.keywordextractor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <br>
 *     Interface for Keyword Extraction
 * <br>
 * Created by Donghun Shin
 * <br>
 * Contact: donghun.shin@sk.com, sindongboy@gmail.com
 * <br>
 * Date: 6/18/14
 * <br>
 */
public interface KeywordExtractionImpl {
    /**
     * Tokenizer
     * @param sentence sentence to be tokenized
     * @return list of tokens
     */
    List<String> tokenize(String sentence);

    /**
     * Sentence Breaker
     * @param document document to be split by sentence
     * @return list of sentence
     */
    List<String> sentence(String document);

    /**
     * Keyword Extraction API
     * @param text keyword extraction source text
     * @return keyword and score pair
     */
    Map<String, Double> extract(String text);

    /**
     * Loading Stopword Dictionary
     *
     */
    void loadStopword() throws IOException;

}
