package com.skplanet.nlp.keywordextractor;

import com.skplanet.nlp.common.Properties;
import com.skplanet.nlp.config.Configuration;
import com.skplanet.nlp.util.OMPNLP;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * NLP Keyword Extractor <br>
 * - Using NLP Module.<br>
 * - Must use NLPAPI for pos tagging, tokenize and sentence break<br>
 * <br>
 * <br>
 * Created by Donghun Shin
 * <br>
 * Contact: donghun.shin@sk.com, sindongboy@gmail.com
 * <br>
 * Date: 8/13/13
 * <br>
 */
public final class NLPKeywordExtraction implements KeywordExtractionImpl {

    //logger
    private static Logger logger = Logger.getLogger(NLPKeywordExtraction.class.getName());

    // OMP NLP Utility
    private OMPNLP nlp = OMPNLP.getInstance();

    // Preprocessed Documents List
    private List<String> sentences = null;

    // -------------- //
    // members
    // -------------- //
    // minimum word length of each word in candidates phrase , 0 by default
    private int MIN_WORD_LEN = 0;

    // minimum token length
    private int MIN_TOKEN_LEN = 0;

    // stopword list
    private List<String> stopwordList = new ArrayList<String>();
    // stopword pattern
    private Pattern stopwordPattern = null;
    // stopword file
    private File stopwordFile = null;
    // stoptag list
    private Set<String> stoptag = null;


    // -------------------- //
    // Methods
    // -------------------- //
    /**
     * Constructor
     */
    public NLPKeywordExtraction() throws IOException {
        // initialize members
        this.sentences = new ArrayList<String>();
        this.stoptag = new HashSet<String>();

        // -------------------- //
        // load configuration
        // -------------------- //
        Configuration config = Configuration.getInstance();
        config.loadProperties(Properties.RAKE_CONFIG);

        // stopword file
        URL stopwordUrl = config.getResource(Properties.STOPWORD_FILE);
        this.stopwordFile = new File(stopwordUrl.getFile());

        // stoptags
        String stoptags = config.readProperty(Properties.RAKE_CONFIG, Properties.STOPTAG);
        Collections.addAll(this.stoptag, stoptags.split(","));

        // Minimum Word Length in candidates phrase
        this.MIN_WORD_LEN = Integer.parseInt(config.readProperty(Properties.RAKE_CONFIG, Properties.MIN_WORD_LEN));

        // Minimum Token Length
        this.MIN_TOKEN_LEN = Integer.parseInt(config.readProperty(Properties.RAKE_CONFIG, Properties.MIN_TOKEN_LEN));
    }

    /**
     * Initialize
     */
    public void init() throws IOException {
        // load stopwords
        logger.info("loading stopwords ...");
        try {
            logger.info("stopword path : " + this.stopwordFile.getCanonicalPath());
        } catch (IOException e) {
            logger.error("file doesn't exist: " + this.stopwordFile.getName());
            System.exit(1);
        }
        loadStopword();
        logger.info("done");
    }

    /**
     * Tokenizer
     * Simple whitespace tokenizer
     * @param sentence sentence to be tokenized
     * @return list of tokens
     */
    public List<String> tokenize(String sentence) {
        String tmp = sentence.replaceAll("(\\s+)", " ");
        List<String> result = new ArrayList<String>();
        for (String word : tmp.split(" ")) {
            if (word.length() < MIN_WORD_LEN) {
                continue;
            }
            result.add(word);
        }
        return result;
    }

    /**
     * Sentence Breaker
     * - Preprocessing raw input document to nlp-processed form
     *
     * @param document document to be split by sentence
     * @return list of sentence
     */
    public List<String> sentence(String document) {
        String[] nlpSents = nlp.getSentences(document);
        for (String sent : nlpSents) {
            if (sent.trim().length() == 0) {
                continue;
            }
            StringBuilder morphs = new StringBuilder();
            String[] tags = nlp.getPOSTags(sent);
            String[] morps = nlp.getMorphs(sent);
            boolean preStopTag = false;
            for (int i = 0; i < tags.length; i++) {
                if (this.stoptag.contains(tags[i])) {
                    if (preStopTag) {
                        continue;
                    }
                    morphs.append(tags[i] + " ");
                    preStopTag = true;
                } else {
                    morphs.append(morps[i] + " ");
                    preStopTag = false;
                }
            }

            this.sentences.add(morphs.toString().trim());
        }
        // sentences are globally assigned, no need to return
        return null;
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

        sentence(text);
        for (String sentence : this.sentences) {
            logger.debug("compiled sentence : " + sentence);
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
        this.sentences.clear();

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
            if (candidate.length() < MIN_TOKEN_LEN) {
                continue;
            }

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

        // ---------------------- //
        // read stopword file
        // ---------------------- //
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

        // ---------------------- //
        // build stopword
        // ---------------------- //
        if (this.stopwordList.size() == 0) {
            // default stopword 가 필요하지는 않을까?
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
        // build stoptags
        count = 0;
        for (String tag : this.stoptag) {
            if (count++ != 0) {
                stopWordPatternBuilder.append("|");
            }
            //stopWordPatternBuilder.append(tag);
            stopWordPatternBuilder.append("\\b").append(tag).append("\\b");
        }

        // regex compile
        this.stopwordPattern = Pattern.compile(stopWordPatternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Reload Stopword
     */
    public void reloadStopword() throws IOException {
        logger.info("reloading stopword ....");
        this.stopwordList.clear();
        loadStopword();
        logger.info("reloading stopword done");
    }
}
