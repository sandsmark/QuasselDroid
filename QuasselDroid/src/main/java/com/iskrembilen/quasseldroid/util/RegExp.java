/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

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
        return "^\\Q"+pattern.replace("*", "\\E.*\\Q").replace("\\?", "\\E.\\Q")+"\\E$";
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
