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

package com.cyanogenmod.explorer.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.adapters.SearchResultAdapter;
import com.cyanogenmod.explorer.adapters.SearchResultAdapter.OnRequestMenuListener;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.model.Query;
import com.cyanogenmod.explorer.model.SearchResult;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.NavigationSortMode;
import com.cyanogenmod.explorer.preferences.ObjectStringIdentifier;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.preferences.SearchSortResultMode;
import com.cyanogenmod.explorer.util.ExceptionUtil;
import com.cyanogenmod.explorer.util.FileHelper;
import com.cyanogenmod.explorer.util.SearchHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A class for paint the resulting file system object of a search.
 */
public class SearchResultDrawingAsyncTask extends AsyncTask<Object, Integer, Boolean> {

    private final ListView mSearchListView;
    private final ProgressBar mSearchWaiting;
    private final List<FileSystemObject> mFiles;
    private final Query mQueries;
    private boolean mRunning;
    private final OnRequestMenuListener mOnRequestMenuListener;

    /**
     * Constructor of <code>SearchResultDrawingAsyncTask</code>.
     *
     * @param searchListView The {@link ListView} reference
     * @param searchWaiting A {@link ProgressBar} reference
     * @param onRequestMenuListener The listener for display the actions menu
     * @param files The files to draw
     * @param queries The terms of the search
     */
    public SearchResultDrawingAsyncTask(
            ListView searchListView, ProgressBar searchWaiting,
            OnRequestMenuListener onRequestMenuListener,
            List<FileSystemObject> files, Query queries) {
        super();
        this.mSearchListView = searchListView;
        this.mSearchWaiting = searchWaiting;
        this.mOnRequestMenuListener = onRequestMenuListener;
        this.mFiles = files;
        this.mQueries = queries;
        this.mRunning = false;
    }

    /**
     * Method that returns if there is a task running.
     *
     * @return boolean If there is a task running
     */
    public boolean isRunning() {
        return this.mRunning;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            //Running
            this.mRunning = true;
            showHideWaiting(true);

            // Get sort mode
            String defaultValue = ((ObjectStringIdentifier)ExplorerSettings.
                    SETTINGS_SORT_SEARCH_RESULTS_MODE.getDefaultValue()).getId();
            String value = Preferences.getSharedPreferences().getString(
                                ExplorerSettings.SETTINGS_SORT_SEARCH_RESULTS_MODE.getId(),
                                defaultValue);
            SearchSortResultMode mode = SearchSortResultMode.fromId(value);

            //Process all the data
            final List<SearchResult> result =
                    SearchHelper.convertToResults(
                            FileHelper.applyUserPreferences(this.mFiles, true),
                            this.mQueries);
            if (mode.compareTo(SearchSortResultMode.NAME) == 0) {
                Collections.sort(result, new Comparator<SearchResult>() {
                    @Override
                    public int compare(SearchResult lhs, SearchResult rhs) {
                        return FileHelper.doCompare(
                                lhs.getFso(), rhs.getFso(), NavigationSortMode.NAME_ASC);
                    }
                });
            } else if (mode.compareTo(SearchSortResultMode.RELEVANCE) == 0) {
                Collections.sort(result);
            }

            this.mSearchListView.post(new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                  //Add list to the listview
                    if (SearchResultDrawingAsyncTask.this.mSearchListView.getAdapter() != null) {
                        ((SearchResultAdapter)SearchResultDrawingAsyncTask.this.
                                mSearchListView.getAdapter()).clear();
                    }

                    // Recreate the adapter
                    SearchResultAdapter adapter = new SearchResultAdapter(
                            SearchResultDrawingAsyncTask.this.mSearchListView.getContext(),
                            result,
                            R.layout.search_item,
                            SearchResultDrawingAsyncTask.this.mQueries);
                    adapter.setOnRequestMenuListener(
                            SearchResultDrawingAsyncTask.this.mOnRequestMenuListener);

                    // Configure the listview
                    SearchResultDrawingAsyncTask.this.mSearchListView.setAdapter(adapter);
                    SearchResultDrawingAsyncTask.this.mSearchListView.setSelection(0);
                }
            });

            //Operation complete
            return Boolean.TRUE;

        } catch (Throwable ex) {
            //Capture and show the exception
            ExceptionUtil.translateException(this.mSearchListView.getContext(), ex);

        } finally {
            this.mRunning = false;
            showHideWaiting(false);
        }

        //Something went wrong
        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled(Boolean result) {
        this.mRunning = false;
        super.onCancelled(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCancelled() {
        this.mRunning = false;
        super.onCancelled();
    }

    /**
     * Method that shows or hides the waiting icon.
     *
     * @param show If the waiting icon must be shown
     */
    private void showHideWaiting(final boolean show) {
        if (this.mSearchWaiting != null) {
            this.mSearchWaiting.post(new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    SearchResultDrawingAsyncTask.this.mSearchWaiting.setVisibility(
                            show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

}
