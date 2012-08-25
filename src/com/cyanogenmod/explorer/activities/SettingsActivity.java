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
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.R;

import java.util.List;

/**
 * The settings preferences activity.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SettingsActivity"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (ExplorerApplication.DEBUG) {
            Log.d(TAG, "SettingsActivity.onCreate"); //$NON-NLS-1$
        }

        //Initialize action bars
        initTitleActionBar();

        //Save state
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        if (ExplorerApplication.DEBUG) {
            Log.d(TAG, "SettingsActivity.onDestroy"); //$NON-NLS-1$
        }

        //All destroy. Continue
        super.onDestroy();
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    private void initTitleActionBar() {
        //Configure the action bar options
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.bg_holo_titlebar));
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        //Load the header settings
        //FIXME Add headers
//        loadHeadersFromResource(R.xml.settings, target);
    }

}
