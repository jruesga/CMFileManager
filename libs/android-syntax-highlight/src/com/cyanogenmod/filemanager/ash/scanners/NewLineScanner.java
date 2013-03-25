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

package com.cyanogenmod.filemanager.ash.scanners;

import java.util.regex.Matcher;

import com.cyanogenmod.filemanager.ash.RegExpUtil;

/**
 * An scanner to process an input, reporting every text into new lines.
 */
public class NewLineScanner extends Scanner {

    private final NewLineScannerListener mListener;

    /**
     * The listener for the newline scanner
     */
    public interface NewLineScannerListener {
        /**
         * When a new line is ready
         *
         * @param newline The newline detected
         * @param start The start position of the new line within the input text
         * @param end The end position of the new line within the input text
         * @param sep The line separator detected
         * @return boolean If processor must continue with the next line
         */
        boolean onNewLine(CharSequence newline, int start, int end, CharSequence sep);
    }

    /**
     * Constructor of <code>Scanner</code>
     *
     * @param input The input
     * @param listener The listener where return every new line
     */
    public NewLineScanner(CharSequence input, NewLineScannerListener listener) {
        super(input);
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scan() {
        if (this.mInput.length() == 0) return;
        Matcher m = RegExpUtil.NEWLINE_PATTERN.matcher(this.mInput);
        int next = 0;
        while(m.find(next)) {
            CharSequence line = this.mInput.subSequence(next, m.start());
            if (!this.mListener.onNewLine(line, next, m.start(), m.group())) {
                return;
            }
            next = m.end();
        }
        // The non-matched data
        CharSequence line = this.mInput.subSequence(next, this.mInput.length());
        this.mListener.onNewLine(line, next, this.mInput.length(), null);
    }
}
