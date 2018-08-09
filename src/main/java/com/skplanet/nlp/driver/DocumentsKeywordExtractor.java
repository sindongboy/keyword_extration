package com.skplanet.nlp.driver;

import com.skplanet.nlp.cli.CommandLineInterface;
import com.skplanet.nlp.keywordextractor.NLPKeywordExtraction;
import com.skplanet.nlp.util.Utility;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * @author Donghun Shin / donghun.shin@sk.com
 * @since 8/12/16
 */
public class DocumentsKeywordExtractor {
    private static final Logger LOGGER = Logger.getLogger(DocumentsKeywordExtractor.class.getName());

    public static void main(String[] args) throws IOException {
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("d", null, true, "document path", true);
        cli.addOption("o", null, true, "output path", true);
        cli.parseOptions(args);

        File documentPath = new File(cli.getOption("d"));

        if (documentPath.isFile()) {
            LOGGER.error("require document path, not a file");
            System.exit(1);
        }

        File[] documents = documentPath.listFiles();
        BufferedReader reader;
        BufferedWriter writer;

        NLPKeywordExtraction extractor = new NLPKeywordExtraction();
        extractor.init();

        for (File documentFile : documents) {

            char[] cbuf = new char[(int) documentFile.length()];
            reader = new BufferedReader(new FileReader(documentFile));
            while (!reader.ready()) { }
            reader.read(cbuf);
            reader.close();

            String sbuf = String.valueOf(cbuf).trim();

            Map<String, Double> result = extractor.extract(sbuf);

            result = Utility.sort(result);

            writer = new BufferedWriter(new FileWriter(cli.getOption("o") + "/" + documentFile.getName()));
            for (String key : result.keySet()) {
                writer.write(key);
                writer.newLine();
            }
            writer.close();
            reader.close();
        }
    }
}
