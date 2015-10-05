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

package com.cyanogenmod.filemanager.ash.spi;

import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import com.cyanogenmod.filemanager.ash.HighlightColors;
import com.cyanogenmod.filemanager.ash.ISyntaxHighlightResourcesResolver;
import com.cyanogenmod.filemanager.ash.RegExpUtil;
import com.cyanogenmod.filemanager.ash.SyntaxHighlightProcessor;
import com.cyanogenmod.filemanager.ash.scanners.NewLineScanner;
import com.cyanogenmod.filemanager.ash.scanners.NewLineScanner.NewLineScannerListener;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <b>properties</b> highlight processor class.</br>
 * </br>
 * The behaviour of this class is:</br>
 * <ul>
 * <li>Comments start with # (only spaces are allowed prior to comment)</li>
 * <li>Assignment character (=) separates key from value</li>
 * <li>Arguments exists only in values, and are composed by {a digit}</li>
 * <li>Values can be extended in multiple lines if line ends with the char "\". A
 * comment in multiline breaks the multiline and starts a new property.</li>
 * </ul>
 * </br>
 * IMP! This class is not thread safe. Calling "process" methods should be
 * done in a synchronous way.
 */
public class PropertiesSyntaxHighlightProcessor extends SyntaxHighlightProcessor {

    private static final String EXT_PROP = "prop"; //$NON-NLS-1$
    private static final String EXT_PROPERTIES = "properties"; //$NON-NLS-1$

    private static final Pattern COMMENT = Pattern.compile("^\\s*#.*"); //$NON-NLS-1$
    private static final Pattern MULTILINE = Pattern.compile(".*\\\\\\s*$"); //$NON-NLS-1$
    private static final Pattern ASSIGNMENT = Pattern.compile("="); //$NON-NLS-1$
    private static final Pattern ARGUMENT = Pattern.compile("\\{\\d+\\}"); //$NON-NLS-1$

    protected Spannable mSpannable;
    private boolean mMultiLine;

    private int mKeyColor;
    private int mAssignmentColor;
    private int mCommentColor;
    private int mValueColor;
    private int mArgumentColor;

    /**
     * Constructor of <code>PropertiesSyntaxHighlightProcessor</code>
     *
     * @param resolver A class for resolve resources
     */
    public PropertiesSyntaxHighlightProcessor(ISyntaxHighlightResourcesResolver resolver) {
        super(resolver);
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean accept(File file) {
        if (file == null) return false;
        return file.getName().toLowerCase().endsWith(EXT_PROP) ||
               file.getName().toLowerCase().endsWith(EXT_PROPERTIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        this.mMultiLine = false;
        this.mSpannable = null;
        if (this.mResourcesResolver != null) {
            this.mKeyColor = this.mResourcesResolver.getColor(
                    HighlightColors.TEXT.getId(),
                    HighlightColors.TEXT.getResId(),
                    HighlightColors.TEXT.getDefault());
            this.mAssignmentColor = this.mResourcesResolver.getColor(
                    HighlightColors.ASSIGNMENT.getId(),
                    HighlightColors.ASSIGNMENT.getResId(),
                    HighlightColors.ASSIGNMENT.getDefault());
            this.mCommentColor = this.mResourcesResolver.getColor(
                    HighlightColors.SINGLE_LINE_COMMENT.getId(),
                    HighlightColors.SINGLE_LINE_COMMENT.getResId(),
                    HighlightColors.SINGLE_LINE_COMMENT.getDefault());
            this.mValueColor = this.mResourcesResolver.getColor(
                    HighlightColors.VARIABLE.getId(),
                    HighlightColors.VARIABLE.getResId(),
                    HighlightColors.VARIABLE.getDefault());
            this.mArgumentColor = this.mResourcesResolver.getColor(
                    HighlightColors.KEYWORD.getId(),
                    HighlightColors.KEYWORD.getResId(),
                    HighlightColors.KEYWORD.getDefault());
        } else {
            // By default
            this.mKeyColor = HighlightColors.TEXT.getDefault();
            this.mAssignmentColor = HighlightColors.TEXT.getDefault();
            this.mCommentColor = HighlightColors.SINGLE_LINE_COMMENT.getDefault();
            this.mValueColor = HighlightColors.VARIABLE.getDefault();
            this.mArgumentColor = HighlightColors.KEYWORD.getDefault();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Spannable spanable) {
        this.mMultiLine = false;
        this.mSpannable = spanable;
        clear(spanable);
        NewLineScanner scanner = new NewLineScanner(spanable, new NewLineScannerListener() {
            @Override
            public boolean onNewLine(CharSequence newline, int start, int end, CharSequence sep) {
                processNewLine(newline, start, end);
                return true;
            }

        });
        scanner.scan();
        this.mSpannable = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Spannable spanable, final int start, final int end) {
        // We need a Retrieve the previous line
        this.mMultiLine = false;
        this.mSpannable = spanable;
        CharSequence seqs = spanable.subSequence(0, start);
        CharSequence seqe = spanable.subSequence(end, spanable.length());
        int s1 = RegExpUtil.getLastMatch(RegExpUtil.NEWLINE_PATTERN, seqs, false);
        if (s1 == RegExpUtil.NO_MATCH) {
            s1 = 0;
        }
        int e1 = RegExpUtil.getNextMatch(RegExpUtil.NEWLINE_PATTERN, seqe, false);
        if (e1 == RegExpUtil.NO_MATCH) {
            e1 = spanable.length();
        } else {
            e1 += end;
        }

        // Also, we need to know about if the previous line is multiline
        if (s1 > 0) {
            int s2 = RegExpUtil.getLastMatch(RegExpUtil.NEWLINE_PATTERN, seqs, true);
            CharSequence seqnl = spanable.subSequence(0, s2);
            int snl = RegExpUtil.getLastMatch(RegExpUtil.NEWLINE_PATTERN, seqnl, false);
            Matcher mlm = MULTILINE.matcher(
                    spanable.subSequence(snl != RegExpUtil.NO_MATCH ? snl : 0, s2));
            this.mMultiLine = mlm.matches();
        }

        // Process the new line
        if (s1 != e1) {
            processNewLine(spanable.subSequence(s1, e1), s1, e1);
        }

        // Now, multiline again (next line). We check always the next line, because we
        // don't know if user delete multiline flag in the current line
        e1 = RegExpUtil.getNextMatch(RegExpUtil.NEWLINE_PATTERN, seqe, true);
        if (e1 != RegExpUtil.NO_MATCH) {
            e1 += end;
            seqe = spanable.subSequence(e1, spanable.length());
            int e2 = RegExpUtil.getNextMatch(RegExpUtil.NEWLINE_PATTERN, seqe, false);
            if (e2 == RegExpUtil.NO_MATCH) {
                e2 = spanable.length();
            } else {
                e2 += e1;
            }
            processNewLine(spanable.subSequence(e1, e2), e1, e2);
        }

        this.mSpannable = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        // Not needed by this processor
    }

    /**
     * A method to process every new line
     *
     * @param newline The newline
     * @param start The start position of the line
     * @param end The end position of the line
     * @hide
     */
    void processNewLine(CharSequence newline, int start, int end) {
        // Remove all spannable of the line (this processor doesn't multiline spans and
        // only uses ForegroundColorSpan spans)
        ForegroundColorSpan[] spans =
                this.mSpannable.getSpans(start, end, ForegroundColorSpan.class);
        int cc = spans.length;
        for (int i = 0; i < cc; i++) {
            this.mSpannable.removeSpan(spans[i]);
        }

        // Find comment
        Matcher cm = COMMENT.matcher(newline);
        if (cm.matches()) {
            // All the line is a comment
            setSpan(this.mSpannable, this.mCommentColor, start, end);
            this.mMultiLine = false;
            return;
        }

        // Has multiline
        Matcher mlm = MULTILINE.matcher(newline);
        boolean ml = mlm.matches();

        //Find the assignment
        int k = this.mMultiLine ? -1 : start;
        int v = start;
        int v2 = 0;
        int a = -1;
        if (!this.mMultiLine) {
            Matcher am = ASSIGNMENT.matcher(newline);
            if (am.find()) {
                // Assignment found
                v2 = am.start() + 1;
                a = start + am.start();
                v = a + 1;
            }
        }

        // All the string is a key
        if (!this.mMultiLine && a == -1) {
            setSpan(this.mSpannable, this.mKeyColor, start, end);

        } else {
            // Key
            if (!this.mMultiLine) {
                setSpan(this.mSpannable, this.mKeyColor, k, a);
            }
            // Assignment
            if (!this.mMultiLine) {
                setSpan(this.mSpannable, this.mAssignmentColor, a, a + 1);
            }
            // Value
            setSpan(this.mSpannable, this.mValueColor, v, end);
            // Argument
            Matcher argm = ARGUMENT.matcher(newline);
            while (argm.find(v2)) {
                int s = start + argm.start();
                int e = start + argm.end();
                setSpan(this.mSpannable, this.mArgumentColor, s, e);
                v2 = argm.end();
            }
        }

        // Multiline?
        this.mMultiLine = ml;
    }
}
