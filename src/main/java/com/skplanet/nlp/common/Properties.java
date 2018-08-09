package com.skplanet.nlp.common;

import org.apache.log4j.Logger;

/**
 * <br>
 *     Common Properties
 * <br>
 * Created by Donghun Shin
 * <br>
 * Contact: donghun.shin@sk.com, sindongboy@gmail.com
 * <br>
 * Date: 6/19/14
 * <br>
 */
public final class Properties {
    private static Logger logger = Logger.getLogger(Properties.class.getName());

    // common file names
    public static final String RAKE_CONFIG = "rake.properties";
    public static final String STOPWORD_FILE = "stopword.kr";

    // properties
    public static final String MIN_WORD_LEN = "MIN_WORD_LEN";
    public static final String MIN_TOKEN_LEN = "MIN_TOKEN_LEN";
    public static final String STOPTAG = "STOPTAG";

}
