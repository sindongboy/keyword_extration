package com.skplanet.nlp.keywordextractor;

import com.skplanet.nlp.config.Configuration;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Keyword Extraction based on 'Rapid Automated Keyword Extraction'
 *
 * @author Donghun Shin, SK planet, donghun.shin@sk.com
 */
public class SimpleKeywordExtraction implements KeywordExtractionImpl {

    // logger
    private static Logger logger = Logger.getLogger(SimpleKeywordExtraction.class.getName());

    // definition
    private static final String RAKE_CONFIG = "rake.properties";
    private static final String STOPWORD_FILE = "stopword.kr";
    private static final String MIN_WORD_LEN_PROP = "MIN_WORD_LEN";

    // properties
    private static int MIN_WORD_LEN = 0;

    // members
    protected List<String> stopwordList = new ArrayList<String>();
    protected Pattern stopwordPattern = null;
    protected File stopwordFile = null;


    /**
     * Initialize
     */
    public void init() throws IOException {
        // get configurations
        Configuration config = Configuration.getInstance();
        try {
            config.loadProperties(RAKE_CONFIG);
        } catch (IOException e) {
            logger.error("failed to load rake configuration : " + RAKE_CONFIG);
            e.printStackTrace();
        }

        // properties reading
        // - minimum length of a word
        String minwordlen = config.readProperty(RAKE_CONFIG, MIN_WORD_LEN_PROP);
        MIN_WORD_LEN = Integer.parseInt(minwordlen);

        // load stopwords
        URL stopwordPath = config.getResource(STOPWORD_FILE);
        this.stopwordFile = new File(stopwordPath.getFile());
        logger.info("loading stopwords ...");
        logger.info("stopword path : " + stopwordPath.getFile());
        loadStopWords(this.stopwordFile);
        logger.info("done");

        // build stopword
        logger.info("compile stopwords...");
        buildStopWordRegex();
        logger.info("done");

        logger.info("initialize done");
    }

    /**
     * Load stopword list
     *
     * @param filePath stopword file path
     */
    protected void loadStopWords(File filePath) throws IOException {

        BufferedReader reader;

        reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            this.stopwordList.add(line.trim().toLowerCase());
        }
        reader.close();
    }

    /**
     * Compile stopwords
     */
    protected void buildStopWordRegex() {

        if (this.stopwordList.size() == 0) {
            logger.warn("No Stopwords!");
        }
        final StringBuilder stopWordPatternBuilder = new StringBuilder();
        int count = 0;
        for (final String stopWord : this.stopwordList) {
            if (count++ != 0) {
                stopWordPatternBuilder.append("|");
            }
            stopWordPatternBuilder.append("\\b").append(stopWord).append("\\b");
        }
        this.stopwordPattern = Pattern.compile(stopWordPatternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Tokenizer
     *
     * Override this method is strongly recommended for all subclasses
     *
     * @param sentence sentence to be tokenized
     * @return list of tokens
     */
    public List<String> tokenize(String sentence) {
        final List<String> separateWords = new ArrayList<String>();
        //final String[] words = text.split("[^가-힣a-zA-Z0-9_\\+\\-/]");
        // white space tokenizer
        final String[] words = sentence.split(" ");

        if (words.length > 0) {
            for (final String word : words) {
                String wordLowerCase = word.trim().toLowerCase();

                if (wordLowerCase.length() > 0 &&
                        wordLowerCase.length() > MIN_WORD_LEN &&
                        !isNumber(wordLowerCase)) {
                    separateWords.add(wordLowerCase);
                }
            }
        }
        return separateWords;
    }

    /**
     * Sentence Breaker
     *
     * @param document document to be split by sentence
     * @return list of sentence
     */
    public List<String> sentence(String document) {
        final String[] sentences = document.split("[.!?,;:\\t\\\\-\\\\\"\\\\(\\\\)\\\\\\'\\u2019\\u2013]");
        return new ArrayList<String>(Arrays.asList(sentences));
    }

    /**
     * Keyword Extraction API
     *
     * @param text keyword extraction source text
     * @return keyword and score pair
     */
    public Map<String, Double> extract(String text) {
        List<String> keywordCandidatesList = new ArrayList<String>();
        Map<String, Double> keywordScore = new HashMap<String, Double>();


        // ------------------------------- //
        // Generate Keyword Candidates
        // ------------------------------- //
        List<String> sentenceList = sentence(text);

        for (String sentence : sentenceList) {
            String[] candidates = this.stopwordPattern.matcher(sentence).replaceAll("|").split("\\|");
            if (candidates.length > 0) {
                for (final String candidate : candidates) {
                    String c = candidate.trim().toLowerCase();
                    if (c.length() > 0) {
                        keywordCandidatesList.add(c);
                    }
                }
            }
        }


        // ----------------------- //
        // calculate word score
        // ----------------------- //
        Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
        Map<String, Integer> wordDegree = new HashMap<String, Integer>();

        for (String candidate : keywordCandidatesList) {

            List<String> wordList = tokenize(candidate);
            int wordListDegree = wordList.size() - 1;

            for (final String word : wordList) {

                if (!wordFrequency.containsKey(word)) {
                    wordFrequency.put(word, 0);
                }

                if (!wordDegree.containsKey(word)) {
                    wordDegree.put(word, 0);
                }

                wordFrequency.put(word, wordFrequency.get(word) + 1);
                wordDegree.put(word, wordDegree.get(word) + wordListDegree);
            }
        }

        for (String word : wordFrequency.keySet()) {
            wordDegree.put(word, wordDegree.get(word) + wordFrequency.get(word));

            if (!keywordScore.containsKey(word)) {
                keywordScore.put(word, 0.0);
            }
            keywordScore.put(word, wordDegree.get(word) / (wordFrequency.get(word) * 1.0));
        }

        // --------------------------- //
        // calculate candidates score
        // --------------------------- //
        final Map<String, Double> result = new HashMap<String, Double>();
        for (String candidate : keywordCandidatesList) {

            final List<String> wordList = tokenize(candidate);
            double score = 0;

            for (final String word : wordList) {
                score += keywordScore.get(word);
            }

            result.put(candidate, score);
        }
        return result;
    }

    /**
     * Loading Stopword Dictionary
     */
    public void loadStopword() throws IOException {

        BufferedReader reader;

        reader = new BufferedReader(new FileReader(this.stopwordFile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            this.stopwordList.add(line.trim().toLowerCase());
        }
        reader.close();
    }

    private boolean isNumber(final String str) {
        return str.matches("[0-9.]");
    }

}
