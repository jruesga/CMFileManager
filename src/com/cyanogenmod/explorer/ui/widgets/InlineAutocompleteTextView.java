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

package com.cyanogenmod.explorer.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.util.DialogHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A widget that performs an inline autocomplete for an {@link EditText}
 * control.
 */
public class InlineAutocompleteTextView extends RelativeLayout
            implements View.OnClickListener, View.OnLongClickListener,
            View.OnKeyListener, TextWatcher {

    /**
     * An interface to communicate events when a text value is changed.
     */
    public interface OnTextChangedListener {
        /**
         * Method invoked when the value of the initial directory was changed.
         *
         * @param newValue The new value of the text
         * @param currentFilterData The current set of string for filter
         */
        void onTextChanged(String newValue, List<String> currentFilterData);
    }

    private List<String> mData;
    private OnTextChangedListener mOnTextChangedListener;

    private EditText mBackgroundText;
    private EditText mForegroundText;
    private String mCompletionString = null;

    private int mFilter = 0;

    /**
     * Constructor of <code>InlineAutocompleteTextView</code>.
     *
     * @param context The current context
     */
    public InlineAutocompleteTextView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor of <code>InlineAutocompleteTextView</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public InlineAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor of <code>InlineAutocompleteTextView</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle The default style to apply to this view. If 0, no style
     *        will be applied (beyond what is included in the theme). This may
     *        either be an attribute resource, whose value will be retrieved
     *        from the current theme, or an explicit style resource.
     */
    public InlineAutocompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Method that initializes the view. This method loads all the necessary
     * information and create an appropriate layout for the view
     */
    private void init() {
        //Initialize data
        this.mData = new ArrayList<String>();
        this.mOnTextChangedListener = null;

        //Inflate the view
        ViewGroup v = (ViewGroup)inflate(getContext(), R.layout.inline_autocomplete, null);
        addView(v);

        //Retrieve views
        this.mBackgroundText = (EditText)findViewById(R.id.inline_autocomplete_bg_text);
        this.mForegroundText = (EditText)findViewById(R.id.inline_autocomplete_fg_text);
        this.mForegroundText.setMovementMethod(new ScrollingMovementMethod());
        this.mForegroundText.addTextChangedListener(this);
        this.mForegroundText.setOnKeyListener(this);
        this.mForegroundText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    Activity activity = (Activity)getContext();
                    activity.getWindow().
                        setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        this.mForegroundText.requestFocus();

        View button = findViewById(R.id.inline_autocomplete_button_tab);
        button.setOnClickListener(this);
        button.setOnLongClickListener(this);

        //Initialize text views
        setText(""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    //Ignore enter
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * Method that sets the current text value.
     *
     * @param value The value to set to current text value
     */
    public void setText(String value) {
        this.mBackgroundText.setVisibility(View.INVISIBLE);
        this.mForegroundText.setText(value);
        this.mBackgroundText.setText(""); //$NON-NLS-1$
        onTextChanged(value, 0, 0, 0);
        this.mForegroundText.requestFocus();
        this.mForegroundText.setSelection(value.length());
        int lines = this.mBackgroundText.getLineCount();
        this.mForegroundText.setLines(lines <= 0 ? 1 : lines);
    }

    /**
     * Method that returns the current text value.
     *
     * @return The current text value
     */
    public String getText() {
        return this.mForegroundText.getText().toString();
    }

    /**
     * Method that sets the listener for received text change events.
     *
     * @param onTextChangedListener The text changed listener
     */
    public void setOnTextChangedListener(OnTextChangedListener onTextChangedListener) {
        this.mOnTextChangedListener = onTextChangedListener;
    }

    /**
     * Method that returns a string for autocomplete.
     *
     * @return String The autocomplete string
     */
    public String getCompletionString() {
        return this.mCompletionString;
    }

    /**
     * Method that sets a string for autocomplete.
     *
     * @param completionString The autocomplete string
     */
    public void setCompletionString(String completionString) {
        this.mCompletionString = completionString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inline_autocomplete_button_tab:
                //Complete with current text
                String current = this.mForegroundText.getText().toString();
                String filter = this.mBackgroundText.getText().toString();
                if (current.length() == 0) {
                    return;
                }
                if (this.mCompletionString != null
                        && current.endsWith(this.mCompletionString)) {
                    if (this.mData.size() <= this.mFilter) {
                        this.mFilter = 0;
                    }
                    if (this.mData.size() == 1 && this.mFilter == 0) {
                        //Autocomplete with the only autocomplete option
                        setText(this.mData.get(this.mFilter));
                    } else {
                        //Show the autocomplete options
                        if (this.mData.size() > 0) {
                            this.mBackgroundText.setText(this.mData.get(this.mFilter));
                            this.mBackgroundText.setVisibility(View.VISIBLE);
                            this.mFilter++;
                        }
                    }
                } else {
                    //Autocomplete
                    if (filter != null && filter.length() > 0) {
                        //Ensure that filter wraps the current text
                        if (filter.startsWith(current)) {
                            setText(filter);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.inline_autocomplete_button_tab:
                //Complete with current suggestion
                String filter = this.mBackgroundText.getText().toString();
                if (this.mBackgroundText.getVisibility() == View.VISIBLE && filter.length() > 0) {
                    setText(filter);
                } else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            DialogHelper.showToast(
                                    getContext(),
                                    R.string.inline_autocomplete_tab_nothing_to_complete_msg,
                                    Toast.LENGTH_SHORT);
                        }
                    });
                }
                return true;

            default:
                break;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        /**NON BLOCK**/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.mFilter = 0;
        if (this.mOnTextChangedListener != null) {
            //Communicate the change
            this.mOnTextChangedListener.onTextChanged(s.toString(), this.mData);

            if (this.mCompletionString != null
                    && !s.toString().endsWith(this.mCompletionString)) {
                //Autocomplete
                autocomplete();
            } else {
                this.mBackgroundText.setVisibility(View.INVISIBLE);
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTextChanged(Editable s) {
        /**NON BLOCK**/
    }

    /**
     * Method that autocompletes the text, showing the best matches.
     */
    private void autocomplete() {
        final String currentText = getText();
        if (!this.mData.isEmpty()) {
            Iterator<String> it = this.mData.iterator();
            while (it.hasNext()) {
                String filterData = it.next();
                if (filterData.startsWith(currentText)) {
                    this.mBackgroundText.setText(filterData);
                    this.mBackgroundText.setVisibility(View.VISIBLE);
                    int lines = this.mBackgroundText.getLineCount();
                    this.mForegroundText.setLines(lines <= 0 ? 1 : lines);
                    return;
                }
            }
        }
        this.mBackgroundText.setVisibility(View.INVISIBLE);
    }

}
