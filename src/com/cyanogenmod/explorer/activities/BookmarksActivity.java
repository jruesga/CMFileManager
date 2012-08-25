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

package com.cyanogenmod.explorer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
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
import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.adapters.BookmarksAdapter;
import com.cyanogenmod.explorer.model.Bookmark;
import com.cyanogenmod.explorer.model.Bookmark.BOOKMARK_TYPE;
import com.cyanogenmod.explorer.preferences.BookmarksDatabase;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.ui.dialogs.InitialDirectoryDialog;
import com.cyanogenmod.explorer.util.DialogHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity for show bookmarks and links.
 */
public class BookmarksActivity extends Activity implements OnItemClickListener, OnClickListener {

    private static final String TAG = "BookmarksActivity"; //$NON-NLS-1$

    // Bookmark list XML tags
    private static final String TAG_BOOKMARKS = "Bookmarks"; //$NON-NLS-1$
    private static final String TAG_BOOKMARK = "bookmark"; //$NON-NLS-1$

    private ListView mBookmarksListView;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle state) {
        if (ExplorerApplication.DEBUG) {
            Log.d(TAG, "NavigationActivity.onCreate"); //$NON-NLS-1$
        }

        //Request features
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Set in transition
        overridePendingTransition(R.anim.translate_to_right_in, R.anim.hold_out);

        //Set the main layout of the activity
        setContentView(R.layout.bookmarks);

        //Initialize action bars and data
        initTitleActionBar();
        initBookmarks();

        //Save state
        super.onCreate(state);
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
        BookmarksAdapter adapter = new BookmarksAdapter(this, loadBookmarks(), this);
        this.mBookmarksListView.setAdapter(adapter);
        this.mBookmarksListView.setOnItemClickListener(this);
    }

    /**
     * Method that makes the refresh of the data.
     */
    private void refresh() {
        BookmarksAdapter adapter = (BookmarksAdapter)this.mBookmarksListView.getAdapter();
        adapter.clear();
        adapter.addAll(loadBookmarks());
        adapter.notifyDataSetChanged();
        this.mBookmarksListView.setSelection(0);
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
        back(false, bookmark.getDirectory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
      //Retrieve the position
      int position = ((Integer)v.getTag()).intValue();
      BookmarksAdapter adapter = (BookmarksAdapter)this.mBookmarksListView.getAdapter();
      Bookmark bookmark = adapter.getItem(position);

      //Configure home
      if (bookmark.getType().compareTo(BOOKMARK_TYPE.HOME) == 0) {
          //Show a dialog for configure initial directory
          List<Bookmark> bookmarks = BookmarksDatabase.getInstance().getAllBookmarks();
          InitialDirectoryDialog dialog = new InitialDirectoryDialog(this, bookmarks);
          dialog.setOnValueChangedListener(new InitialDirectoryDialog.OnValueChangedListener() {
              @Override
              @SuppressWarnings("synthetic-access")
              public void onValueChanged(String newInitialDir) {
                  refresh();
              }
          });
          dialog.show();
          return;
      }

      //Remove bookmark
      if (bookmark.getType().compareTo(BOOKMARK_TYPE.USER_DEFINED) == 0) {
          boolean result = BookmarksDatabase.getInstance().removeBookmark(bookmark);
          if (!result) {
              //Show warning
              DialogHelper.showToast(this, R.string.msgs_operation_failure, Toast.LENGTH_SHORT);
              return;
          }
          refresh();
          return;
      }
    }

    /**
     * Method that returns to previous activity and.
     *
     * @param canceled Indicates if the activity was canceled
     * @param directory The directory of the selected bookmark
     */
    private void back(final boolean canceled, final String directory) {
        Intent intent =  new Intent();
        if (canceled) {
            setResult(RESULT_CANCELED, intent);
        } else {
            intent.putExtra(NavigationActivity.EXTRA_BOOKMARK_SELECTION, directory);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    /**
     * Method that loads all kind of bookmarks and join in
     * an array to be used in the listview adapter.
     *
     * @return List<Bookmark>
     */
    private List<Bookmark> loadBookmarks() {
        //Bookmarks = HOME + FILESYSTEM + SD STORAGES + USER DEFINED
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        bookmarks.add(loadHomeBookmarks());
        bookmarks.addAll(loadFilesystemBookmarks());
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
                                ExplorerSettings.SETTINGS_INITIAL_DIR.getId(),
                                (String)ExplorerSettings.SETTINGS_INITIAL_DIR.getDefaultValue());
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
                        CharSequence name =
                                getString(parser.getAttributeResourceValue(
                                        R.styleable.Bookmark_name, 0));
                        CharSequence directory =
                                parser.getAttributeValue(R.styleable.Bookmark_directory);
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
            //IMP!! Android SDK doesn't have a "getVolumeList" but is supported by CM9.
            //Use reflect to get this value (if possible)
            StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            Method method = sm.getClass().getMethod("getVolumeList"); //$NON-NLS-1$
            StorageVolume[] volumes = (StorageVolume[])method.invoke(sm);
            for (int i = 0; i < volumes.length; i++) {
                if (volumes[i].getPath().toLowerCase().indexOf("usb") != -1) { //$NON-NLS-1$
                    bookmarks.add(
                            new Bookmark(
                                    BOOKMARK_TYPE.USB,
                                    volumes[i].getDescription(),
                                    volumes[i].getPath()));
                } else {
                    bookmarks.add(
                            new Bookmark(
                                    BOOKMARK_TYPE.SDCARD,
                                    volumes[i].getDescription(),
                                    volumes[i].getPath()));
                }
            }

            //Return the bookmarks
            return bookmarks;

        } catch (NoSuchMethodException nsmex) {
            //Ignore. Android SDK StorageManager class doesn't have this method
            //Use default android information from environment
            try {
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage != null) {
                    String path = externalStorage.getCanonicalPath();
                    if (path.toLowerCase().indexOf("usb") != -1) { //$NON-NLS-1$
                        bookmarks.add(
                                new Bookmark(
                                        BOOKMARK_TYPE.USB,
                                        getString(R.string.bookmarks_external_storage),
                                        path));
                    } else {
                        bookmarks.add(
                                new Bookmark(
                                        BOOKMARK_TYPE.SDCARD,
                                        getString(R.string.bookmarks_external_storage),
                                        path));
                    }
                }
                //Return the bookmarks
                return bookmarks;
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }


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
    @SuppressWarnings("static-method")
    private List<Bookmark> loadUserBookmarks() {
        return BookmarksDatabase.getInstance().getAllBookmarks();
    }
}

