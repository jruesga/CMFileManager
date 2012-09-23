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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.adapters.HistoryAdapter;
import com.cyanogenmod.explorer.model.History;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity for show navigation history.
 */
public class HistoryActivity extends Activity implements OnItemClickListener {

    private static final String TAG = "HistoryActivity"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    /**
     * Intent extra parameter for the history data.
     */
    public static final String EXTRA_HISTORY_LIST = "extra_history_list";  //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle state) {
        if (DEBUG) {
            Log.d(TAG, "NavigationActivity.onCreate"); //$NON-NLS-1$
        }

        //Request features
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Set in transition
        overridePendingTransition(R.anim.translate_to_right_in, R.anim.hold_out);

        //Set the main layout of the activity
        setContentView(R.layout.history);

        //Initialize action bars and data
        initTitleActionBar();
        initHistory();

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
        title.setText(R.string.history);
        title.setContentDescription(getString(R.string.history));
        getActionBar().setCustomView(customTitle);
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    @SuppressWarnings("unchecked")
    private void initHistory() {
        List<History> history = (List<History>)getIntent().getSerializableExtra(EXTRA_HISTORY_LIST);
        if (history.isEmpty()) {
            View msg = findViewById(R.id.history_empty_msg);
            msg.setVisibility(View.VISIBLE);
            return;
        }

        //Show inverted history
        final List<History> adapterList = new ArrayList<History>(history);
        Collections.reverse(adapterList);

        ListView lv = (ListView)findViewById(R.id.history_listview);
        HistoryAdapter adapter = new HistoryAdapter(this, adapterList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
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
        History history = ((HistoryAdapter)parent.getAdapter()).getItem(position);
        back(false, history);
    }

    /**
     * Method that returns to previous activity and.
     *
     * @param canceled Indicates if the activity was canceled
     * @param history The selected history
     */
    private void back(final boolean canceled, final History history) {
        Intent intent =  new Intent();
        if (canceled) {
            setResult(RESULT_CANCELED, intent);
        } else {
            intent.putExtra(NavigationActivity.EXTRA_HISTORY_ENTRY_SELECTION, history);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}

