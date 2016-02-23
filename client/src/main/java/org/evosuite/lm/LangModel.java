package org.evosuite.lm;

import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a language model, a set of bigrams, unigrams and associated
 * log-probabilities.
 */
public class LangModel {

    // class variables
    // Hashes storing various Language Model probabilities
    /**
     * Probability of a unigram occurring.
     */
    private Map<String, Double> unigram_probs = new HashMap<String, Double>();
    /**
     * Unigram backoff probabilities (used in bigram probability estimation).
     */
    private Map<String, Double> unigram_backoff_probs = new HashMap<String, Double>();
    /**
     * Probability that Unigram2 follows Unigram1, where each key is of the form "Unigram1 Unigram2".
     */
    private Map<String, Double> bigram_probs = new HashMap<String, Double>();

    //Sentinel unigram values:
    public static final String START_OF_STRING = "<s>";
    public static final String END_OF_STRING="</s>";
    public static final String START_NEW_WORD="<w>";

    private double unknown_char_prob = 0;


    // Hashes to store most probable next characters in bigram
    /**
     * Mapping of the nth most likely unigrams to follow each unigram.
     * Encoded as: <code>(unigram)(n)> -> (unigram)</code>
     */
    private HashMap<String, String> context_char = new HashMap<String, String>();
    /**
     * Mapping of the probability of the nth most likely unigram to follow each unigram.
     * Encoded as: <code>(unigram)(n)> -> (log_probability)</code>
     */
    private HashMap<String, Double> context_prob = new HashMap<String, Double>();

    // Maximum number of characters to predict for each bigram
    int predicted_chars = 10;

    // Constructors
    // Read in data from language model to be manipulated later
    // Takes language model file as argument

    /**
     * Load the language model.
     * @param lmFileName path to a language model file.
     * @throws IOException if the model file can't be found or read.
     */
    public LangModel(String lmFileName) throws IOException {



        // Flag to indicate length of n-grams currently being read (0 == read
        // nothing)
        int ngram_len = 0; //size of the n-grams we're reading (i.e. ngram_len = 5 implies 5-grams).

        InputStream fstream = LangModel.class.getClassLoader().getResourceAsStream(lmFileName);
        // FileInputStream fstream = new FileInputStream(lmFileName);

        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;

        double highest_unigram_prob = 0;

        // Read file line by line
        while ((strLine = br.readLine()) != null) {
            Pattern ngram_len_p = Pattern.compile("(\\d+)-grams:");
            Matcher match_ngram_len = ngram_len_p.matcher(strLine);
            //does line match (\d+)-grams: ?
            if (match_ngram_len.find()) {
                ngram_len = Integer.parseInt(match_ngram_len.group(1));

            } else if (ngram_len == 1) {
                //We're looking at unigrams;
                Pattern unigram_p = Pattern
                        .compile("([-0-9\\.]+)\\s*(\\S+)\\s*([-0-9\\.]+)");
                // Match with <floating point number> <one or more chars> <floating point number>
                //                        |                   |                 +------ backoff probability
                //                        |                   +------------------------ unigram
                //                        +-------------------------------------------- unigram probability
                Matcher match_unigram = unigram_p.matcher(strLine);
                if (match_unigram.find()) {

                    double unigram_prob = Double.parseDouble(match_unigram
                            .group(1));
                    String unigram = match_unigram.group(2);
                    double unigram_backoff_prob = Double
                            .parseDouble(match_unigram.group(3));

                    unigram_probs.put(unigram, unigram_prob);
                    unigram_backoff_probs.put(unigram, unigram_backoff_prob);

                    if(unigram_prob < unknown_char_prob) {
                        unknown_char_prob = unigram_prob;
                    } // if
                    if(unigram_prob > highest_unigram_prob) {
                        highest_unigram_prob = unigram_prob;
                    } //if

                } // if

            } else if (ngram_len == 2) {
                Pattern bigram_p = Pattern.compile("([-0-9\\.]+)\\s*(\\S+) (\\S+)");
                //Match line with <floating point number> <one or more chars> <one or more chars>
                //                            |                   |                    +---- end char of bigram
                //                            |                   +------------------------- start char of bigram
                //                            +--------------------------------------------- bigram probability
                Matcher match_bigram = bigram_p.matcher(strLine);
                if (match_bigram.find()) {
                    double bigram_prob = Double.parseDouble(match_bigram
                            .group(1));
                    String bigram_start = match_bigram.group(2);
                    String bigram_end = match_bigram.group(3);
                    String bigram = bigram_start + " " + bigram_end;

                    bigram_probs.put(bigram, bigram_prob);

                } // if

            } // if/else
        } // while
        // Close the input stream
        in.close();

        ValueComparator bvc = new ValueComparator(bigram_probs);
        TreeMap<String, Double> sorted_bigram_probs = new TreeMap<String, Double>(
                bvc);

        //Store bigrams sorted by probability:
        sorted_bigram_probs.putAll(bigram_probs);

        // Regular expressions setup
        Pattern context_p = Pattern.compile("(\\S+) (\\S+)");

        //Go through each bigram in order (most likely first) and build a
        // table of the predicted_chars most likely characters to follow each character.
        for (Map.Entry<String, Double> entry : sorted_bigram_probs.entrySet()) {
            Matcher match_context = context_p.matcher(entry.getKey());
            if (match_context.find()) {
                String pre = match_context.group(1);
                String middle = match_context.group(2);

                // Add to hash (do this by starting counter at 0 and then
                // testing hash and
                // filling first empty slot. If no empty slot found then value
                // is not stored.
                for (int c = 0; c < predicted_chars; c++) {
                    String key = pre + c;
                    if (!context_char.containsKey(key)) {
                        context_char.put(key, middle);
                        context_prob.put(key, entry.getValue());
                        break;
                    } // if
                } // for
            } // if

        } // for

        // Print out as sanity check
        //for (Map.Entry<String, String> entry : context_char.entrySet()) {
        // System.out.println("Key = " + entry.getKey() + ", Value = " +
        // entry.getValue());
        //}

    } // LangModel

    // Method which returns language model score for string str Splits
    // string into bigrams and looks up the probability for each. If
    // the bigram isn't found then backs off to use the unigram and
    // backoff probabilities str is string for which score is
    // computed, verbose is flag indicating whether to print out
    // details about how this score is computed

    /**
     * Splits a string into bigrams and calculates the language model score.
     * For each bigram, it looks up the probability. The score is the geometric mean
     * of the probability of each bigram in the string according to the model.
     *
     * If a given bigram isn't in the model, unigrams are used to estimate the probability
     * of the bigram instead
     *
     * @param str String for which to compute the score
     * @param verbose whether to print information
     * @return
     */
    public double score(String str, boolean verbose) {

        if (verbose == true) {
            System.out.println("String is " + str);
        } // if

        double log_prob = 0;

        // Get length of string
        int no_chars = str.length();

        // Break string down into bigrams
        for (int i = -1; i < (no_chars - 1); i++) {
            String first_char;
            String second_char;
            if (i == -1) {
                first_char = "<s>";
                second_char = str.substring(0, 1);
            } else {
                first_char = str.substring(i, i + 1);
                second_char = str.substring(i + 1, i + 2);
            } // if/else

            if (first_char.equals(" ")) {
                first_char = "<w>";
            } // if
            if (second_char.equals(" ")) {
                second_char = "<w>";
            } // if
            String bigram = first_char + " " + second_char;

            if (verbose == true) {
                System.out.println("Bigram is " + bigram);
            } // if

            // Get negative log likelihood for each bigram
            // (Either get directly or estimate using backoff)
            if (bigram_probs.containsKey(bigram)) {
                // Get direct bigram probabilities
                double bigram_prob = bigram_probs.get(bigram);
                log_prob = log_prob + bigram_prob;
                if (verbose == true) {
                    System.out.println("Direct bigram prob: "
                            + Math.pow(10, bigram_prob) + "\n");
                } // if
            } else if(unigram_probs.containsKey(second_char) && unigram_backoff_probs.containsKey(first_char)){

                // Otherwise split into unigrams and do backoff
                double unigram_backoff_prob = unigram_backoff_probs
                        .get(first_char);
                log_prob = log_prob + unigram_backoff_prob;
                // System.out.println("Unigram ("+first_char+") backoff prob: "+unigram_backoff_prob);




                double unigram_prob = unigram_probs.get(second_char);
                log_prob = log_prob + unigram_prob;

                if (verbose == true) {
                    double bigram_prob = unigram_backoff_prob + unigram_prob;
                    System.out.println("Inferred bigram prob: "
                            + Math.pow(10, bigram_prob)
                            + " (formed from unigram probs " + first_char
                            + ": " + Math.pow(10, unigram_backoff_prob)
                            + " and " + second_char + ": "
                            + Math.pow(10, unigram_prob) + ")\n");
                } // if
            } else {
                //Note: we don't penalise strings containing weird (non-printable) characters.
                //If we hit one (this block), just do nothing.
                //throw new RuntimeException("Language Model can't give predictions for bigram " + bigram);

                log_prob += unknown_char_prob;

            }

        } // for

        // Convert log probs to probs and take geometric mean
        //TODO: if none of the chars are accepted bigrams or unigrams this function used to return 1.0...
        //did averaging the prob (rather than exponentiating the average log-prob) break anything?
        double avg_prob = Math.pow(10, log_prob / ((double) no_chars));

        return avg_prob;

    } // score

    /**
     * Convenience method for {@link #score(String, boolean)} with verbose flag set to false.
     */
    public double score(String str) {

        return score(str, false);

    } // score

    /**
     * @return the nth most likely character to follow pre
     */
    public String predict_char(String pre, int n) {

        if (pre.equals(" ")) {
            pre = "<w>";
        }

        String key = pre + n;

        if (n < 0 || n > predicted_chars) {
            return null;
        } else {
            return context_char.get(key);
        } // if/else

    } // predict_char

    /**
     * @return the nth most likely character that a string will start with
     */
    public String predict_char(int n) {

        return predict_char("<s>", n);

    } // predict_char

    /**
     * Method which returns the probability of the nth most likely character, given a
     * preceeding character (pre). Use in combination with the predict_char methods.
     * @return the probability of the nth character that is most likely to appear
     */
    public double predict_char_prob(String pre, int n) {

        if (n < 0 || n > predicted_chars) {
            return 0;
        }

        if (pre.equals(" ")) {
            pre = "<w>";
        }

        String key = pre + n;
        Double prob = context_prob.get(key);

        if (prob != null) {
            prob = Math.pow(10, prob);
        } // if

        return prob;

    } // predict_char_prob

    /**
     * Method which returns the probability of the nth most likley character at
     * the start of a sentence.
     * N.B. Simply calls predict_char_prob/2 with preceeding char set to "<s>".
     * @return the probability associated with the nth most likely character to start a sentence
     */
    public double predict_char_prob(int n) {

        return predict_char_prob("<s>", n);

    } // predict_char_prob

    public boolean isMagicChar(String character){

        return character.equals(START_NEW_WORD) || character.equals(END_OF_STRING) || character.equals(START_OF_STRING);
    }

    public boolean isEndOfSentence(String character){
        return character.equals(END_OF_STRING);
    }


} // LangModel

/**
 * Compares values based on their values in an associated Map.
 */
class ValueComparator implements Comparator<String> {

    Map<String, Double> base;

    /**
     * Create a new comparator using a mapping of probabilities.
     * The comparator will use the attached probabilities to return the
     * ordering for two Strings.
     * @param base a mapping of probabilities for strings.
     */
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}