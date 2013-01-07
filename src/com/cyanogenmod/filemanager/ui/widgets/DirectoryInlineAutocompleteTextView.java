/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.filemanager.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.io.File;
import java.util.List;

/**
 * A widget based on  {@link InlineAutocompleteTextView} for autocomplete
 * directories like a bash console do (with tab key).
 */
public class DirectoryInlineAutocompleteTextView
                    extends InlineAutocompleteTextView
                    implements InlineAutocompleteTextView.OnTextChangedListener {

    /**
     * An interface to communicate validation events.
     */
    public interface OnValidationListener {
        /**
         * Method invoked when the value is void.
         */
        void onVoidValue();
        /**
         * Method invoked when the value is a valid value.
         */
        void onValidValue();
        /**
         * Method invoked when the value is a invalid value.
         */
        void onInvalidValue();
    }

    private static final String TAG = "DirectoryInlineAutocompleteTextView"; //$NON-NLS-1$

    private OnValidationListener mOnValidationListener;
    private String mLastParent;

    /**
     * Constructor of <code>DirectoryInlineAutocompleteTextView</code>.
     *
     * @param context The current context
     */
    public DirectoryInlineAutocompleteTextView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor of <code>DirectoryInlineAutocompleteTextView</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public DirectoryInlineAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor of <code>DirectoryInlineAutocompleteTextView</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle The default style to apply to this view. If 0, no style
     *        will be applied (beyond what is included in the theme). This may
     *        either be an attribute resource, whose value will be retrieved
     *        from the current theme, or an explicit style resource.
     */
    public DirectoryInlineAutocompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Method that initializes the view. This method loads all the necessary
     * information and create an appropriate layout for the view
     */
    private void init() {
        //Sets last parent
        this.mLastParent = ""; //$NON-NLS-1$

        //Set the listener
        setOnTextChangedListener(this);
        setCompletionString(File.separator);
    }

    /**
     * Method that set the listener for retrieve validation events.
     *
     * @param onValidationListener The listener for retrieve validation events
     */
    public void setOnValidationListener(OnValidationListener onValidationListener) {
        this.mOnValidationListener = onValidationListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTextChanged(String newValue, List<String> currentFilterData) {
        String value = newValue;

        //Check if directory is valid
        if (value.length() == 0) {
            if (this.mOnValidationListener != null) {
                this.mOnValidationListener.onVoidValue();
            }
        } else {
            boolean relative = FileHelper.isRelativePath(value);
            if (relative) {
                if (this.mOnValidationListener != null) {
                    this.mOnValidationListener.onInvalidValue();
                }
            } else {
                if (this.mOnValidationListener != null) {
                    this.mOnValidationListener.onValidValue();
                }
            }
        }

        //Ensure data
        if (!value.startsWith(File.separator)) {
            currentFilterData.clear();
            this.mLastParent = ""; //$NON-NLS-1$
            return;
        }

        //Get the new parent
        String newParent = FileHelper.getParentDir(new File(value));
        if (newParent == null) {
            newParent = FileHelper.ROOT_DIRECTORY;
        }
        if (!newParent.endsWith(File.separator)) {
            newParent += File.separator;
        }
        if (value.compareTo(File.separator) == 0) {
            newParent = File.separator;
            currentFilterData.clear();
        } else if (value.endsWith(File.separator)) {
            //Force the change of parent
            newParent = new File(value, "a").getParent(); //$NON-NLS-1$
            if (!newParent.endsWith(File.separator)) {
                newParent += File.separator;
            }
            currentFilterData.clear();
        } else {
            value = newParent;
        }

        //If a new path is detected, then load the new data
        if (newParent.compareTo(this.mLastParent) != 0 || currentFilterData.isEmpty()) {
            this.mLastParent = newParent;
            currentFilterData.clear();
            try {
                List<String> newData =
                        CommandHelper.quickFolderSearch(getContext(), value, null);
                currentFilterData.addAll(newData);
            } catch (Throwable ex) {
                Log.e(TAG, "Quick folder search failed", ex); //$NON-NLS-1$
                currentFilterData.clear();
            }
        }
    }
}
