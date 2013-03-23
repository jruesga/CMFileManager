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

package com.cyanogenmod.filemanager.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.android.internal.util.HexDump;
import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.activities.preferences.SettingsPreferences;
import com.cyanogenmod.filemanager.activities.preferences.SettingsPreferences.EditorPreferenceFragment;
import com.cyanogenmod.filemanager.adapters.HighlightedSimpleMenuListAdapter;
import com.cyanogenmod.filemanager.adapters.SimpleMenuListAdapter;
import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.commands.WriteExecutable;
import com.cyanogenmod.filemanager.console.ConsoleBuilder;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.preferences.FileManagerSettings;
import com.cyanogenmod.filemanager.preferences.Preferences;
import com.cyanogenmod.filemanager.ui.ThemeManager;
import com.cyanogenmod.filemanager.ui.ThemeManager.Theme;
import com.cyanogenmod.filemanager.ui.widgets.ButtonItem;
import com.cyanogenmod.filemanager.util.AndroidHelper;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.DialogHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;
import com.cyanogenmod.filemanager.util.ExceptionUtil.OnRelaunchCommandResult;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * An internal activity for view and edit files.
 */
public class EditorActivity extends Activity implements TextWatcher {

    private static final String TAG = "EditorActivity"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    private final BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().compareTo(FileManagerSettings.INTENT_THEME_CHANGED) == 0) {
                    applyTheme();
                    return;
                }
                if (intent.getAction().compareTo(FileManagerSettings.INTENT_SETTING_CHANGED) == 0) {
                    // The settings has changed
                    String key = intent.getStringExtra(FileManagerSettings.EXTRA_SETTING_CHANGED_KEY);
                    if (key != null) {
                        // Word wrap
                        if (key.compareTo(FileManagerSettings.
                                SETTINGS_EDITOR_WORD_WRAP.getId()) == 0) {
                            // Do we have a different setting?
                            boolean wordWrapSetting = Preferences.getSharedPreferences().getBoolean(
                                    FileManagerSettings.SETTINGS_EDITOR_WORD_WRAP.getId(),
                                    ((Boolean)FileManagerSettings.SETTINGS_EDITOR_WORD_WRAP.
                                            getDefaultValue()).booleanValue());
                            if (wordWrapSetting != EditorActivity.this.mWordWrap) {
                                toggleWordWrap();
                            }
                        }
                    }
                    return;
                }
            }
        }
    };

    /**
     * Internal interface to notify progress update
     */
    private interface OnProgressListener {
        void onProgress(int progress);
    }

    /**
     * An internal listener for read a file
     */
    @SuppressWarnings("hiding")
    private class AsyncReader implements AsyncResultListener {

        final Object mSync = new Object();
        ByteArrayOutputStream mByteBuffer = null;
        StringBuilder mBuffer = null;
        Exception mCause;
        long mSize;
        FileSystemObject mFso;
        OnProgressListener mListener;

        /**
         * Constructor of <code>AsyncReader</code>. For enclosing access.
         */
        public AsyncReader() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAsyncStart() {
            this.mByteBuffer = new ByteArrayOutputStream((int)this.mFso.getSize());
            this.mSize = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAsyncEnd(boolean cancelled) {/**NON BLOCK**/}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAsyncExitCode(int exitCode) {
            synchronized (this.mSync) {
                this.mSync.notify();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPartialResult(Object result) {
            try {
                if (result == null) return;
                byte[] partial = (byte[])result;

                // Check if the file is a binary file. In this case the editor
                // is read-only
                if (!EditorActivity.this.mReadOnly) {
                    for (int i = 0; i < partial.length-1; i++) {
                        if (!isPrintableCharacter((char)partial[i])) {
                            EditorActivity.this.mBinary = true;
                            EditorActivity.this.mReadOnly = true;
                            break;
                        }
                    }
                }

                this.mByteBuffer.write(partial, 0, partial.length);
                this.mSize += partial.length;
                if (this.mListener != null && this.mFso != null) {
                    int progress = 0;
                    if (this.mFso.getSize() != 0) {
                        progress = (int)((this.mSize*100) / this.mFso.getSize());
                    }
                    this.mListener.onProgress(progress);
                }
            } catch (Exception e) {
                this.mCause = e;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onException(Exception cause) {
            this.mCause = cause;
        }
    }

    /**
     * An internal listener for write a file
     */
    private class AsyncWriter implements AsyncResultListener {

        Exception mCause;

        /**
         * Constructor of <code>AsyncWriter</code>. For enclosing access.
         */
        public AsyncWriter() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAsyncStart() {/**NON BLOCK**/}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAsyncEnd(boolean cancelled) {/**NON BLOCK**/}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAsyncExitCode(int exitCode) {/**NON BLOCK**/}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPartialResult(Object result) {/**NON BLOCK**/}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onException(Exception cause) {
            this.mCause = cause;
        }
    }

    /**
     * @hide
     */
    FileSystemObject mFso;

    private int mBufferSize;
    private int mMaxFileSize;

    /**
     * @hide
     */
    boolean mDirty;
    /**
     * @hide
     */
    boolean mReadOnly;
    /**
     * @hide
     */
    boolean mBinary;

    /**
     * @hide
     */
    TextView mTitle;
    /**
     * @hide
     */
    EditText mEditor;
    /**
     * @hide
     */
    View mProgress;
    /**
     * @hide
     */
    ProgressBar mProgressBar;
    /**
     * @hide
     */
    TextView mProgressBarMsg;
    /**
     * @hide
     */
    ButtonItem mSave;

    // Word wrap status
    private ViewGroup mWordWrapView;
    private ViewGroup mNoWordWrapView;
    /**
     * @hide
     */
    boolean mWordWrap;

    private View mOptionsAnchorView;

    private static final char[] VALID_NON_PRINTABLE_CHARS = {' ', '\t', '\r', '\n'};

    /**
     * @hide
     */
    String mHexLineSeparator;

    /**
     * Intent extra parameter for the path of the file to open.
     */
    public static final String EXTRA_OPEN_FILE = "extra_open_file";  //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle state) {
        if (DEBUG) {
            Log.d(TAG, "EditorActivity.onCreate"); //$NON-NLS-1$
        }

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileManagerSettings.INTENT_THEME_CHANGED);
        filter.addAction(FileManagerSettings.INTENT_SETTING_CHANGED);
        registerReceiver(this.mNotificationReceiver, filter);

        // Generate a random separator
        this.mHexLineSeparator = UUID.randomUUID().toString();

        //Set the main layout of the activity
        setContentView(R.layout.editor);

        // Get the limit vars
        this.mBufferSize =
                getApplicationContext().getResources().getInteger(R.integer.buffer_size);
        this.mMaxFileSize =
                getApplicationContext().getResources().getInteger(R.integer.editor_max_file_size);

        //Initialize
        initTitleActionBar();
        initLayout();

        // Apply the theme
        applyTheme();

        // Initialize the console
        initializeConsole();

        // Read the file
        readFile();

        //Save state
        super.onCreate(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(TAG, "EditorActivity.onDestroy"); //$NON-NLS-1$
        }

        // Unregister the receiver
        try {
            unregisterReceiver(this.mNotificationReceiver);
        } catch (Throwable ex) {
            /**NON BLOCK**/
        }

        //All destroy. Continue
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    private void initTitleActionBar() {
        //Configure the action bar options
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.bg_holo_titlebar));
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        View customTitle = getLayoutInflater().inflate(R.layout.simple_customtitle, null, false);
        this.mTitle = (TextView)customTitle.findViewById(R.id.customtitle_title);
        this.mTitle.setText(R.string.editor);
        this.mTitle.setContentDescription(getString(R.string.editor));
        this.mSave = (ButtonItem)customTitle.findViewById(R.id.ab_button1);
        this.mSave.setImageResource(R.drawable.ic_holo_light_save);
        this.mSave.setContentDescription(getString(R.string.actionbar_button_save_cd));
        this.mSave.setVisibility(View.GONE);

        ButtonItem configuration = (ButtonItem)customTitle.findViewById(R.id.ab_button2);
        configuration.setImageResource(R.drawable.ic_holo_light_overflow);
        configuration.setContentDescription(getString(R.string.actionbar_button_overflow_cd));

        View status = findViewById(R.id.editor_status);
        boolean showOptionsMenu = AndroidHelper.showOptionsMenu(getApplicationContext());
        configuration.setVisibility(showOptionsMenu ? View.VISIBLE : View.GONE);
        this.mOptionsAnchorView = showOptionsMenu ? configuration : status;

        getActionBar().setCustomView(customTitle);
    }

    /**
     * Method that initializes the layout and components of the activity.
     */
    private void initLayout() {
        this.mEditor = (EditText)findViewById(R.id.editor);
        this.mEditor.setText(null);
        this.mEditor.addTextChangedListener(this);
        this.mEditor.setEnabled(false);
        this.mWordWrapView = (ViewGroup)findViewById(R.id.editor_word_wrap_view);
        this.mNoWordWrapView = (ViewGroup)findViewById(R.id.editor_no_word_wrap_view);
        this.mWordWrap = true;
        this.mWordWrapView.setVisibility(View.VISIBLE);
        this.mNoWordWrapView.setVisibility(View.GONE);

        // Load the word wrap setting
        boolean wordWrapSetting = Preferences.getSharedPreferences().getBoolean(
                FileManagerSettings.SETTINGS_EDITOR_WORD_WRAP.getId(),
                ((Boolean)FileManagerSettings.SETTINGS_EDITOR_WORD_WRAP.
                        getDefaultValue()).booleanValue());
        if (wordWrapSetting != this.mWordWrap) {
            toggleWordWrap();
        }

        this.mProgress = findViewById(R.id.editor_progress);
        this.mProgressBar = (ProgressBar)findViewById(R.id.editor_progress_bar);
        this.mProgressBarMsg = (TextView)findViewById(R.id.editor_progress_msg);
    }

    /**
     * Method that toggle the word wrap property of the editor
     * @hide
     */
    /**package**/ void toggleWordWrap() {
        ViewGroup vSrc = this.mWordWrap ? this.mWordWrapView : this.mNoWordWrapView;
        ViewGroup vDst = this.mWordWrap ? this.mNoWordWrapView : this.mWordWrapView;
        ViewGroup vSrcParent = this.mWordWrap
                                            ? this.mWordWrapView
                                            : (ViewGroup)this.mNoWordWrapView.getChildAt(0);
        ViewGroup vDstParent = this.mWordWrap
                                            ? (ViewGroup)this.mNoWordWrapView.getChildAt(0)
                                            : this.mWordWrapView;
        vSrc.setVisibility(View.GONE);
        vSrcParent.removeView(this.mEditor);
        vDstParent.addView(this.mEditor);
        vDst.setVisibility(View.VISIBLE);
        vDst.scrollTo(0, 0);
        this.mWordWrap = !this.mWordWrap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                showOverflowPopUp(this.mOptionsAnchorView);
                return true;
            case KeyEvent.KEYCODE_BACK:
                checkDirtyState();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case android.R.id.home:
              if ((getActionBar().getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP)
                      == ActionBar.DISPLAY_HOME_AS_UP) {
                  checkDirtyState();
              }
              return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }

    /**
     * Method that shows a popup with the activity main menu.
     *
     * @param anchor The anchor of the popup
     */
    private void showOverflowPopUp(View anchor) {
        SimpleMenuListAdapter adapter =
                new HighlightedSimpleMenuListAdapter(this, R.menu.editor);
        MenuItem wordWrap = adapter.getMenu().findItem(R.id.mnu_word_wrap);
        if (wordWrap != null) {
            if (this.mBinary) {
                adapter.getMenu().removeItem(R.id.mnu_word_wrap);
            } else {
                wordWrap.setChecked(this.mWordWrap);
            }
        }

        final ListPopupWindow popup =
                DialogHelper.createListPopupWindow(this, adapter, anchor);
        popup.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(
                    final AdapterView<?> parent, final View v,
                    final int position, final long id) {
                final int itemId = (int)id;
                switch (itemId) {
                    case R.id.mnu_word_wrap:
                        popup.dismiss();
                        toggleWordWrap();
                        break;
                    case R.id.mnu_settings:
                        //Settings
                        Intent settings = new Intent(EditorActivity.this, SettingsPreferences.class);
                        settings.putExtra(
                                PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                                EditorPreferenceFragment.class.getName());
                        startActivity(settings);
                        break;
                }
                popup.dismiss();
            }
        });
        popup.show();
    }

    /**
     * Method invoked when an action item is clicked.
     *
     * @param view The button pushed
     */
    public void onActionBarItemClick(View view) {
        switch (view.getId()) {
            case R.id.ab_button1:
                // Save the file
                checkAndWrite();
                break;

            case R.id.ab_button2:
                // Show overflow menu
                showOverflowPopUp(this.mOptionsAnchorView);
                break;

            default:
                break;
        }
    }

    /**
     * Method that initializes a console
     */
    private boolean initializeConsole() {
        try {
            ConsoleBuilder.getConsole(this);
            // There is a console allocated. Use it.
            return true;
        } catch (Throwable _throw) {
            // Capture the exception
            ExceptionUtil.translateException(this, _throw, false, true);
        }
        return false;
    }

    /**
     * Method that reads the requested file
     */
    private void readFile() {
        // For now editor is not dirty and editable.
        setDirty(false);
        this.mBinary = false;

        // Check for a valid action
        String action = getIntent().getAction();
        if (action == null ||
                (action.compareTo(Intent.ACTION_VIEW) != 0) &&
                (action.compareTo(Intent.ACTION_EDIT) != 0)) {
            DialogHelper.showToast(
                    this, R.string.editor_invalid_file_msg, Toast.LENGTH_SHORT);
            return;
        }
        // This var should be set depending on ACTION_VIEW or ACTION_EDIT action, but for
        // better compatibility, IntentsActionPolicy use always ACTION_VIEW, so we have
        // to ignore this check here
        this.mReadOnly = false;

        // Read the intent and check that is has a valid request
        String path = getIntent().getData().getPath();
        if (path == null || path.length() == 0) {
            DialogHelper.showToast(
                    this, R.string.editor_invalid_file_msg, Toast.LENGTH_SHORT);
            return;
        }

        // Set the title of the dialog
        File f = new File(path);
        this.mTitle.setText(f.getName());

        // Check that the file exists (the real file, not the symlink)
        try {
            this.mFso = CommandHelper.getFileInfo(this, path, true, null);
            if (this.mFso == null) {
                DialogHelper.showToast(
                        this, R.string.editor_file_not_found_msg, Toast.LENGTH_SHORT);
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get file reference", e); //$NON-NLS-1$
            DialogHelper.showToast(
                    this, R.string.editor_file_not_found_msg, Toast.LENGTH_SHORT);
            return;
        }

        // Check that we can handle the length of the file (by device)
        if (this.mMaxFileSize < this.mFso.getSize()) {
            DialogHelper.showToast(
                    this, R.string.editor_file_exceed_size_msg, Toast.LENGTH_SHORT);
            return;
        }

        // Check that we have read access
        try {
            FileHelper.ensureReadAccess(
                    ConsoleBuilder.getConsole(this),
                    this.mFso,
                    null);

            // Read the file in background
            asyncRead();

        } catch (Exception ex) {
            ExceptionUtil.translateException(
                    this, ex, false, true, new OnRelaunchCommandResult() {
                @Override
                public void onSuccess() {
                    // Read the file in background
                    asyncRead();
                }

                @Override
                public void onFailed(Throwable cause) {
                    finish();
                }

                @Override
                public void onCancelled() {
                    finish();
                }
            });
        }
    }

    /**
     * Method that does the read of the file in background
     * @hide
     */
    void asyncRead() {
        // Do the load of the file
        AsyncTask<FileSystemObject, Integer, Boolean> mReadTask =
                            new AsyncTask<FileSystemObject, Integer, Boolean>() {

            private Exception mCause;
            private AsyncReader mReader;
            private boolean changeToBinaryMode;
            private boolean changeToDisplaying;

            @Override
            protected void onPreExecute() {
                // Show the progress
                this.changeToBinaryMode = false;
                this.changeToDisplaying = false;
                doProgress(true, 0);
            }

            @Override
            protected Boolean doInBackground(FileSystemObject... params) {
                // Only one argument (the file to open)
                FileSystemObject fso = params[0];
                this.mCause = null;

                // Read the file in an async listener
                try {
                    while (true) {
                        // Configure the reader
                        this.mReader = new AsyncReader();
                        this.mReader.mFso = fso;
                        this.mReader.mListener = new OnProgressListener() {
                            @Override
                            @SuppressWarnings("synthetic-access")
                            public void onProgress(int progress) {
                                publishProgress(Integer.valueOf(progress));
                            }
                        };

                        // Execute the command (read the file)
                        CommandHelper.read(
                                EditorActivity.this, fso.getFullPath(), this.mReader, null);

                        // Wait for
                        synchronized (this.mReader.mSync) {
                            this.mReader.mSync.wait();
                        }

                        // 100%
                        publishProgress(new Integer(100));

                        // Check if the read was successfully
                        if (this.mReader.mCause != null) {
                            this.mCause = this.mReader.mCause;
                            return Boolean.FALSE;
                        }
                        break;
                    }

                    // Now we have the byte array with all the data. is a binary file?
                    // Then dump them byte array to hex dump string
                    // Don't use the Hexdump helper class, so we can show the progress of
                    // the dump process
                    if (EditorActivity.this.mBinary) {
                        this.mReader.mBuffer =
                                new StringBuilder(
                                        toHexPrintableString(
                                                toHexDump(
                                                        this.mReader.mByteBuffer.toByteArray())));
                    } else {
                        this.mReader.mBuffer =
                                new StringBuilder(
                                        new String(this.mReader.mByteBuffer.toByteArray()));
                    }
                    this.mReader.mByteBuffer = null;

                    // 100% - We need two calls here to proper display the message
                    this.changeToDisplaying = true;
                    publishProgress(new Integer(0));
                    publishProgress(new Integer(0));

                } catch (Exception e) {
                    this.mCause = e;
                    return Boolean.FALSE;
                }

                return Boolean.TRUE;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                // Do progress
                doProgress(true, values[0].intValue());
            }

            @Override
            protected void onPostExecute(Boolean result) {
                // Is error?
                if (!result.booleanValue()) {
                    if (this.mCause != null) {
                        ExceptionUtil.translateException(EditorActivity.this, this.mCause);
                        EditorActivity.this.mEditor.setEnabled(false);
                    }
                } else {
                    // Now we have the buffer, set the text of the editor
                    if (EditorActivity.this.mBinary) {
                        EditorActivity.this.mEditor.setText(
                                this.mReader.mBuffer, BufferType.NORMAL);
                    } else {
                        EditorActivity.this.mEditor.setText(
                                this.mReader.mBuffer, BufferType.EDITABLE);
                    }
                    this.mReader.mBuffer = null; //Cleanup
                    setDirty(false);
                    EditorActivity.this.mEditor.setEnabled(!EditorActivity.this.mReadOnly);

                    // Notify read-only mode
                    if (EditorActivity.this.mReadOnly) {
                        DialogHelper.showToast(
                                EditorActivity.this,
                                R.string.editor_read_only_mode,
                                Toast.LENGTH_SHORT);
                    }
                }

                doProgress(false, 0);
            }

            @Override
            protected void onCancelled() {
                // Hide the progress
                doProgress(false, 0);
            }

            /**
             * Method that update the progress status
             *
             * @param visible If the progress bar need to be hidden
             * @param progress The progress
             */
            private void doProgress(boolean visible, int progress) {
                // Show the progress bar
                EditorActivity.this.mProgressBar.setProgress(progress);
                EditorActivity.this.mProgress.setVisibility(
                            visible ? View.VISIBLE : View.GONE);

                if (this.changeToBinaryMode) {
                    // Hexdump always in nowrap mode
                    if (EditorActivity.this.mWordWrap) {
                        EditorActivity.this.toggleWordWrap();
                    }

                    // Show hex dumping text
                    EditorActivity.this.mProgressBarMsg.setText(R.string.dumping_message);
                    EditorActivity.this.mEditor.setTextAppearance(
                            EditorActivity.this, R.style.hexeditor_text_appearance);
                    EditorActivity.this.mEditor.setTypeface(Typeface.MONOSPACE);
                    this.changeToBinaryMode = false;
                }
                else if (this.changeToDisplaying) {
                    EditorActivity.this.mProgressBarMsg.setText(R.string.displaying_message);
                    this.changeToDisplaying = false;
                }
            }

            /**
             * Create a hex dump of the data while show progress to user
             *
             * @param data The data to hex dump
             * @return StringBuilder The hex dump buffer
             */
            private String toHexDump(byte[] data) {
                //Change to binary mode
                this.changeToBinaryMode = true;

                // Start progress
                publishProgress(Integer.valueOf(0));

                // Calculate max dir size
                int length = data.length;

                final int DISPLAY_SIZE = 16;  // Bytes per line
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                byte[] line = new byte[DISPLAY_SIZE];
                int read = 0;
                int offset = 0;
                StringBuilder sb = new StringBuilder();
                while ((read = bais.read(line, 0, DISPLAY_SIZE)) != -1) {
                    //offset   dump(16)   data\n
                    String linedata = new String(line, 0, read);
                    sb.append(HexDump.toHexString(offset));
                    sb.append("   "); //$NON-NLS-1$
                    String hexDump = HexDump.toHexString(line, 0, read);
                    if (hexDump.length() != (DISPLAY_SIZE * 2)) {
                        char[] array = new char[(DISPLAY_SIZE * 2) - hexDump.length()];
                        Arrays.fill(array, ' ');
                        hexDump += new String(array);
                    }
                    sb.append(hexDump);
                    sb.append("   "); //$NON-NLS-1$
                    sb.append(linedata);
                    sb.append(EditorActivity.this.mHexLineSeparator);
                    offset += DISPLAY_SIZE;
                    if (offset % 5 == 0) {
                        publishProgress(Integer.valueOf((offset * 100) / length));
                    }
                }

                // End of the dump process
                publishProgress(Integer.valueOf(100));

                return sb.toString();
            }

            /**
             * Method that converts to a visual printable hex string
             *
             * @param string The string to check
             */
            private String toHexPrintableString(String string) {
                // Remove characters without visual representation
                final String REPLACED_SYMBOL = "."; //$NON-NLS-1$
                final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
                String printable = string.replaceAll("\\p{Cntrl}", REPLACED_SYMBOL); //$NON-NLS-1$
                printable = printable.replaceAll("[^\\p{Print}]", REPLACED_SYMBOL); //$NON-NLS-1$
                printable = printable.replaceAll("\\p{C}", REPLACED_SYMBOL); //$NON-NLS-1$
                printable = printable.replaceAll(EditorActivity.this.mHexLineSeparator, NEWLINE);
                return printable;
            }
        };
        mReadTask.execute(this.mFso);
    }

    private void checkAndWrite() {
        // Check that we have write access
        try {
            FileHelper.ensureWriteAccess(
                    ConsoleBuilder.getConsole(this),
                    this.mFso,
                    null);

            // Write the file
            syncWrite();

        } catch (Exception ex) {
            ExceptionUtil.translateException(
                    this, ex, false, true, new OnRelaunchCommandResult() {
                @Override
                public void onSuccess() {
                    // Write the file
                    syncWrite();
                }

                @Override
                public void onFailed(Throwable cause) {/**NON BLOCK**/}

                @Override
                public void onCancelled() {/**NON BLOCK**/}
            });
        }
    }

    /**
     * Method that write the file.
     * @hide
     */
    void syncWrite() {
        try {
            // Configure the writer
            AsyncWriter writer = new AsyncWriter();

            // Create the writable command
            WriteExecutable cmd =
                    CommandHelper.write(this, this.mFso.getFullPath(), writer, null);

            // Obtain access to the buffer (IMP! don't close the buffer here, it's manage
            // by the command)
            OutputStream os = cmd.createOutputStream();
            try {
                // Retrieve the text from the editor
                String text = this.mEditor.getText().toString();
                ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());
                text = null;
                try {
                    // Buffered write
                    byte[] data = new byte[this.mBufferSize];
                    int read = 0;
                    while ((read = bais.read(data, 0, this.mBufferSize)) != -1) {
                        os.write(data, 0, read);
                    }
                } finally {
                    try {
                        bais.close();
                    } catch (Exception e) {/**NON BLOCK**/}
                }

            } finally {
                // Ok. Data is written or ensure buffer close
                cmd.end();
            }

            // Sleep a bit
            Thread.sleep(150L);

            // Is error?
            if (writer.mCause != null) {
                // Something was wrong. The file probably is corrupted
                DialogHelper.showToast(
                        this, R.string.msgs_operation_failure, Toast.LENGTH_SHORT);
            } else {
                // Success. The file was saved
                DialogHelper.showToast(
                        this, R.string.editor_successfully_saved, Toast.LENGTH_SHORT);
                setDirty(false);

                // Send a message that allow other activities to update his data
                Intent intent = new Intent(FileManagerSettings.INTENT_FILE_CHANGED);
                intent.putExtra(
                        FileManagerSettings.EXTRA_FILE_CHANGED_KEY, this.mFso.getFullPath());
                sendBroadcast(intent);
            }

        } catch (Exception e) {
            // Something was wrong, but the file was NOT written
            DialogHelper.showToast(
                    this, R.string.msgs_operation_failure, Toast.LENGTH_SHORT);
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeTextChanged(
            CharSequence s, int start, int count, int after) {/**NON BLOCK**/}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {/**NON BLOCK**/}

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTextChanged(Editable s) {
        setDirty(true);
    }

    /**
     * Method that sets if the editor is dirty (has changed)
     *
     * @param dirty If the editor is dirty
     * @hide
     */
    void setDirty(boolean dirty) {
        this.mDirty = dirty;
        this.mSave.setVisibility(dirty ? View.VISIBLE : View.GONE);
    }

    /**
     * Check the dirty state of the editor, and ask the user to save the changes
     * prior to exit.
     */
    public void checkDirtyState() {
        if (this.mDirty) {
            AlertDialog dlg = DialogHelper.createYesNoDialog(
                    this,
                    R.string.editor_dirty_ask_title,
                    R.string.editor_dirty_ask_msg,
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                dialog.dismiss();
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        }
                    });
            DialogHelper.delegateDialogShow(this, dlg);
            return;
        }
        setResult(Activity.RESULT_OK);
        finish();
    }

    /**
     * Method that check if a character is valid printable character
     *
     * @param c The character to check
     * @return boolean If the character is printable
     * @hide
     */
    static boolean isPrintableCharacter(char c) {
        int cc = VALID_NON_PRINTABLE_CHARS.length;
        for (int i = 0; i < cc; i++) {
            if (c == VALID_NON_PRINTABLE_CHARS[i]) {
                return true;
            }
        }
        return TextUtils.isGraphic(c);
    }

    /**
     * Method that applies the current theme to the activity
     * @hide
     */
    void applyTheme() {
        Theme theme = ThemeManager.getCurrentTheme(this);
        theme.setBaseTheme(this, false);

        //- ActionBar
        theme.setTitlebarDrawable(this, getActionBar(), "titlebar_drawable"); //$NON-NLS-1$
        View v = getActionBar().getCustomView().findViewById(R.id.customtitle_title);
        theme.setTextColor(this, (TextView)v, "text_color"); //$NON-NLS-1$
        v = findViewById(R.id.ab_button1);
        theme.setImageDrawable(this, (ImageView)v, "ab_save_drawable"); //$NON-NLS-1$
        //- View
        v = findViewById(R.id.editor_layout);
        theme.setBackgroundDrawable(this, v, "background_drawable"); //$NON-NLS-1$
        v = findViewById(R.id.editor);
        theme.setTextColor(this, (TextView)v, "text_color"); //$NON-NLS-1$
        //- ProgressBar
        Drawable dw = theme.getDrawable(this, "horizontal_progress_bar"); //$NON-NLS-1$
        this.mProgressBar.setProgressDrawable(dw);
    }

}
