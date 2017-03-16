package com.wtf.whatsthatfoodapp.search;

class SearchUtils {

    /**
     * Returns a collection of query tokens, none of which contain spaces,
     * from the given query.
     */
    static String[] getTokens(String query) {
        return raisePound(query).split("\\s+");
    }

    /**
     * Returns a string where the every # with word chars following, is
     * replaced by the symbol ⋕ (a Unicode approximation with codepoint >
     * 128, so that it is included in tokens by the SQLite FTS "simple"
     * tokenizer).
     */
    static String raisePound(String str) {
        return str.replaceAll("#(?=[^\\s]+)", "\u22d5");
    }

}
