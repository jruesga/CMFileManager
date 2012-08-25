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

package com.cyanogenmod.explorer.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.model.Bookmark;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.ui.widgets.DirectoryInlineAutocompleteTextView;
import com.cyanogenmod.explorer.util.DialogHelper;

import java.io.File;
import java.util.List;

/**
 * A class that wraps a dialog for showing list of consoles for choosing one.
 * This class lets the user to set the default console.
 */
public class InitialDirectoryDialog implements DialogInterface.OnClickListener {

    /**
     * An interface to communicate events for value changing.
     */
    public interface OnValueChangedListener {
        /**
         * Method invoked when the value of the initial directory was changed.
         *
         * @param newInitialDir The new initial directory
         */
        void onValueChanged(String newInitialDir);
    }

    private static final String TAG = "InitialDirectoryDialog"; //$NON-NLS-1$

    private final AlertDialog mDialog;
    private final DirectoryInlineAutocompleteTextView mAutocomplete;
    private OnValueChangedListener mOnValueChangedListener;

    /**
     * Constructor of <code>InitialDirectoryDialog</code>.
     *
     * @param context The current context
     * @param bookmarks The current bookmarks
     */
    public InitialDirectoryDialog(Context context, final List<Bookmark> bookmarks) {
        super();

        //Extract current value
        String value = Preferences.getSharedPreferences().getString(
                ExplorerSettings.SETTINGS_INITIAL_DIR.getId(),
                (String)ExplorerSettings.SETTINGS_INITIAL_DIR.getDefaultValue());

        //Create the layout
        LayoutInflater li =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout)li.inflate(R.layout.initial_directory, null);
        final View msgView = layout.findViewById(R.id.initial_directory_info_msg);
        this.mAutocomplete =
                (DirectoryInlineAutocompleteTextView)layout.findViewById(
                        R.id.initial_directory_edittext);
        this.mAutocomplete.setOnValidationListener(
                new DirectoryInlineAutocompleteTextView.OnValidationListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void onVoidValue() {
                msgView.setVisibility(View.GONE);
                //The first invocation is valid. Can be ignore safely
                if (InitialDirectoryDialog.this.mDialog != null) {
                    InitialDirectoryDialog.this.mDialog.getButton(
                            DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            @SuppressWarnings("synthetic-access")
            public void onValidValue() {
                msgView.setVisibility(View.GONE);
                //The first invocation is valid. Can be ignore safely
                if (InitialDirectoryDialog.this.mDialog != null) {
                    InitialDirectoryDialog.this.mDialog.getButton(
                            DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                }
            }
            @Override
            @SuppressWarnings("synthetic-access")
            public void onInvalidValue() {
                msgView.setVisibility(View.VISIBLE);
                //The first invocation is valid. Can be ignore safely
                if (InitialDirectoryDialog.this.mDialog != null) {
                    InitialDirectoryDialog.this.mDialog.getButton(
                            DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        this.mAutocomplete.setText(value);

        //Create the dialog
        this.mDialog = DialogHelper.createDialog(
                                        context,
                                        R.drawable.ic_holo_light_home,
                                        R.string.initial_directory_dialog_title,
                                        layout);
        this.mDialog.setButton(
                DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
        this.mDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);
    }

    /**
     * Method that set the listener for retrieve value changed events.
     *
     * @param onValueChangedListener The listener for retrieve value changed events
     */
    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.mOnValueChangedListener = onValueChangedListener;
    }

    /**
     * Method that shows the dialog.
     */
    public void show() {
        this.mDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //Check that the directory is a valid directory
                String newInitialDir = this.mAutocomplete.getText().toString();
                try {
                    if (!newInitialDir.endsWith(File.separator)) {
                        newInitialDir += File.separator;
                    }
                    Preferences.savePreference(
                            ExplorerSettings.SETTINGS_INITIAL_DIR, newInitialDir, true);
                    if (this.mOnValueChangedListener != null) {
                        this.mOnValueChangedListener.onValueChanged(newInitialDir);
                    }
                } catch (Throwable ex) {
                    Log.e(TAG, "Saves initial directory setting fail", ex); //$NON-NLS-1$
                    DialogHelper.showToast(
                            this.mDialog.getContext(),
                            R.string.initial_directory_error_msg,
                            Toast.LENGTH_LONG);
                }
                break;

            default:
                break;
        }
    }
}
