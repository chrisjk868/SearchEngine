import java.util.*;
import java.io.*;
import java.nio.file.*;

// Christopher Ku
// Section: AG with Jiamae Wang
// Assessment 3: SearchEngine
//
// This SearchEngine class represents an inverted index of terms
// that is individually associated with different documents it appears in.
// A search query could be used to extract documents that contains
// the terms within the search query.
public class SearchEngine {
    private Map<String, Set<String>> invertedIndex;

    /**
     * Used to construct a Search Engine instance
     * that represents an inverted index.
     */
    public SearchEngine() {
        this.invertedIndex = new HashMap<>();
    }

    /**
     * Takes in a String parameter that represents
     * a document with different words. By ignoring any special
     * characters and also ignoring the cases of
     * each term contained within the document parameter
     * it adds and indexes each term within the passed
     * in String to the inverted index.
     *
     * @param document   A String that contains different terms
     *                   that is used to represent a document.
     */
    public void index(String document) {
        Set<String> term = this.split(document);
        for (String word : term) {
            if (!this.invertedIndex.containsKey(word)) {
                Set<String> emptySet = new HashSet<>();
                this.invertedIndex.put(word, emptySet);
            }
            this.invertedIndex.get(word).add(document);
        }
    }

    /**
     * Returns a set of documents in their original form
     * that contains terms associated with the passed in
     * String query by also ignoring any special characters
     * and the cases of individual terms within the search
     * query.
     *
     * @param query   A String parameter that represents a
     *                search by the client, which could contain
     *                different terms.
     *
     * @return   returns a set of documents in the form of Strings
     *           that could contain terms within the search query.
     *           returns an empty set if none of the words from
     *           the query matches the term within the inverted index.
     *           Also returns an empty set if the given query is empty.
     */
    public Set<String> search(String query) {
        Boolean start = true;
        Set<String> validDocuments = new HashSet<>();
        Set<String> processedQuery = this.removeIrregularities(query);
        for(String queryTerm : processedQuery) {
            if (start) {
                validDocuments.addAll(this.invertedIndex.get(queryTerm));
                start = false;
            }
            validDocuments.retainAll(this.invertedIndex.get(queryTerm));
        }
        return validDocuments;
    }

    /**
     * Processes the query by client by ignoring any
     * special characters and cases of each term. Then
     * manually removes any terms within the search query
     * which isn't indexed within the inverted index and
     * returns a set of Strings representing the final
     * and ready version of the client's search query for
     * further processing. Does this by taking in a String
     * parameter representing the client's search query.
     *
     * @param query   A String parameter that represents a
     *                search by the client, which could contain
     *                different terms.
     *
     * @return   A set in the form of Strings that is the processed
     *           and final version of the client's search query with
     *           any previously unindexed terms removed and the search
     *           query terms' cases ignored and special characters removed.
     */
    private Set<String> removeIrregularities(String query) {
        Set<String> normalizedQuery = this.split(query);
        for (Iterator<String> iterator = normalizedQuery.iterator(); iterator.hasNext();) {
            String value = iterator.next();
            if (!this.invertedIndex.containsKey(value)) {
                iterator.remove();
            }
        }
        return normalizedQuery;
    }

    // Return the set of normalized terms split from the given text
    private static Set<String> split(String text) {
        Set<String> result = new HashSet<>();
        for (String term : text.split("\\s+")) {
            term = normalize(term);
            if (!term.isEmpty()) {
                result.add(term);
            }
        }
        return result;
    }

    // Return a standardized lowercase representation of the given string
    private static String normalize(String s) {
        return s.toLowerCase().replaceAll("(^\\p{P}+\\s*)|(\\s*\\p{P}+$)", "");
    }

    public static void main(String[] args) throws IOException {
        SearchEngine engine = new SearchEngine();
        engine.index("a b c d");
        engine.index("a b c z");
        System.out.println("a");
        System.out.println(engine.search("a"));
        System.out.println("a z");
        System.out.println(engine.search("a z"));
        System.out.println("x y z t");
        System.out.println(engine.search("x y z t"));
    }
}

