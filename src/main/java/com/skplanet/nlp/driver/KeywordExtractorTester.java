package com.skplanet.nlp.driver;

import com.skplanet.nlp.keywordextractor.NLPKeywordExtraction;
import com.skplanet.nlp.util.Utility;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Keyword Extractor
 * <br>
 * <br>
 * Created by Donghun Shin
 * <br>
 * Contact: donghun.shin@sk.com, sindongboy@gmail.com
 * <br>
 * Date: 8/13/13
 * <br>
 */
public class KeywordExtractorTester {

    private static Logger logger = Logger.getLogger(KeywordExtractorTester.class.getName());

    // main
    public static void main(String[] args) throws IOException {

        String keyword = "";
        Scanner scan = new Scanner(System.in);
        NLPKeywordExtraction keywordExtractor = new NLPKeywordExtraction();
        keywordExtractor.init();
        System.out.print("INPUT: ");
        while ((keyword = scan.nextLine()) != null) {

            if (keyword.trim().length() == 0) {
                System.out.print("INPUT: ");
                continue;
            }

            if (keyword.trim().toLowerCase().equals("reload")) {
                keywordExtractor.reloadStopword();
                System.out.print("INPUT: ");
                continue;
            }

            if (keyword.trim().toLowerCase().equals("quit")) {
                break;
            }

			System.out.println(keyword);

			Map<String, Double> result = keywordExtractor.extract(keyword);

            result = Utility.sort(result);
            Set<String> keys = result.keySet();
            for (String k : keys) {
                System.out.println(k + " : " + result.get(k));
            }
            System.out.print("\nINPUT: ");

        }
    }

}
