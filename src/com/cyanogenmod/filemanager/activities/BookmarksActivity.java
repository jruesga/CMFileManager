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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.XmlUtils;
import com.cyanogenmod.filemanager.FileManagerApplication;
import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.adapters.BookmarksAdapter;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.Bookmark;
import com.cyanogenmod.filemanager.model.Bookmark.BOOKMARK_TYPE;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.preferences.AccessMode;
import com.cyanogenmod.filemanager.preferences.Bookmarks;
import com.cyanogenmod.filemanager.preferences.FileManagerSettings;
import com.cyanogenmod.filemanager.preferences.Preferences;
import com.cyanogenmod.filemanager.ui.ThemeManager;
import com.cyanogenmod.filemanager.ui.ThemeManager.Theme;
import com.cyanogenmod.filemanager.ui.dialogs.InitialDirectoryDialog;
import com.cyanogenmod.filemanager.ui.widgets.FlingerListView;
import com.cyanogenmod.filemanager.ui.widgets.FlingerListView.OnItemFlingerListener;
import com.cyanogenmod.filemanager.ui.widgets.FlingerListView.OnItemFlingerResponder;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.DialogHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;
import com.cyanogenmod.filemanager.util.StorageHelper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An activity for show bookmarks and links.
 */
public class BookmarksActivity extends Activity implements OnItemClickListener, OnClickListener {

    private static final String TAG = "BookmarksActivity"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    /**
     * A listener for flinging events from {@link FlingerListView}
     */
    private final OnItemFlingerListener mOnItemFlingerListener = new OnItemFlingerListener() {

        @Override
        public boolean onItemFlingerStart(
                AdapterView<?> parent, View view, int position, long id) {
            try {
                // Response if the item can be removed
                BookmarksAdapter adapter = (BookmarksAdapter)parent.getAdapter();
                Bookmark bookmark = adapter.getItem(position);
                if (bookmark != null &&
                    bookmark.mType.compareTo(BOOKMARK_TYPE.USER_DEFINED) == 0) {
                    return true;
                }
            } catch (Exception e) {
                ExceptionUtil.translateException(BookmarksActivity.this, e, true, false);
            }
            return false;
        }

        @Override
        public void onItemFlingerEnd(OnItemFlingerResponder responder,
                AdapterView<?> parent, View view, int position, long id) {

            try {
                // Response if the item can be removed
                BookmarksAdapter adapter = (BookmarksAdapter)parent.getAdapter();
                Bookmark bookmark = adapter.getItem(position);
                if (bookmark != null &&
                        bookmark.mType.compareTo(BOOKMARK_TYPE.USER_DEFINED) == 0) {
                    boolean result = Bookmarks.removeBookmark(BookmarksActivity.this, bookmark);
                    if (!result) {
                        //Show warning
                        DialogHelper.showToast(BookmarksActivity.this,
                                R.string.msgs_operation_failure, Toast.LENGTH_SHORT);
                        responder.cancel();
                        return;
                    }
                    responder.accept();
                    adapter.remove(bookmark);
                    return;
                }

                // Cancels the flinger operation
                responder.cancel();

            } catch (Exception e) {
                ExceptionUtil.translateException(BookmarksActivity.this, e, true, false);
                responder.cancel();
            }
        }
    };

    private final BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().compareTo(FileManagerSettings.INTENT_THEME_CHANGED) == 0) {
                    applyTheme();
                }
            }
        }
    };

    // Bookmark list XML tags
    private static final String TAG_BOOKMARKS = "Bookmarks"; //$NON-NLS-1$
    private static final String TAG_BOOKMARK = "bookmark"; //$NON-NLS-1$

    /**
     * @hide
     */
    ListView mBookmarksListView;

    private boolean mChRooted;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle state) {
        if (DEBUG) {
            Log.d(TAG, "BookmarksActivity.onCreate"); //$NON-NLS-1$
        }

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileManagerSettings.INTENT_THEME_CHANGED);
        registerReceiver(this.mNotificationReceiver, filter);

        // Is ChRooted?
        this.mChRooted = FileManagerApplication.getAccessMode().compareTo(AccessMode.SAFE) == 0;

        //Set in transition
        overridePendingTransition(R.anim.translate_to_right_in, R.anim.hold_out);

        //Set the main layout of the activity
        setContentView(R.layout.bookmarks);

        //Initialize action bars and data
        initTitleActionBar();
        initBookmarks();

        // Apply the theme
        applyTheme();

        //Save state
        super.onCreate(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(TAG, "BookmarksActivity.onDestroy"); //$NON-NLS-1$
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
    protected void onPause() {
        //Set out transition
        overridePendingTransition(R.anim.hold_in, R.anim.translate_to_left_out);
        super.onPause();
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
        TextView title = (TextView)customTitle.findViewById(R.id.customtitle_title);
        title.setText(R.string.bookmarks);
        title.setContentDescription(getString(R.string.bookmarks));
        getActionBar().setCustomView(customTitle);
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    private void initBookmarks() {
        this.mBookmarksListView = (ListView)findViewById(R.id.bookmarks_listview);
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        BookmarksAdapter adapter = new BookmarksAdapter(this, bookmarks, this);
        this.mBookmarksListView.setAdapter(adapter);
        this.mBookmarksListView.setOnItemClickListener(this);

        // If we should set the listview to response to flinger gesture detection
        boolean useFlinger =
                Preferences.getSharedPreferences().getBoolean(
                        FileManagerSettings.SETTINGS_USE_FLINGER.getId(),
                            ((Boolean)FileManagerSettings.
                                    SETTINGS_USE_FLINGER.
                                        getDefaultValue()).booleanValue());
        if (useFlinger) {
            ((FlingerListView)this.mBookmarksListView).
                setOnItemFlingerListener(this.mOnItemFlingerListener);
        }

        // Reload the data
        refresh();
    }

    /**
     * Method that makes the refresh of the data.
     */
    void refresh() {
        // Retrieve the loading view
        final View waiting = findViewById(R.id.bookmarks_waiting);
        final BookmarksAdapter adapter = (BookmarksAdapter)this.mBookmarksListView.getAdapter();

        // Load the history in background
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            Exception mCause;
            List<Bookmark> mBookmarks;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    this.mBookmarks = loadBookmarks();
                    return Boolean.TRUE;

                } catch (Exception e) {
                    this.mCause = e;
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPreExecute() {
                waiting.setVisibility(View.VISIBLE);
                adapter.clear();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                waiting.setVisibility(View.GONE);
                if (result.booleanValue()) {
                    adapter.addAll(this.mBookmarks);
                    BookmarksActivity.this.mBookmarksListView.setSelection(0);

                } else {
                    if (this.mCause != null) {
                        ExceptionUtil.translateException(BookmarksActivity.this, this.mCause);
                    }
                }
            }

            @Override
            protected void onCancelled() {
                waiting.setVisibility(View.GONE);
            }
        };
        task.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                back(true, null);
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
              back(true, null);
              return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bookmark bookmark = ((BookmarksAdapter)parent.getAdapter()).getItem(position);
        back(false, bookmark.mPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
      //Retrieve the position
      final int position = ((Integer)v.getTag()).intValue();
      final BookmarksAdapter adapter = (BookmarksAdapter)this.mBookmarksListView.getAdapter();
      final Bookmark bookmark = adapter.getItem(position);

      //Configure home
      if (bookmark.mType.compareTo(BOOKMARK_TYPE.HOME) == 0) {
          //Show a dialog for configure initial directory
          InitialDirectoryDialog dialog = new InitialDirectoryDialog(this);
          dialog.setOnValueChangedListener(new InitialDirectoryDialog.OnValueChangedListener() {
              @Override
              public void onValueChanged(String newInitialDir) {
                  adapter.getItem(position).mPath = newInitialDir;
                  adapter.notifyDataSetChanged();
              }
          });
          dialog.show();
          return;
      }

      //Remove bookmark
      if (bookmark.mType.compareTo(BOOKMARK_TYPE.USER_DEFINED) == 0) {
          boolean result = Bookmarks.removeBookmark(this, bookmark);
          if (!result) {
              //Show warning
              DialogHelper.showToast(this, R.string.msgs_operation_failure, Toast.LENGTH_SHORT);
              return;
          }
          adapter.remove(bookmark);
          return;
      }
    }

    /**
     * Method that returns to previous activity and.
     *
     * @param cancelled Indicates if the activity was cancelled
     * @param path The path of the selected bookmark
     */
    private void back(final boolean cancelled, final String path) {
        Intent intent =  new Intent();
        if (cancelled) {
            setResult(RESULT_CANCELED, intent);
        } else {
            // Check that the bookmark exists
            try {
                FileSystemObject fso = CommandHelper.getFileInfo(this, path, null);
                if (fso != null) {
                    intent.putExtra(NavigationActivity.EXTRA_BOOKMARK_SELECTION, fso);
                    setResult(RESULT_OK, intent);
                } else {
                    // The bookmark not exists, delete the user-defined bookmark
                    try {
                        Bookmark b = Bookmarks.getBookmark(getContentResolver(), path);
                        Bookmarks.removeBookmark(this, b);
                        refresh();
                    } catch (Exception ex) {/**NON BLOCK**/}
                }
            } catch (Exception e) {
                // Capture the exception
                ExceptionUtil.translateException(this, e);
                if (e instanceof NoSuchFileOrDirectory || e instanceof FileNotFoundException) {
                    // The bookmark not exists, delete the user-defined bookmark
                    try {
                        Bookmark b = Bookmarks.getBookmark(getContentResolver(), path);
                        Bookmarks.removeBookmark(this, b);
                        refresh();
                    } catch (Exception ex) {/**NON BLOCK**/}
                }
                return;
            }
        }
        finish();
    }

    /**
     * Method that loads all kind of bookmarks and join in
     * an array to be used in the listview adapter.
     *
     * @return List<Bookmark>
     * @hide
     */
    List<Bookmark> loadBookmarks() {
        // Bookmarks = HOME + FILESYSTEM + SD STORAGES + USER DEFINED
        // In ChRooted mode = SD STORAGES + USER DEFINED (from SD STORAGES)
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        if (!this.mChRooted) {
            bookmarks.add(loadHomeBookmarks());
            bookmarks.addAll(loadFilesystemBookmarks());
        }
        bookmarks.addAll(loadSdStorageBookmarks());
        bookmarks.addAll(loadUserBookmarks());
        return bookmarks;
    }

    /**
     * Method that loads the home bookmark from the user preference.
     *
     * @return Bookmark The bookmark loaded
     */
    private Bookmark loadHomeBookmarks() {
        String initialDir = Preferences.getSharedPreferences().getString(
                                FileManagerSettings.SETTINGS_INITIAL_DIR.getId(),
                                (String)FileManagerSettings.SETTINGS_INITIAL_DIR.getDefaultValue());
        return new Bookmark(BOOKMARK_TYPE.HOME, getString(R.string.bookmarks_home), initialDir);
    }

    /**
     * Method that loads the filesystem bookmarks from the internal xml file.
     * (defined by this application)
     *
     * @return List<Bookmark> The bookmarks loaded
     */
    private List<Bookmark> loadFilesystemBookmarks() {
        try {
            //Initialize the bookmarks
            List<Bookmark> bookmarks = new ArrayList<Bookmark>();

            //Read the command list xml file
            XmlResourceParser parser = getResources().getXml(R.xml.filesystem_bookmarks);

            try {
                //Find the root element
                XmlUtils.beginDocument(parser, TAG_BOOKMARKS);
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        break;
                    }

                    if (TAG_BOOKMARK.equals(element)) {
                        CharSequence name = null;
                        CharSequence directory = null;

                        try {
                            name =
                                    getString(parser.getAttributeResourceValue(
                                            R.styleable.Bookmark_name, 0));
                        } catch (Exception e) {/**NON BLOCK**/}
                        try {
                            directory =
                                    getString(parser.getAttributeResourceValue(
                                            R.styleable.Bookmark_directory, 0));
                        } catch (Exception e) {/**NON BLOCK**/}
                        if (directory == null) {
                            directory =
                                    parser.getAttributeValue(R.styleable.Bookmark_directory);
                        }
                        if (name != null && directory != null) {
                            bookmarks.add(
                                    new Bookmark(
                                            BOOKMARK_TYPE.FILESYSTEM,
                                            name.toString(),
                                            directory.toString()));
                        }
                    }
                }

                //Return the bookmarks
                return bookmarks;

            } finally {
                parser.close();
            }
        } catch (Throwable ex) {
            Log.e(TAG, "Load filesystem bookmarks failed", ex); //$NON-NLS-1$
        }

        //No data
        return new ArrayList<Bookmark>();
    }

    /**
     * Method that loads the secure digital card storage bookmarks from the system.
     *
     * @return List<Bookmark> The bookmarks loaded
     */
    private List<Bookmark> loadSdStorageBookmarks() {
        //Initialize the bookmarks
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();

        try {
            //Recovery sdcards from storage manager
            StorageVolume[] volumes = StorageHelper.getStorageVolumes(getApplication());
            int cc = volumes.length;
            for (int i = 0; i < cc ; i++) {
                if (volumes[i].getPath().toLowerCase(Locale.ROOT).indexOf("usb") != -1) { //$NON-NLS-1$
                    bookmarks.add(
                            new Bookmark(
                                    BOOKMARK_TYPE.USB,
                                    StorageHelper.getStorageVolumeDescription(
                                            getApplication(), volumes[i]),
                                    volumes[i].getPath()));
                } else {
                    bookmarks.add(
                            new Bookmark(
                                    BOOKMARK_TYPE.SDCARD,
                                    StorageHelper.getStorageVolumeDescription(
                                            getApplication(), volumes[i]),
                                    volumes[i].getPath()));
                }
            }

            //Return the bookmarks
            return bookmarks;
        } catch (Throwable ex) {
            Log.e(TAG, "Load filesystem bookmarks failed", ex); //$NON-NLS-1$
        }

        //No data
        return new ArrayList<Bookmark>();
    }

    /**
     * Method that loads the user bookmarks (added by the user).
     *
     * @return List<Bookmark> The bookmarks loaded
     */
    private List<Bookmark> loadUserBookmarks() {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        Cursor cursor = Bookmarks.getAllBookmarks(this.getContentResolver());
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Bookmark bm = new Bookmark(cursor);
                    if (this.mChRooted && !StorageHelper.isPathInStorageVolume(bm.mPath)) {
                        continue;
                    }
                    bookmarks.add(bm);
                } while (cursor.moveToNext());
            }
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {/**NON BLOCK**/}
        }
        return bookmarks;
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
        // -View
        theme.setBackgroundDrawable(
                this, this.mBookmarksListView, "background_drawable"); //$NON-NLS-1$
        if (((BookmarksAdapter)this.mBookmarksListView.getAdapter()) != null) {
            ((BookmarksAdapter)this.mBookmarksListView.getAdapter()).notifyThemeChanged();
            ((BookmarksAdapter)this.mBookmarksListView.getAdapter()).notifyDataSetChanged();
        }
        this.mBookmarksListView.setDivider(
                theme.getDrawable(this, "horizontal_divider_drawable")); //$NON-NLS-1$
        this.mBookmarksListView.invalidate();
    }
}
