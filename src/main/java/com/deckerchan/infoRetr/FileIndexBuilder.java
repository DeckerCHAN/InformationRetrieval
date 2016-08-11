/**
 * Build a file-based Lucene inverted index.
 *
 * @author Scott Sanner, Paul Thomas
 */

package com.deckerchan.infoRetr;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class FileIndexBuilder {

    public Analyzer _analyzer;
    public String _indexPath;

    public FileIndexBuilder(String index_path) {

        // Specify the analyzer for tokenizing text.
        // The same analyzer should be used for indexing and searching
        // See the lucene "analysers-common" library for some more options;
        // the .jar file is included in the lib/ directory, and there is
        // good documentation online
        _analyzer = new StandardAnalyzer();

        // Store the index path
        _indexPath = index_path;
    }

    public static void main(String[] args) throws Exception {

        String index_path = Configuration.INDEX_PATH;
        FileIndexBuilder builder = new FileIndexBuilder(index_path);
        builder.addFiles(FileFinder.GetAllFiles(Configuration.DOCUMENT_PATH, null, true),
                true /*clear_old_index = false if adding*/);

        //IndexDisplay.Display(index_path, System.out);
    }

    /**
     * Main procedure for adding files to the index
     *
     * @param files files need to add
     * @param clear_old_index set to true to create a new index, or
     *                        false to add to a currently existing index
     * @return successful add
     */
    public boolean addFiles(List<File> files, boolean clear_old_index) {

        try {
            // The boolean arg in the IndexWriter ctor means to
            // create a new index, overwriting any existing index
            //
            // NOTES: Set create=false to add to an index (even while
            //        searchers and readers are accessing it... additional
            //        content goes into separate segments).
            //
            //        To merge can use:
            //        IndexWriter.addIndexes(IndexReader[]) and
            //        IndexWriter.addIndexes(Directory[])
            //
            //        Index is optimized on optimize() or close()
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(_analyzer);
            if (clear_old_index) {
                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }
            Directory d = new SimpleFSDirectory(Paths.get(_indexPath));
            IndexWriter w = new IndexWriter(d, indexWriterConfig);

            // Add all files
            for (File f : files) {
                System.out.println("Adding: " + f.getPath());
                DocAdder.AddDoc(w, f);
            }

            // Close index writer
            w.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
