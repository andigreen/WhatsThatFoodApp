package com.wtf.whatsthatfoodapp.search;

class SearchUtils {

    /**
     * Returns a collection of query tokens, none of which contain spaces,
     * from the given query.
     */
    static String[] getTokens(String query) {
        return query.split("\\s+");
    }

}
