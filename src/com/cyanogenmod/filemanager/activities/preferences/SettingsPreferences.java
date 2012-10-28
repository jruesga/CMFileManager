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

package com.cyanogenmod.filemanager.activities.preferences;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cyanogenmod.filemanager.FileManagerApplication;
import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.activities.ChangeLogActivity;
import com.cyanogenmod.filemanager.console.ConsoleBuilder;
import com.cyanogenmod.filemanager.preferences.AccessMode;
import com.cyanogenmod.filemanager.preferences.FileManagerSettings;
import com.cyanogenmod.filemanager.preferences.ObjectStringIdentifier;
import com.cyanogenmod.filemanager.preferences.Preferences;
import com.cyanogenmod.filemanager.providers.RecentSearchesContentProvider;
import com.cyanogenmod.filemanager.util.DialogHelper;

import java.util.List;

/**
 * The {@link SettingsPreferences} preferences
 */
public class SettingsPreferences extends PreferenceActivity {

    private static final boolean DEBUG = false;

    private static final String LOG_TAG = "SettingsPreferences"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize action bars
        initTitleActionBar();
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
        title.setText(R.string.pref);
        title.setContentDescription(getString(R.string.pref));
        getActionBar().setCustomView(customTitle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);

        // Retrieve the about header
        Header aboutHeader = target.get(target.size()-1);
        try {
            String appver =
                    this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            aboutHeader.summary = getString(R.string.pref_about_summary, appver);
        } catch (Exception e) {
            aboutHeader.summary = getString(R.string.pref_about_summary, ""); //$NON-NLS-1$
        }
        aboutHeader.intent = new Intent(getApplicationContext(), ChangeLogActivity.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case android.R.id.home:
              finish();
              return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }

    /**
     * A class that manages the commons options of the application
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        private CheckBoxPreference mCaseSensitiveSort;
        private ListPreference mFreeDiskSpaceWarningLevel;
        private CheckBoxPreference mComputeFolderStatistics;
//        private CheckBoxPreference mUseFlinger;
        private ListPreference mAccessMode;
        private CheckBoxPreference mDebugTraces;

        /**
         * @hide
         */
        boolean mLoaded = false;

        private final OnPreferenceChangeListener mOnChangeListener =
                new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                boolean ret = true;

                String key = preference.getKey();
                if (DEBUG) {
                    Log.d(LOG_TAG,
                        String.format("New value for %s: %s",  //$NON-NLS-1$
                                key,
                                String.valueOf(newValue)));
                }

                // Disk usage warning level
                if (FileManagerSettings.SETTINGS_DISK_USAGE_WARNING_LEVEL.
                        getId().compareTo(key) == 0) {
                    String value = (String)newValue;
                    preference.setSummary(
                            getResources().getString(
                                    R.string.pref_disk_usage_warning_level_summary, value));
                }

                // Access mode
                else if (FileManagerSettings.SETTINGS_ACCESS_MODE.getId().compareTo(key) == 0) {
                    Activity activity = GeneralPreferenceFragment.this.getActivity();

                    String value = (String)newValue;
                    AccessMode oldMode = FileManagerApplication.getAccessMode();
                    AccessMode newMode = AccessMode.fromId(value);
                    if (oldMode.compareTo(newMode) != 0) {
                        // The mode was changes. Change the console
                        if (newMode.compareTo(AccessMode.ROOT) == 0) {
                            if (!ConsoleBuilder.changeToPrivilegedConsole(
                                    activity.getApplicationContext())) {
                                value = String.valueOf(oldMode.ordinal());
                                ret = false;
                            }
                        } else {
                            if (!ConsoleBuilder.changeToNonPrivilegedConsole(
                                    activity.getApplicationContext())) {
                                value = String.valueOf(oldMode.ordinal());
                                ret = false;
                            }
                        }
                    }

                    int valueId = Integer.valueOf(value).intValue();
                    String[] summary = getResources().getStringArray(
                            R.array.access_mode_summaries);
                                        preference.setSummary(summary[valueId]);
                }

                // Notify the change (only if fragment is loaded. Default values are loaded
                // while not in loaded mode)
                if (GeneralPreferenceFragment.this.mLoaded && ret) {
                    Intent intent = new Intent(FileManagerSettings.INTENT_SETTING_CHANGED);
                    intent.putExtra(
                            FileManagerSettings.EXTRA_SETTING_CHANGED_KEY, preference.getKey());
                    getActivity().sendBroadcast(intent);
                }

                return ret;
            }
        };

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Change the preference manager
            getPreferenceManager().setSharedPreferencesName(Preferences.SETTINGS_FILENAME);
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            // Add the preferences
            addPreferencesFromResource(R.xml.preferences_general);

            // Case sensitive sort
            this.mCaseSensitiveSort =
                    (CheckBoxPreference)findPreference(
                            FileManagerSettings.SETTINGS_CASE_SENSITIVE_SORT.getId());
            this.mCaseSensitiveSort.setOnPreferenceChangeListener(this.mOnChangeListener);

            //Disk usage warning level
            this.mFreeDiskSpaceWarningLevel =
                    (ListPreference)findPreference(
                            FileManagerSettings.SETTINGS_DISK_USAGE_WARNING_LEVEL.getId());
            this.mFreeDiskSpaceWarningLevel.setOnPreferenceChangeListener(this.mOnChangeListener);
            String defaultValue = ((String)FileManagerSettings.
                                SETTINGS_DISK_USAGE_WARNING_LEVEL.getDefaultValue());
            String value = Preferences.getSharedPreferences().getString(
                                FileManagerSettings.SETTINGS_DISK_USAGE_WARNING_LEVEL.getId(),
                                defaultValue);
            this.mOnChangeListener.onPreferenceChange(this.mFreeDiskSpaceWarningLevel, value);

            // Compute folder statistics
            this.mComputeFolderStatistics =
                    (CheckBoxPreference)findPreference(
                            FileManagerSettings.SETTINGS_COMPUTE_FOLDER_STATISTICS.getId());
            this.mComputeFolderStatistics.setOnPreferenceChangeListener(this.mOnChangeListener);

            // Use flinger
//            this.mUseFlinger =
//                    (CheckBoxPreference)findPreference(
//                            FileManagerSettings.SETTINGS_USE_FLINGER.getId());
//            this.mUseFlinger.setOnPreferenceChangeListener(this.mOnChangeListener);

            // Access mode
            this.mAccessMode =
                    (ListPreference)findPreference(
                            FileManagerSettings.SETTINGS_ACCESS_MODE.getId());
            this.mAccessMode.setOnPreferenceChangeListener(this.mOnChangeListener);
            defaultValue = ((ObjectStringIdentifier)FileManagerSettings.
                                SETTINGS_ACCESS_MODE.getDefaultValue()).getId();
            value = Preferences.getSharedPreferences().getString(
                                FileManagerSettings.SETTINGS_ACCESS_MODE.getId(),
                                defaultValue);
            this.mOnChangeListener.onPreferenceChange(this.mAccessMode, value);

            // Capture Debug traces
            this.mDebugTraces =
                    (CheckBoxPreference)findPreference(
                            FileManagerSettings.SETTINGS_SHOW_TRACES.getId());
            this.mDebugTraces.setOnPreferenceChangeListener(this.mOnChangeListener);

            // Loaded
            this.mLoaded = true;
        }
    }

    /**
     * A class that manages the search options
     */
    public static class SearchPreferenceFragment extends PreferenceFragment {

        // Internal keys
        private static final String REMOVE_SEARCH_TERMS_KEY =
                                            "cm_filemanager_remove_saved_search_terms"; //$NON-NLS-1$

        private CheckBoxPreference mHighlightTerms;
        private CheckBoxPreference mShowRelevanceWidget;
        private ListPreference mSortSearchResultMode;
        private CheckBoxPreference mSaveSearchTerms;
        private Preference mRemoveSearchTerms;

        /**
         * @hide
         */
        boolean mLoaded = false;

        private final OnPreferenceChangeListener mOnChangeListener =
                new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String key = preference.getKey();
                if (DEBUG) {
                    Log.d(LOG_TAG,
                        String.format("New value for %s: %s",  //$NON-NLS-1$
                                key,
                                String.valueOf(newValue)));
                }

                // Saved search terms
                if (preference.getKey().compareTo(
                        FileManagerSettings.SETTINGS_SAVE_SEARCH_TERMS.getId()) == 0) {
                    if (!((Boolean)newValue).booleanValue()) {
                        // Remove search terms if saved search terms
                        // is not active by the user
                        clearRecentSearchTerms();
                    }

                // Sort search result mode
                } else if (FileManagerSettings.SETTINGS_SORT_SEARCH_RESULTS_MODE.
                        getId().compareTo(key) == 0) {
                    int value = Integer.valueOf((String)newValue).intValue();
                    String[] summary = getResources().getStringArray(
                            R.array.sort_search_results_mode_labels);
                    preference.setSummary(summary[value]);
                }

                // Notify the change (only if fragment is loaded. Default values are loaded
                // while not in loaded mode)
                if (SearchPreferenceFragment.this.mLoaded) {
                    Intent intent = new Intent(FileManagerSettings.INTENT_SETTING_CHANGED);
                    intent.putExtra(
                            FileManagerSettings.EXTRA_SETTING_CHANGED_KEY, preference.getKey());
                    getActivity().sendBroadcast(intent);
                }

                return true;
            }
        };

        private final OnPreferenceClickListener mOnClickListener =
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().compareTo(REMOVE_SEARCH_TERMS_KEY) == 0) {
                    // Remove search terms
                    clearRecentSearchTerms();

                    // Advise the user
                    DialogHelper.showToast(
                            getActivity(),
                            getActivity().getString(R.string.pref_remove_saved_search_terms_msg),
                            Toast.LENGTH_SHORT);
                }
                return false;
            }
        };

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Change the preference manager
            getPreferenceManager().setSharedPreferencesName(Preferences.SETTINGS_FILENAME);
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            this.mLoaded = false;

            // Add the preferences
            addPreferencesFromResource(R.xml.preferences_search);

            // Highlight terms
            this.mHighlightTerms =
                    (CheckBoxPreference)findPreference(
                            FileManagerSettings.SETTINGS_HIGHLIGHT_TERMS.getId());
            this.mHighlightTerms.setOnPreferenceChangeListener(this.mOnChangeListener);

            // Relevance widget
            this.mShowRelevanceWidget =
                    (CheckBoxPreference)findPreference(
                            FileManagerSettings.SETTINGS_SHOW_RELEVANCE_WIDGET.getId());
            this.mShowRelevanceWidget.setOnPreferenceChangeListener(this.mOnChangeListener);

            // Sort search result mode
            this.mSortSearchResultMode =
                    (ListPreference)findPreference(
                            FileManagerSettings.SETTINGS_SORT_SEARCH_RESULTS_MODE.getId());
            this.mSortSearchResultMode.setOnPreferenceChangeListener(this.mOnChangeListener);
            String defaultValue = ((ObjectStringIdentifier)FileManagerSettings.
                                    SETTINGS_SORT_SEARCH_RESULTS_MODE.getDefaultValue()).getId();
            String value = Preferences.getSharedPreferences().getString(
                                    FileManagerSettings.SETTINGS_SORT_SEARCH_RESULTS_MODE.getId(),
                                    defaultValue);
            this.mOnChangeListener.onPreferenceChange(this.mSortSearchResultMode, value);

            // Saved search terms
            this.mSaveSearchTerms =
                    (CheckBoxPreference)findPreference(
                            FileManagerSettings.SETTINGS_SAVE_SEARCH_TERMS.getId());
            this.mSaveSearchTerms.setOnPreferenceChangeListener(this.mOnChangeListener);

            // Remove search terms
            this.mRemoveSearchTerms = findPreference(REMOVE_SEARCH_TERMS_KEY);
            this.mRemoveSearchTerms.setOnPreferenceClickListener(this.mOnClickListener);

            // Loaded
            this.mLoaded = true;
        }

        /**
         * Method that removes the recent suggestions on search activity
         * @hide
         */
        void clearRecentSearchTerms() {
            SearchRecentSuggestions suggestions =
                    new SearchRecentSuggestions(getActivity(),
                            RecentSearchesContentProvider.AUTHORITY,
                            RecentSearchesContentProvider.MODE);
            suggestions.clearHistory();
            Preferences.setLastSearch(null);
        }
    }

}
