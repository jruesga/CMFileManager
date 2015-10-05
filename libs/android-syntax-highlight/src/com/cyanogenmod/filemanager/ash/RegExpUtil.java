/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.filemanager.ash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A helper class for deal with patters
 */
public final class RegExpUtil {

    /**
     * A constant that is returned when the no expression matches.
     */
    public static final int NO_MATCH = -1;

    /**
     * New line pattern
     */
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("(\r\n|\n|\r)"); //$NON-NLS-1$

    /**
     * Method that returns the last match position of a regexp,
     *
     * @param pattern The patter
     * @param input The input
     * @param withPattern Whether the return position should contains the pattern or not.
     * @return int The matched position or -1
     */
    public static int getLastMatch(Pattern pattern, CharSequence input, boolean withPattern) {
        Matcher m = pattern.matcher(input);
        int p = NO_MATCH;
        while (m.find()) {
            p = withPattern ? m.start() : m.end();
        }
        return p;
    }

    /**
     * Method that returns the next match position of a regexp,
     *
     * @param pattern The patter
     * @param input The input
     * @param withPattern Whether the return position should contains the pattern or not.
     * @return int The matched position or -1
     */
    public static int getNextMatch(Pattern pattern, CharSequence input, boolean withPattern) {
        Matcher m = pattern.matcher(input);
        int p = NO_MATCH;
        if (m.find()) {
            return withPattern ? m.end() : m.start();
        }
        return p;
    }
}
