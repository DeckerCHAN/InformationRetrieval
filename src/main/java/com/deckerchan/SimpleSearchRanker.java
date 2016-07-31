/**
 * Exhibits standard Lucene searches for ranking documents.
 *
 * @author Scott Sanner, Paul Thomas
 */

package com.deckerchan;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleSearchRanker {

    String _indexPath;
    StandardQueryParser _parser;
    IndexReader _reader;
    IndexSearcher _searcher;


    public SimpleSearchRanker(String index_path, String default_field, Analyzer a)
            throws IOException {
        _indexPath = index_path;
        Directory d = new SimpleFSDirectory(Paths.get(_indexPath));
        DirectoryReader dr = DirectoryReader.open(d);
        _searcher = new IndexSearcher(dr);
        _parser = new StandardQueryParser(a);
    }

    public static void main(String[] args) throws Exception {

        String index_path = Configuration.INDEX_PATH;
        String default_field = "CONTENT";

        FileIndexBuilder builder = new FileIndexBuilder(index_path);
        SimpleSearchRanker ranker = new SimpleSearchRanker(builder._indexPath, default_field, builder._analyzer);

        // See the following for query parser syntax
        //   https://lucene.apache.org/core/5_2_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description
        //
        // IN SHORT: the default scoring function for OR terms is a variant of TF-IDF
        //           where one can individually boost the importance of query terms with
        //           a multipler using ^

        PrintStream fileOut = new PrintStream(new FileOutputStream("out.txt", false));

        try (BufferedReader br = new BufferedReader(new FileReader(Configuration.TOPIC_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String content = null;
                Matcher contentMatcher = Pattern.compile("(?<=\\d\\s).*").matcher(line);
                if (contentMatcher.find()) {
                    content = contentMatcher.group(0);
                } else {
                    System.exit(0);
                }

                String index = null;
                Matcher indexMatcher = Pattern.compile("\\d+(?=\\s)").matcher(line);
                if (indexMatcher.find()) {
                    index = indexMatcher.group(0);
                } else {
                    System.exit(0);
                }

                ranker.doSearch(index, content, fileOut);

            }
        }

//        // Standard single term
//        ranker.doSearch("Obama", 5, fileOut);
//
//        // Multiple term (implicit OR)
//        ranker.doSearch("Obama Hillary", 5, fileOut);
//
//        // Wild card
//        ranker.doSearch("Ob*ma", 5, fileOut);
//
//        // Edit distance
//        ranker.doSearch("Obama~.4", 5, fileOut);
//
//        // Fielded search (FIELD:...), boolean (AND OR NOT)
//        ranker.doSearch("FIRST_LINE:Obama AND Hillary", 5, System.out);
//        ranker.doSearch("FIRST_LINE:Obama AND NOT Hillary", 5, System.out);
//
//        // Phrase search (slop factor ~k allows words to be within k distance)
//        ranker.doSearch("\"Barack Obama\"", 5, System.out);
//        ranker.doSearch("\"Barack Obama\"~5", 5, System.out);
//
//        // Note: can boost terms or subqueries using ^ (e.g., ^10 or ^.1) -- default is 1
//        ranker.doSearch("Obama^10 Hillary^0.1", 5, System.out);
//        ranker.doSearch("(FIRST_LINE:\"Barack Obama\")^10 OR Hillary^0.1", 5, System.out);
//
//        // Reversing boost... see change in ranking
//        ranker.doSearch("Obama^0.1 Hillary^10", 5, System.out);
//        ranker.doSearch("(FIRST_LINE:\"Barack Obama\")^0.1 OR Hillary^10", 5, System.out);
//
//        // Complex query
//        ranker.doSearch("(FIRST_LINE:\"Barack Obama\"~5^10 AND Obama~.4) OR Hillary", 5, System.out);
    }

    public void doSearch(String index, String queryContent, PrintStream ps)
            throws Exception {

        Query query = _parser.parse(queryContent, "CONTENT");
        TopScoreDocCollector collector = TopScoreDocCollector.create(Configuration.SEARCH_NUMBER_HIT);
        _searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

//        ps.println("Found " + hits.length + " hits " + " for query " + queryContent + ":");
        for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;
            Document document = _searcher.doc(docId);

            ps.printf("%s Q0 %s %d %s myname %n", index, Paths.get(document.get("PATH")).getFileName().toString(), i, Configuration.DECIMAL_FORMAT.format(hits[i].score));
//            ps.println((i + 1) + ". (" + _df.format(hits[i].score)
//                    + ") " + document.get("PATH"));
        }
    }

}
