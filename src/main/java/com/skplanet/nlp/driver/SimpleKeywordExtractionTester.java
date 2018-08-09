package com.skplanet.nlp.driver;

import com.skplanet.nlp.keywordextractor.SimpleKeywordExtraction;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * <br>
 *     Keyword Extraction Example
 * <br>
 * Created by Donghun Shin
 * <br>
 * Contact: donghun.shin@sk.com, sindongboy@gmail.com
 * <br>
 * Date: 6/18/14
 * <br>
 */
public class SimpleKeywordExtractionTester {
    private static final Logger logger = Logger.getLogger(SimpleKeywordExtraction.class.getName());

    public static void main(String[] args) throws IOException {
        SimpleKeywordExtraction ke = new SimpleKeywordExtraction();

        ke.init();

        Scanner scan = new Scanner(System.in);

        String line;
        System.out.print("INPUT: ");
        while ((line = scan.nextLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            if (line.trim().toLowerCase().equals("quit")) {
                break;
            }

            Map<String, Double> result = ke.extract(line);
            Set<String> key = result.keySet();

            for (String k : key) {
                System.out.println(k + " : " + result.get(k));
            }

            System.out.print("\nINPUT: ");


        }
    }

}
