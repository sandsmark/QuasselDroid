package com.iskrembilen.quasseldroid.util;

import java.util.regex.Pattern;

public class RegExp {
    private CaseSensitivity caseSensitivity;
    private PatternSyntax patternSyntax;
    private String pattern;

    public RegExp(String pattern, CaseSensitivity caseSensitivity, PatternSyntax patternSyntax) {
        this.pattern = pattern;
        this.caseSensitivity = caseSensitivity;
        this.patternSyntax = patternSyntax;
    }

    public RegExp() {

    }

    public void setCaseSensitivity(CaseSensitivity caseSensitivity) {
        this.caseSensitivity = caseSensitivity;
    }

    public void setPatternSyntax(PatternSyntax patternSyntax) {
        this.patternSyntax = patternSyntax;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public static enum CaseSensitivity {
        CASE_SENSITIVE,
        CASE_INSENSITIVE
    }

    public static enum PatternSyntax {
        WILDCARD,
        REGEX
    }

    private String compileGlobExpression(String pattern) {
        // With \Q we start a quote, with \E we end it.
        // ^ and $ mean it will only match full strings, not parts
        // Then we replace every instance of * (matches 0 or more characters) with the appropriate
        // RegEx (.*) and replace ? (matches one character) with (.)
        return "^\\Q"+pattern.replace("*", "\\E\\S*?\\Q").replace("(?<!\\\\S\\*)\\?", "\\E\\S?\\Q")+"\\E$";
    }

    public Pattern getPattern() {
        return compilePattern(pattern);
    }

    public Pattern compilePattern(String pattern) {
        int flag = 0;
        if (caseSensitivity == CaseSensitivity.CASE_INSENSITIVE)
            flag = flag | Pattern.CASE_INSENSITIVE;

        if (patternSyntax == PatternSyntax.REGEX)
            return Pattern.compile(pattern, flag);
        else if (patternSyntax == PatternSyntax.WILDCARD)
            return Pattern.compile(compileGlobExpression(pattern), flag);
        else
            return Pattern.compile(pattern);
    }
}
