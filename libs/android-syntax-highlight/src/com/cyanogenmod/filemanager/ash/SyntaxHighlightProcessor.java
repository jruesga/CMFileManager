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

import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.io.File;

/**
 * The base class for all the syntax highlight processors.</br>
 */
public abstract class SyntaxHighlightProcessor {

    protected final ISyntaxHighlightResourcesResolver mResourcesResolver;

    /**
     * Constructor of <code>SyntaxHighlightProcessor</code>
     *
     * @param resolver A class for resolve resources
     */
    public SyntaxHighlightProcessor(ISyntaxHighlightResourcesResolver resolver) {
        super();
        this.mResourcesResolver = resolver;
    }

    /**
     * Method that request to the syntax highlight processor if it is able to parse
     * the file
     *
     * @param file The file to check
     * @return boolean If the syntax highlight processor accepts process the file
     */
    protected abstract boolean accept(File file);

    /**
     * Method that initializes the processor
     */
    public abstract void initialize();

    /**
     * Method that request to the syntax highlight processor to do process and highlight a
     * document. This method request a full process.
     *
     * @param spanable The spannable source to highlight
     */
    public abstract void process(Spannable spanable);

    /**
     * Method that request to the syntax highlight processor to process and highlight a
     * document. This method request a partial process.
     *
     * @param spanable The spannable source to highlight
     * @param start The start of spannable to process
     * @param end The end of spannable to process
     */
    public abstract void process(Spannable spanable, int start, int end);

    /**
     * Method that cancels the active processor
     */
    public abstract void cancel();

    /**
     * Method that clear all the existent spans
     *
     * @param spanable The spannable
     */
    @SuppressWarnings("static-method")
    public void clear(Spannable spanable) {
        ForegroundColorSpan[] spans =
                spanable.getSpans(0, spanable.length(), ForegroundColorSpan.class);
        int cc = spans.length;
        for (int i = 0; i < cc; i++) {
            spanable.removeSpan(spans[i]);
        }
    }


    /**
     * Method that sets a new <code>Spannable</code>.
     *
     * @param spanable The spannable
     * @param color The color of the span
     * @param start The start of the span
     * @param end The end of the span
     */
    @SuppressWarnings("static-method")
    protected void setSpan(Spannable spanable, int color, int start, int end) {
        if (start == end) return;
        spanable.setSpan(
                new ForegroundColorSpan(color),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
