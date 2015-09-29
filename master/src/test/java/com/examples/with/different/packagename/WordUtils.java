package com.examples.with.different.packagename;

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.text.WordUtils)
 */
public class WordUtils {

    /**
     * <p>Checks if the String contains all words in the given array.</p>
     *
     * <p>
     * A {@code null} String will return {@code false}. A {@code null, zero
     * length search array or if one element of array is null will return {@code false}.
     * </p>
     *
     * <pre>
     * WordUtils.containsAllWords(null, *)            = false
     * WordUtils.containsAllWords("", *)              = false
     * WordUtils.containsAllWords(*, null)            = false
     * WordUtils.containsAllWords(*, [])              = false
     * WordUtils.containsAllWords("abcd", "ab", "cd") = false
     * WordUtils.containsAllWords("abc def", "def", "abc") = true
     * </pre>
     *
     *
     * @param str The str to check, may be null
     * @param words The array of String words to search for, may be null
     * @return {@code true} if all search words are found, {@code false} otherwise
     */
    public static boolean containsAllWords(CharSequence word, CharSequence... words) {
        if (StringUtils.isEmpty(word) || ArrayUtils.isEmpty(words)) {
            return false;
        }
        for (CharSequence w : words) {
            if (StringUtils.isBlank(w)) {
                return false;
            }
            Pattern p = Pattern.compile(".*\\b" + w + "\\b.*");
            if (!p.matcher(word).matches()) {
                return false;
            }
        }
        return true;
    }
}
