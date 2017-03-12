package com.wtf.whatsthatfoodapp;

import android.net.Uri;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.regex.Pattern;

public class TextUtil {

    private static final String APP_SCHEME = "com.wtf.whatsthatfoodapp://";
    private static final Pattern TAG_PATTERN = Pattern.compile("#\\w+");

    public static void linkifyTags(TextView view) {
        Linkify.addLinks(view, TAG_PATTERN, APP_SCHEME);
    }

    public static String removeScheme(Uri uri) {
        String uriStr = uri.toString();
        if (uriStr.startsWith(APP_SCHEME)) {
            return uriStr.substring(APP_SCHEME.length());
        }
        return uriStr;
    }

}
