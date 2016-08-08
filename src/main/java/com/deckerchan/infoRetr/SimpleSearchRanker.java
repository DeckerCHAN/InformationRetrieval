/**
 * Exhibits standard Lucene searches for ranking documents.
 *
 * @author Scott Sanner, Paul Thomas
 */

package com.deckerchan.infoRetr;

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

        PrintStream fileOut = new PrintStream(new FileOutputStream(Configuration.OUT_FILE_PATH, false));

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

        System.out.printf("File wrote to %s.%n", Configuration.OUT_FILE_PATH);
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
