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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.adapters.CheckableListAdapter;
import com.cyanogenmod.explorer.console.Console;
import com.cyanogenmod.explorer.console.ConsoleBuilder;
import com.cyanogenmod.explorer.console.shell.NonPriviledgeConsole;
import com.cyanogenmod.explorer.console.shell.PrivilegedConsole;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.util.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that wraps a dialog for showing list of consoles for choosing one.
 * This class lets the user to set the default console.
 */
public class ChooseConsoleDialog implements OnItemClickListener {

    private static final String TAG = "ChooseConsoleDialog"; //$NON-NLS-1$

    private final Context mContext;
    private final AlertDialog mDialog;

    //List of implemented consoles
    private static final Class<?>[] CONSOLES =
            { NonPriviledgeConsole.class, PrivilegedConsole.class };

    /**
     * Constructor of <code>ChooseConsoleDialog</code>.
     *
     * @param context The current context
     */
    public ChooseConsoleDialog(Context context) {
        super();

        // Save the context
        this.mContext = context;

        //Retrieve the consoles
        Console console = null;
        try {
            console = ConsoleBuilder.getConsole(context);
        } catch (Exception e) {
            Log.e(TAG, "No console allocated", e); //$NON-NLS-1$
        }
        String[] consoles = context.getResources().getStringArray(R.array.implemented_consoles);
        List<CheckableListAdapter.CheckableItem> items =
                new ArrayList<CheckableListAdapter.CheckableItem>(consoles.length);
        int cc = consoles.length;
        for (int i = 0; i < cc; i++) {
            boolean checked = false;
            if (console != null) {
                checked = CONSOLES[i].getCanonicalName().compareTo(
                        console.getClass().getCanonicalName()) == 0;
            }
            items.add(new CheckableListAdapter.CheckableItem(consoles[i], true, checked));
        }
        CheckableListAdapter adapter = new CheckableListAdapter(context, items);

        //Create the list view
        ListView listView = new ListView(context);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        listView.setLayoutParams(params);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        //Create the dialog
        this.mDialog = DialogHelper.createDialog(
                                        context,
                                        R.drawable.ic_holo_light_console,
                                        R.string.choose_console_dialog_title,
                                        listView);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            ((CheckableListAdapter)((ListView)parent).getAdapter()).setSelectedItem(position);
        } catch (Exception e) {/**NON BLOCK**/}
        this.mDialog.dismiss();
        boolean ret = false;
        Boolean superuser = Boolean.FALSE;
        switch (position) {
            case 0:
                //Change to non-privileged console
                ret = ConsoleBuilder.changeToNonPrivilegedConsole(this.mContext);
                break;

            case 1:
                //Change to privileged console
                ret = ConsoleBuilder.changeToPrivilegedConsole(this.mContext);
                superuser = Boolean.TRUE;
                break;
            default:
                break;
        }

        //Show a message
        if (!ret) {
            DialogHelper.createErrorDialog(
                    this.mContext,
                    R.string.msgs_console_change_failed).show();
        } else {
            try {
                Preferences.savePreference(
                        ExplorerSettings.SETTINGS_SUPERUSER_MODE, superuser, true);
            } catch (Throwable ex) {
                Log.w(TAG, "Can't save console preference", ex); //$NON-NLS-1$
            }
        }
    }

}
