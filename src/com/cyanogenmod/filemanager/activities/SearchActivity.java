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
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.cyanogenmod.filemanager.FileManagerApplication;
import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.activities.preferences.SearchPreferenceFragment;
import com.cyanogenmod.filemanager.activities.preferences.SettingsPreferences;
import com.cyanogenmod.filemanager.adapters.SearchResultAdapter;
import com.cyanogenmod.filemanager.commands.AsyncResultExecutable;
import com.cyanogenmod.filemanager.commands.ConcurrentAsyncResultListener;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.console.RelaunchableException;
import com.cyanogenmod.filemanager.listeners.OnRequestRefreshListener;
import com.cyanogenmod.filemanager.model.Directory;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.ParentDirectory;
import com.cyanogenmod.filemanager.model.Query;
import com.cyanogenmod.filemanager.model.SearchResult;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.parcelables.SearchInfoParcelable;
import com.cyanogenmod.filemanager.preferences.AccessMode;
import com.cyanogenmod.filemanager.preferences.FileManagerSettings;
import com.cyanogenmod.filemanager.preferences.Preferences;
import com.cyanogenmod.filemanager.providers.RecentSearchesContentProvider;
import com.cyanogenmod.filemanager.ui.ThemeManager;
import com.cyanogenmod.filemanager.ui.ThemeManager.Theme;
import com.cyanogenmod.filemanager.ui.dialogs.ActionsDialog;
import com.cyanogenmod.filemanager.ui.policy.DeleteActionPolicy;
import com.cyanogenmod.filemanager.ui.policy.IntentsActionPolicy;
import com.cyanogenmod.filemanager.ui.widgets.ButtonItem;
import com.cyanogenmod.filemanager.ui.widgets.FlingerListView;
import com.cyanogenmod.filemanager.ui.widgets.FlingerListView.OnItemFlingerListener;
import com.cyanogenmod.filemanager.ui.widgets.FlingerListView.OnItemFlingerResponder;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.DialogHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;
import com.cyanogenmod.filemanager.util.ExceptionUtil.OnRelaunchCommandResult;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper.MimeTypeCategory;
import com.cyanogenmod.filemanager.util.SearchHelper;
import com.cyanogenmod.filemanager.util.StorageHelper;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An activity for search files and folders.
 */
public class SearchActivity extends Activity
    implements OnItemClickListener, OnItemLongClickListener, OnRequestRefreshListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "SearchActivity"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    /**
     * An {@link Intent} action for restore view information.
     */
    public static final String ACTION_RESTORE =
            "com.cyanogenmod.filemanager.activities.SearchActivity#Restore"; //$NON-NLS-1$

    /**
     * Intent extra parameter for search in the selected directory on enter.
     */
    public static final String EXTRA_SEARCH_DIRECTORY = "extra_search_directory";  //$NON-NLS-1$

    /**
     * Intent extra parameter for pass the restore information.
     */
    public static final String EXTRA_SEARCH_RESTORE = "extra_search_restore";  //$NON-NLS-1$

    /**
     * Intent extra parameter for passing the target mime type information.
     */
    public static final String EXTRA_SEARCH_MIMETYPE = "extra_search_mimetype";  //$NON-NLS-1$

    //Minimum characters to allow query
    private static final int MIN_CHARS_SEARCH = 3;

    private final BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().compareTo(
                        FileManagerSettings.INTENT_SETTING_CHANGED) == 0) {

                    // The settings has changed
                    String key = intent.getStringExtra(
                            FileManagerSettings.EXTRA_SETTING_CHANGED_KEY);
                    if (key != null) {
                        if (SearchActivity.this.mSearchListView.getAdapter() != null &&
                           (key.compareTo(
                                   FileManagerSettings.
                                       SETTINGS_HIGHLIGHT_TERMS.getId()) == 0 ||
                            key.compareTo(
                                    FileManagerSettings.
                                        SETTINGS_SHOW_RELEVANCE_WIDGET.getId()) == 0 ||
                            key.compareTo(
                                    FileManagerSettings.
                                        SETTINGS_SORT_SEARCH_RESULTS_MODE.getId()) == 0)) {

                            // Recreate the adapter
                            int pos = SearchActivity.
                                        this.mSearchListView.getFirstVisiblePosition();
                            mAdapter.notifyDataSetChanged();
                            SearchActivity.this.mSearchListView.setSelection(pos);
                            return;
                        }
                    }
                } else if (intent.getAction().compareTo(
                        FileManagerSettings.INTENT_THEME_CHANGED) == 0) {
                    applyTheme();
                }
            }
        }
    };

    /**
     * A listener for flinging events from {@link FlingerListView}
     */
    private final OnItemFlingerListener mOnItemFlingerListener = new OnItemFlingerListener() {

        @Override
        public boolean onItemFlingerStart(
                AdapterView<?> parent, View view, int position, long id) {
            try {
                // Response if the item can be removed
                SearchResultAdapter adapter = (SearchResultAdapter)parent.getAdapter();
                SearchResult result = adapter.getItem(position);
                if (result != null && result.getFso() != null) {
                    if (result.getFso() instanceof ParentDirectory) {
                        // This is not possible ...
                        return false;
                    }
                    return true;
                }
            } catch (Exception e) {
                ExceptionUtil.translateException(SearchActivity.this, e, true, false);
            }
            return false;
        }

        @Override
        public void onItemFlingerEnd(OnItemFlingerResponder responder,
                AdapterView<?> parent, View view, int position, long id) {

            try {
                // Response if the item can be removed
                SearchResultAdapter adapter = (SearchResultAdapter)parent.getAdapter();
                SearchResult result = adapter.getItem(position);
                if (result != null && result.getFso() != null) {
                    DeleteActionPolicy.removeFileSystemObject(
                            SearchActivity.this,
                            result.getFso(),
                            null,
                            SearchActivity.this,
                            responder);
                    return;
                }

                // Cancels the flinger operation
                responder.cancel();

            } catch (Exception e) {
                ExceptionUtil.translateException(SearchActivity.this, e, true, false);
                responder.cancel();
            }
        }
    };

    private ConcurrentAsyncResultListener mAsyncListener = new ConcurrentAsyncResultListener() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onConcurrentAsyncStart() {
            mSearchInProgress = true;
            mAdapter.startStreaming();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onConcurrentAsyncEnd(boolean cancelled) {
            mSearchInProgress = false;
            mSearchListView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mExecutable = null;
                        mAdapter.stopStreaming();
                        int resultsSize = mAdapter.resultsSize();
                        mStreamingSearchProgress.setVisibility(View.INVISIBLE);
                        if (mMimeTypeCategories != null && mMimeTypeCategories.size() > 1) {
                            mMimeTypeSpinner.setVisibility(View.VISIBLE);
                        }
                        mSearchListView.setVisibility(resultsSize > 0 ? View.VISIBLE : View.GONE);
                        mEmptyListMsg.setVisibility(resultsSize > 0 ? View.GONE : View.VISIBLE);

                    } catch (Throwable ex) {
                        Log.e(TAG, "onAsyncEnd method fails", ex); //$NON-NLS-1$
                    }
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public void onConcurrentPartialResult(final Object partialResults) {
            //Saved in the global result list, for save at the end
            FileSystemObject result = null;
            if (partialResults instanceof FileSystemObject) {
                FileSystemObject fso = (FileSystemObject) partialResults;
                if (mMimeTypeCategories == null || mMimeTypeCategories.contains(MimeTypeHelper
                        .getCategory(SearchActivity.this, fso))) {
                    SearchActivity.this.mResultList.add((FileSystemObject) partialResults);
                    showSearchResult((FileSystemObject) partialResults);
                }
            } else {
                List<FileSystemObject> fsoList = (List<FileSystemObject>) partialResults;
                for (FileSystemObject fso : fsoList) {
                    if (mMimeTypeCategories == null || mMimeTypeCategories.contains(MimeTypeHelper
                            .getCategory(SearchActivity.this, fso))) {
                        SearchActivity.this.mResultList.add(fso);
                        showSearchResult(fso);
                    }
                }
            }

            //Notify progress
            mSearchListView.post(new Runnable() {
                @Override
                public void run() {
                    int progress = mAdapter.resultsSize();
                    String foundItems =
                            getResources().
                                    getQuantityString(
                                            R.plurals.search_found_items, progress,
                                            Integer.valueOf(progress) );
                    mSearchFoundItems.setText(
                            getString(
                                    R.string.search_found_items_in_directory,
                                    foundItems,
                                    mSearchDirectory));
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onConcurrentAsyncExitCode(int exitCode) {/**NON BLOCK**/}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onConcurrentException(Exception cause) {
            //Capture the exception
            ExceptionUtil.translateException(SearchActivity.this, cause);
        }
    };

    /**
     * @hide
     */
    AsyncResultExecutable mExecutable = null;

    /**
     * @hide
     */
    ListView mSearchListView;
    /**
     * @hide
     */
    ProgressBar mSearchWaiting;
    /**
     * @hide
     */
    TextView mSearchFoundItems;
    /**
     * @hide
     */
    TextView mSearchTerms;
    private View mEmptyListMsg;

    /**
     * @hide
     */
    Spinner mMimeTypeSpinner;

    private String mSearchDirectory;
    /**
     * @hide
     */
    List<FileSystemObject> mResultList;
    /**
     * @hide
     */
    Query mQuery;

    /**
     * @hide
     */
    SearchInfoParcelable mRestoreState;

    /**
     * @hide
     */
    boolean mChRooted;

    /**
     * @hide
     */
    HashSet<MimeTypeCategory> mMimeTypeCategories;

    private SearchResultAdapter mAdapter;
    private ProgressBar mStreamingSearchProgress;
    private boolean mSearchInProgress;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle state) {
        if (DEBUG) {
            Log.d(TAG, "SearchActivity.onCreate"); //$NON-NLS-1$
        }

        // Check if app is running in chrooted mode
        this.mChRooted = FileManagerApplication.getAccessMode().compareTo(AccessMode.SAFE) == 0;

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileManagerSettings.INTENT_SETTING_CHANGED);
        filter.addAction(FileManagerSettings.INTENT_THEME_CHANGED);
        registerReceiver(this.mNotificationReceiver, filter);

        // Set the theme before setContentView
        Theme theme = ThemeManager.getCurrentTheme(this);
        theme.setBaseTheme(this, false);

        //Set in transition
        overridePendingTransition(R.anim.translate_to_right_in, R.anim.hold_out);

        //Set the main layout of the activity
        setContentView(R.layout.search);

        //Restore state
        if (state != null) {
            restoreState(state);
        }

        //Initialize action bars and searc
        initTitleActionBar();
        initComponents();

        // Apply current theme
        applyTheme();

        if (this.mRestoreState != null) {
            //Restore activity from cached data
            loadFromCacheData();
        } else {
            //New query
            if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
                initSearch();
            } else if (ACTION_RESTORE.equals(getIntent().getAction())) {
                restoreState(getIntent().getExtras());
                loadFromCacheData();
            }
        }

        //Save state
        super.onCreate(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(TAG, "SearchActivity.onDestroy"); //$NON-NLS-1$
        }

        // Unregister the receiver
        try {
            unregisterReceiver(this.mNotificationReceiver);
        } catch (Throwable ex) {
            /**NON BLOCK**/
        }
        recycle();

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
     * {@inheritDoc}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        //New query
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            initSearch();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        //Set out transition
        overridePendingTransition(R.anim.hold_in, R.anim.translate_to_left_out);
        super.onPause();
        // stop search if the activity moves out of the foreground
        if (mExecutable != null) {
            mExecutable.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG) {
            Log.d(TAG, "SearchActivity.onSaveInstanceState"); //$NON-NLS-1$
        }
        saveState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Method that save the instance of the activity.
     *
     * @param state The current state of the activity
     */
    private void saveState(Bundle state) {
        try {
            if (this.mSearchListView.getAdapter() != null) {
                state.putParcelable(EXTRA_SEARCH_RESTORE, createSearchInfo());
            }
        } catch (Throwable ex) {
            Log.w(TAG, "The state can't be saved", ex); //$NON-NLS-1$
        }
    }

    /**
     * Method that restore the instance of the activity.
     *
     * @param state The previous state of the activity
     */
    private void restoreState(Bundle state) {
        try {
            if (state.containsKey(EXTRA_SEARCH_RESTORE)) {
                this.mRestoreState = state.getParcelable(EXTRA_SEARCH_RESTORE);
            }
        } catch (Throwable ex) {
            Log.w(TAG, "The state can't be restored", ex); //$NON-NLS-1$
        }
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    private void initTitleActionBar() {
        //Configure the action bar options
        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.bg_material_titlebar));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);

        View customTitle = getLayoutInflater().inflate(R.layout.simple_customtitle, null, false);

        TextView title = (TextView)customTitle.findViewById(R.id.customtitle_title);
        title.setText(R.string.search);
        title.setContentDescription(getString(R.string.search));
        ButtonItem configuration = (ButtonItem)customTitle.findViewById(R.id.ab_button1);
        configuration.setImageResource(R.drawable.ic_material_light_config);
        configuration.setVisibility(View.VISIBLE);
        actionBar.setCustomView(customTitle);
    }

    /**
     * Method that initializes the component of the activity.
     */
    private void initComponents() {
        //Empty list view
        this.mEmptyListMsg = findViewById(R.id.search_empty_msg);
        //The list view
        this.mSearchListView = (ListView)findViewById(R.id.search_listview);
        this.mSearchListView.setOnItemClickListener(this);
        this.mSearchListView.setOnItemLongClickListener(this);

        // If we should set the listview to response to flinger gesture detection
        boolean useFlinger =
                Preferences.getSharedPreferences().getBoolean(
                        FileManagerSettings.SETTINGS_USE_FLINGER.getId(),
                            ((Boolean)FileManagerSettings.
                                    SETTINGS_USE_FLINGER.
                                        getDefaultValue()).booleanValue());
        if (useFlinger) {
            ((FlingerListView)this.mSearchListView).
                    setOnItemFlingerListener(this.mOnItemFlingerListener);
        }

        //Other components
        this.mSearchWaiting = (ProgressBar)findViewById(R.id.search_waiting);
        mStreamingSearchProgress = (ProgressBar) findViewById(R.id.streaming_progress_bar);
        mStreamingSearchProgress.getIndeterminateDrawable()
                .setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        this.mSearchFoundItems = (TextView)findViewById(R.id.search_status_found_items);
        setFoundItems(0, ""); //$NON-NLS-1$
        this.mSearchTerms = (TextView)findViewById(R.id.search_status_query_terms);
        this.mSearchTerms.setText(
                Html.fromHtml(getString(R.string.search_terms, ""))); //$NON-NLS-1$

        // populate Mime Types spinner for search results filtering
        mMimeTypeSpinner = (Spinner) findViewById(R.id.search_status_type_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.search_spinner_item,
                MimeTypeHelper.MimeTypeCategory.getFriendlyLocalizedNames(this));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMimeTypeSpinner.setAdapter(adapter);
        mMimeTypeSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Method invoked when an action item is clicked.
     *
     * @param view The button pushed
     */
    public void onActionBarItemClick(View view) {
        switch (view.getId()) {
            case R.id.ab_button1:
                //Settings
                Intent settings = new Intent(this, SettingsPreferences.class);
                settings.putExtra(
                        PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        SearchPreferenceFragment.class.getName());
                startActivity(settings);
                break;

            default:
                break;
        }
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    private void initSearch() {
        Serializable mimeTypeExtra = getIntent().getSerializableExtra(EXTRA_SEARCH_MIMETYPE);

        if (mimeTypeExtra != null) {
            ArrayList<MimeTypeCategory> categories = (ArrayList<MimeTypeCategory>) getIntent()
                    .getSerializableExtra(EXTRA_SEARCH_MIMETYPE);
            // setting load factor to 1 to avoid the backing map's resizing
            mMimeTypeCategories = new HashSet<MimeTypeCategory>(categories.size(), 1);
            for (MimeTypeCategory category : categories) {
                mMimeTypeCategories.add(category);
            }
        }

        //If data is not present, use root directory to do the search
        this.mSearchDirectory = FileHelper.ROOT_DIRECTORY;
        String searchDirectory = getIntent()
                .getStringExtra(SearchActivity.EXTRA_SEARCH_DIRECTORY);

        if (!TextUtils.isEmpty(searchDirectory)) {
            this.mSearchDirectory = searchDirectory;
        }
        setFoundItems(0, mSearchDirectory);
        //Retrieve the query ¿from voice recognizer?
        boolean voiceQuery = true;
        List<String> userQueries =
                getIntent().getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
        if (userQueries == null || userQueries.size() == 0) {
            //From input text
            userQueries = new ArrayList<String>();
            //Recovers and save the last term search in the memory
            Preferences.setLastSearch(getIntent().getStringExtra(SearchManager.QUERY));
            userQueries.add(Preferences.getLastSearch());
            voiceQuery = false;
        }

        //Filter the queries? Needed if queries come from voice recognition
        final List<String> filteredUserQueries =
                (voiceQuery) ? filterQuery(userQueries) : userQueries;

        //Create the queries
        this.mQuery = new Query().fillSlots(filteredUserQueries);
        List<String> queries = this.mQuery.getQueries();

        boolean ask = false;
        // Mime type search uses '*' which needs to bypass
        // length check
        if (mMimeTypeCategories == null) {
            //Check if some queries has lower than allowed, in this case
            //request the user for stop the search
            int cc = queries.size();
            for (int i = 0; i < cc; i++) {
                if (queries.get(i).trim().length() < MIN_CHARS_SEARCH) {
                    ask = true;
                    break;
                }
            }
            mMimeTypeSpinner.setVisibility(View.VISIBLE);
        } else {
            mMimeTypeSpinner.setVisibility(View.INVISIBLE);
        }
        if (ask) {
            askUserBeforeSearch(voiceQuery, this.mQuery, this.mSearchDirectory);
        } else {
            doSearch(voiceQuery, this.mQuery, this.mSearchDirectory);
        }

    }

    /**
     * Method that ask the user before do the search.
     *
     * @param voiceQuery Indicates if the query is from voice recognition
     * @param query The terms of the search
     * @param searchDirectory The directory of the search
     */
    private void askUserBeforeSearch(
            final boolean voiceQuery, final Query query, final String searchDirectory) {
        //Show a dialog asking the user
        AlertDialog dialog =
                DialogHelper.createYesNoDialog(
                        this,
                        R.string.search_few_characters_title,
                        R.string.search_few_characters_msg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface alertDialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    doSearch(voiceQuery, query, searchDirectory);
                                    return;
                                }

                                //Close search activity
                                back(true, null, false);
                            }
                       });
        DialogHelper.delegateDialogShow(this, dialog);
    }

    /**
     * Method that do the search.
     *
     * @param voiceQuery Indicates if the query is from voice recognition
     * @param query The terms of the search
     * @param searchDirectory The directory of the search
     * @hide
     */
    void doSearch(
            final boolean voiceQuery, final Query query, final String searchDirectory) {

        // Recovers the user preferences about save suggestions
        boolean saveSuggestions = Preferences.getSharedPreferences().getBoolean(
                FileManagerSettings.SETTINGS_SAVE_SEARCH_TERMS.getId(),
                ((Boolean) FileManagerSettings.SETTINGS_SAVE_SEARCH_TERMS.
                        getDefaultValue()).booleanValue());
        if (saveSuggestions) {
            //Save every query for use as recent suggestions
            SearchRecentSuggestions suggestions =
                    new SearchRecentSuggestions(this,
                            RecentSearchesContentProvider.AUTHORITY,
                            RecentSearchesContentProvider.MODE);
            if (!voiceQuery) {
                List<String> queries = query.getQueries();
                int cc = queries.size();
                for (int i = 0; i < cc; i++) {
                    suggestions.saveRecentQuery(queries.get(i), null);
                }
            }
        }

        //Set the listview
        if (this.mSearchListView.getAdapter() != null) {
            ((SearchResultAdapter)this.mSearchListView.getAdapter()).dispose();
        }
        this.mResultList = new ArrayList<FileSystemObject>();
        mAdapter =
                new SearchResultAdapter(this,
                        new ArrayList<SearchResult>(), R.layout.search_item, this.mQuery);
        this.mSearchListView.setAdapter(mAdapter);

        //Set terms
        if (mMimeTypeCategories == null) {
            this.mSearchTerms.setText(
                    Html.fromHtml(getString(R.string.search_terms, query.getTerms())));
        } else {
            ArrayList<String> localizedNames = new ArrayList<String>(mMimeTypeCategories.size());
            for (MimeTypeCategory category : mMimeTypeCategories) {
                localizedNames
                        .add(NavigationActivity.MIME_TYPE_LOCALIZED_NAMES[category.ordinal()]);
            }
             this.mSearchTerms.setText(
                     Html.fromHtml(getString(R.string.search_terms, localizedNames)));
        }

        //Now, do the search in background
        this.mSearchListView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // Execute the query (search in background)
                    SearchActivity.this.mExecutable =
                            CommandHelper.findFiles(
                                    SearchActivity.this,
                                    searchDirectory,
                                    mQuery,
                                    mAsyncListener,
                                    null);

                } catch (Throwable ex) {
                    //Remove all elements
                    try {
                        SearchActivity.this.removeAll();
                    } catch (Throwable ex2) {
                        /**NON BLOCK**/
                    }

                    //Capture the exception
                    Log.e(TAG, "Search failed", ex); //$NON-NLS-1$
                    DialogHelper.showToast(
                            SearchActivity.this,
                            R.string.search_error_msg, Toast.LENGTH_SHORT);
                    SearchActivity.this.mSearchListView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Ensures the search result meets user preferences and passes it to the adapter for display
     *
     * @param result FileSystemObject that matches the search result criteria
     */
    private void showSearchResult(FileSystemObject result) {
        // check against user's display preferences
        if ( !FileHelper.compliesWithDisplayPreferences(result, null, mChRooted) ) {
            return;
        }

        // resolve sym links
        FileHelper.resolveSymlink(this, result);

        // convert to search result
        SearchResult searchResult = SearchHelper.convertToResult(result, mQuery);

        // add to adapter
        mAdapter.addNewItem(searchResult);
    }

    /**
     * Method that restore the activity from the cached data.
     */
    private void loadFromCacheData() {
        this.mSearchListView.post(new Runnable() {
            @Override
            public void run() {
                //Toggle results
                List<SearchResult> list = SearchActivity.this.mRestoreState.getSearchResultList();
                String directory = SearchActivity.this.mRestoreState.getSearchDirectory();
                SearchActivity.this.toggleResults(list.size() > 0, true);
                setFoundItems(list.size(), directory);

                //Set terms
                Query query = SearchActivity.this.mRestoreState.getSearchQuery();
                String terms =
                        TextUtils.join(" | ",  //$NON-NLS-1$;
                                query.getQueries().toArray(new String[]{}));
                if (terms.endsWith(" | ")) { //$NON-NLS-1$;
                    terms = ""; //$NON-NLS-1$;
                }
                SearchActivity.this.mSearchTerms.setText(
                        Html.fromHtml(getString(R.string.search_terms, terms)));

                try {
                    if (SearchActivity.this.mSearchWaiting != null) {
                        SearchActivity.this.mSearchWaiting.setVisibility(View.VISIBLE);
                    }

                    //Add list to the listview
                    if (SearchActivity.this.mSearchListView.getAdapter() != null) {
                        ((SearchResultAdapter)SearchActivity.this.
                                mSearchListView.getAdapter()).clear();
                    }
                    SearchResultAdapter adapter =
                            new SearchResultAdapter(
                                                SearchActivity.this.mSearchListView.getContext(),
                                                list,
                                                R.layout.search_item,
                                                query);
                    SearchActivity.this.mSearchListView.setAdapter(adapter);
                    SearchActivity.this.mSearchListView.setSelection(0);

                    SearchActivity.this.mQuery = query;
                    SearchActivity.this.mSearchDirectory = mRestoreState.getSearchDirectory();

                    mStreamingSearchProgress.setVisibility(View.INVISIBLE);

                } catch (Throwable ex) {
                    //Capture the exception
                    ExceptionUtil.translateException(SearchActivity.this, ex);

                } finally {
                    //Hide waiting
                    if (SearchActivity.this.mSearchWaiting != null) {
                        SearchActivity.this.mSearchWaiting.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    /**
     * Method that filter the user queries for valid queries only.<br/>
     * <br/>
     * Only allow query strings with more that 3 characters
     *
     * @param original The original user queries
     * @return List<String> The list of queries filtered
     */
    @SuppressWarnings("static-method")
    private List<String> filterQuery(List<String> original) {
        List<String> dst = new ArrayList<String>(original);
        int cc = dst.size();
        for (int i = cc - 1; i >= 0; i--) {
            String query = dst.get(i);
            if (query == null || query.trim().length() < MIN_CHARS_SEARCH) {
                dst.remove(i);
            }
        }
        return dst;
    }

    /**
     * Method that removes all items and display a message.
     * @hide
     */
    void removeAll() {
        SearchResultAdapter adapter = (SearchResultAdapter)this.mSearchListView.getAdapter();
        adapter.clear();
        this.mSearchListView.setSelection(0);
        toggleResults(false, true);
    }

    /**
     * Method that toggle the views when there are results.
     *
     * @param hasResults Indicates if there are results
     * @param showEmpty Show the empty list message
     * @hide
     */
    void toggleResults(boolean hasResults, boolean showEmpty) {
        this.mSearchListView.setVisibility(hasResults ? View.VISIBLE : View.INVISIBLE);
        this.mEmptyListMsg.setVisibility(!hasResults && showEmpty ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Method that display the number of found items.
     *
     * @param items The number of items
     * @param searchDirectory The search directory path
     * @hide
     */
    void setFoundItems(final int items, final String searchDirectory) {
        if (this.mSearchFoundItems != null) {
            this.mSearchFoundItems.post(new Runnable() {
                @Override
                public void run() {
                    String directory = searchDirectory;
                    if (SearchActivity.this.mChRooted &&
                            directory != null && directory.length() > 0) {
                        directory = StorageHelper.getChrootedPath(directory);
                    }

                    String foundItems =
                            getResources().
                                getQuantityString(
                                    R.plurals.search_found_items, items, Integer.valueOf(items));
                    SearchActivity.this.mSearchFoundItems.setText(
                                            getString(
                                                R.string.search_found_items_in_directory,
                                                foundItems,
                                                directory));
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
       if (mExecutable != null) {
            mExecutable.end();
       }
       back(true, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case android.R.id.home:
              // release Console lock held by the async search task
              if (mExecutable != null) {
                  mExecutable.end();
              }
              back(true, null, false);
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
        // cancel search query if in progress
        // *need* to do this as the async query holds a lock on the Console and we need the Console
        // to gather additional file info in order to process the click event
        if (mSearchInProgress) mExecutable.end();

        try {
            SearchResult result = ((SearchResultAdapter)parent.getAdapter()).getItem(position);
            FileSystemObject fso = result.getFso();
            if (fso instanceof Directory) {
                back(false, fso, false);
                return;
            } else if (fso instanceof Symlink) {
                Symlink symlink = (Symlink)fso;
                if (symlink.getLinkRef() != null && symlink.getLinkRef() instanceof Directory) {
                    back(false, symlink.getLinkRef(), false);
                    return;
                }
                fso = symlink.getLinkRef();
            }

            // special treatment for video files
            // all of the video files in the current search will also be sent as an extra in the
            // intent along with the item that was clicked
            MimeTypeCategory fileCategory = MimeTypeHelper.getCategoryFromExt(this,
                    FileHelper.getExtension(fso), fso.getFullPath());
            if (fileCategory == MimeTypeCategory.VIDEO) {

                ArrayList<FileSystemObject> filteredList = filterSearchResults(fileCategory);
                ArrayList<Uri> uris = new ArrayList<Uri>(filteredList.size());

                for (FileSystemObject f : filteredList) {
                    uris.add(f.getFileUri());
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(fso.getFileUri(), MimeTypeHelper.getMimeType(this, fso));
                if (filteredList.size() > 1) {
                    intent.putParcelableArrayListExtra("EXTRA_FILE_LIST", uris);
                }

                if (DEBUG) {
                    Log.i(TAG, "video intent : " + intent);
                }

                try {
                    startActivity(intent);
                } catch(ActivityNotFoundException e) {
                    Log.e(TAG, "ActivityNotFoundException when opening a video file");
                    Toast.makeText(this, R.string.activity_not_found_exception, Toast.LENGTH_SHORT);
                }

                return;
            }

            // for other files, open them with their preferred registered app
            back(false, fso, false);

        } catch (Throwable ex) {
            ExceptionUtil.translateException(this.mSearchListView.getContext(), ex);
        }
    }

    /**
     * Returns a subset of the search results falling into the given category
     * @param category MimeTypeCategory
     * @return list of FileSystemObjects that
     */
    private ArrayList<FileSystemObject> filterSearchResults(MimeTypeCategory category) {
        ArrayList<FileSystemObject> filteredList = new ArrayList<FileSystemObject>();

        if (mAdapter.getCount() < 1) return filteredList;

        for (FileSystemObject fso : mAdapter.getFiles()) {
            if (MimeTypeHelper.getCategoryFromExt(this,
                                                  FileHelper.getExtension(fso),
                                                  fso.getFullPath()) == category) {
                filteredList.add(fso);
            }
        }

        return filteredList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // Different actions depending on user preference

        // Get the adapter, the search result and the fso
        SearchResultAdapter adapter = ((SearchResultAdapter)parent.getAdapter());
        SearchResult searchResult = adapter.getItem(position);
        FileSystemObject fso = searchResult.getFso();

        // Open the actions menu
        onRequestMenu(fso);
        return true; //Always consume the event
    }

    /**
     * Method invoked when a request to show the menu associated
     * with an item is started.
     *
     * @param item The item for which the request was started
     */
    public void onRequestMenu(FileSystemObject item) {
        // Prior to show the dialog, refresh the item reference
        FileSystemObject fso = null;
        try {
            fso = CommandHelper.getFileInfo(this, item.getFullPath(), false, null);
            if (fso == null) {
                throw new NoSuchFileOrDirectory(item.getFullPath());
            }

        } catch (Exception e) {
            // Notify the user
            ExceptionUtil.translateException(this, e);

            // Remove the object
            if (e instanceof FileNotFoundException || e instanceof NoSuchFileOrDirectory) {
                removeItem(item);
            }
            return;
        }

        ActionsDialog dialog = new ActionsDialog(this, null, fso, false, true);
        dialog.setOnRequestRefreshListener(this);
        dialog.show();
    }

    /**
     * Method that removes the {@link FileSystemObject} reference
     *
     * @param fso The file system object
     */
    private void removeItem(FileSystemObject fso) {
        SearchResultAdapter adapter =
                (SearchResultAdapter)this.mSearchListView.getAdapter();
        if (adapter != null) {
            int pos = adapter.getPosition(fso);
            if (pos != -1) {
                SearchResult sr = adapter.getItem(pos);
                adapter.remove(sr);
            }

            // Toggle resultset?
            toggleResults(adapter.getCount() > 0, true);
            setFoundItems(adapter.getCount(), this.mSearchDirectory);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestRefresh(Object o, boolean clearSelection) {
        // Refresh only the item
        SearchResultAdapter adapter =
                (SearchResultAdapter)this.mSearchListView.getAdapter();
        if (adapter != null) {
            if (o instanceof FileSystemObject) {

                FileSystemObject fso = (FileSystemObject)o;
                int pos = adapter.getPosition(fso);
                if (pos >= 0) {
                    SearchResult sr = adapter.getItem(pos);
                    sr.setFso(fso);
                }
            } else if (o == null) {
                // Refresh all
                List<SearchResult> results = adapter.getData();
                this.mResultList = new ArrayList<FileSystemObject>(results.size());
                int cc = results.size();
                for (int i = 0; i < cc; i++) {
                    this.mResultList.add(results.get(i).getFso());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestBookmarksRefresh() {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestRemove(Object o, boolean clearSelection) {
        if (o instanceof FileSystemObject) {
            removeItem((FileSystemObject) o);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNavigateTo(Object o) {
        if (o instanceof FileSystemObject) {
            back(false, (FileSystemObject) o, true);
        }
    }

    @Override
    public void onCancel() {
        // nop
    }

    /**
     * Method that returns to previous activity.
     *
     * @param cancelled Indicates if the activity was cancelled
     * @param item The fso
     * @param isChecked If the fso was fully retrieve previously to this call. Otherwise, a
     * getFileInfo call is done to complete the fso information
     * @hide
     */
    void back(final boolean cancelled, FileSystemObject item, boolean isChecked) {
        final Context ctx = SearchActivity.this;
        boolean finish = true;
        if (cancelled) {
            final Intent intent =  new Intent();
            if (this.mRestoreState != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(NavigationActivity.EXTRA_SEARCH_LAST_SEARCH_DATA,
                        (Parcelable)this.mRestoreState);
                intent.putExtras(bundle);
            }
            setResult(RESULT_CANCELED, intent);
        } else {
            // Check that the bookmark exists
            FileSystemObject fso = item;
            try {
                if (!isChecked) {
                    fso = CommandHelper.getFileInfo(ctx, item.getFullPath(), null);
                }
                finish = navigateTo(fso);

            } catch (Exception e) {
                // Capture the exception
                final FileSystemObject fFso = fso;
                final OnRelaunchCommandResult relaunchListener = new OnRelaunchCommandResult() {
                    @Override
                    public void onSuccess() {
                        if (navigateTo(fFso)) {
                            exit();
                        }
                    }
                    @Override
                    public void onFailed(Throwable cause) {
                        ExceptionUtil.translateException(ctx, cause, false, false);
                    }
                    @Override
                    public void onCancelled() { /** NON BLOCK**/}
                };
                ExceptionUtil.translateException(ctx, e, false, true, relaunchListener);
                if (!(e instanceof RelaunchableException)) {
                    if (e instanceof NoSuchFileOrDirectory || e instanceof FileNotFoundException) {
                        // The fso not exists, delete the fso from the search
                        try {
                            removeItem(fso);
                        } catch (Exception ex) {/**NON BLOCK**/}
                    }
                }
                return;
            }
        }

        // End this activity
        if (finish) {
            exit();
        }
    }

    /**
     * Method invoked when the activity needs to exit
     */
    private void exit() {
        finish();
    }

    private void recycle() {
        if (this.mSearchListView.getAdapter() != null) {
            ((SearchResultAdapter)this.mSearchListView.getAdapter()).dispose();
        }
    }

    /**
     * Method that navigate to the file system used the intent (NavigationActivity)
     *
     * @param fso The file system object to navigate to
     * @param intent The intent used to navigate to
     * @return boolean If the action implies finish this activity
     */
    boolean navigateTo(FileSystemObject fso) {
        if (fso != null) {
            if (FileHelper.isDirectory(fso)) {
                final Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(NavigationActivity.EXTRA_SEARCH_ENTRY_SELECTION, fso);
                bundle.putParcelable(NavigationActivity.EXTRA_SEARCH_LAST_SEARCH_DATA,
                        (Parcelable)createSearchInfo());
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                return true;
            }

            // Open the file here, so when focus back to the app, the search activity
            // its in top of the stack
            IntentsActionPolicy.openFileSystemObject(this, fso, false, null, null);
        } else {
            // The fso not exists, delete the fso from the search
            try {
                removeItem(fso);
            } catch (Exception ex) {/**NON BLOCK**/}
        }
        return false;
    }

    /**
     * Method that creates a {@link SearchInfoParcelable} reference from
     * the current data.
     *
     * @return SearchInfoParcelable The search info reference
     */
    private SearchInfoParcelable createSearchInfo() {
        SearchInfoParcelable parcel = new SearchInfoParcelable(
                mSearchDirectory,
                ((SearchResultAdapter)this.mSearchListView.getAdapter()).getData(),
                mQuery);
        return parcel;
    }

    /**
     * Method that applies the current theme to the activity
     * @hide
     */
    void applyTheme() {
        Theme theme = ThemeManager.getCurrentTheme(this);
        theme.setBaseTheme(this, false);

        //- ActionBar
        View v = getActionBar().getCustomView().findViewById(R.id.customtitle_title);
        theme.setTextColor(this, (TextView)v, "action_bar_text_color"); //$NON-NLS-1$
        v = findViewById(R.id.ab_button1);
        theme.setImageDrawable(this, (ImageView)v, "ic_config_drawable"); //$NON-NLS-1$
        // ContentView
        theme.setBackgroundDrawable(
                this, getWindow().getDecorView(), "background_drawable"); //$NON-NLS-1$
        //- StatusBar
        v = findViewById(R.id.search_status);
        theme.setBackgroundDrawable(this, v, "statusbar_drawable"); //$NON-NLS-1$
        v = findViewById(R.id.search_status_found_items);
        theme.setTextColor(this, (TextView)v, "action_bar_text_color"); //$NON-NLS-1$
        v = findViewById(R.id.search_status_query_terms);
        theme.setTextColor(this, (TextView)v, "action_bar_text_color"); //$NON-NLS-1$

        //ListView
        if (this.mSearchListView.getAdapter() != null) {
            ((SearchResultAdapter)this.mSearchListView.getAdapter()).notifyDataSetChanged();
        }
        this.mSearchListView.setDivider(
                theme.getDrawable(this, "horizontal_divider_drawable")); //$NON-NLS-1$
        this.mSearchListView.invalidate();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String category = MimeTypeHelper.MimeTypeCategory.names()[i];
        SearchResultAdapter adapter = ((SearchResultAdapter) this.mSearchListView.getAdapter());
        if (adapter != null) {
            adapter.setMimeFilter(category);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //ignore
    }
}

