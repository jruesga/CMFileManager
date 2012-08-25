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
import android.os.AsyncTask;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.adapters.TwoColumnsMenuListAdapter;
import com.cyanogenmod.explorer.console.RelaunchableException;
import com.cyanogenmod.explorer.listeners.OnRequestRefreshListener;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.ui.widgets.NavigationView;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.ExceptionUtil;
import com.cyanogenmod.explorer.util.SelectionHelper;

import java.io.File;

/**
 * A class that wraps a dialog for showing the list of actions that
 * the user can do.
 */
public class ActionsDialog implements OnItemClickListener, OnItemLongClickListener {

    private AlertDialog mDialog;
    private ListView mListView;
    private final NavigationView mNavigationView;
    private final FileSystemObject mFso;

    private OnRequestRefreshListener mOnRequestRefreshListener;

    /**
     * Constructor of <code>ActionsDialog</code>.
     *
     * @param context The current context
     * @param navigationView The current navigation view
     */
    public ActionsDialog(Context context, NavigationView navigationView) {
        super();

        //Save the data
        this.mNavigationView = navigationView;
        this.mFso = null;

        //Initialize dialog
        init(context, R.id.mnu_actions_global);
    }

    /**
     * Constructor of <code>ActionsDialog</code>.
     *
     * @param context The current context
     * @param navigationView The current navigation view
     * @param fso The file system object associated
     */
    public ActionsDialog(Context context, NavigationView navigationView, FileSystemObject fso) {
        super();

        //Save the data
        this.mNavigationView = navigationView;
        this.mFso = fso;

        //Initialize dialog
        init(context, R.id.mnu_actions_fso);
    }

    /**
     * Method that initializes the dialog.
     *
     * @param context The current context
     * @param group The group of action menus to show
     */
    private void init(Context context, int group) {
        //Create the menu adapter
        TwoColumnsMenuListAdapter adapter =
                new TwoColumnsMenuListAdapter(context, R.menu.actions, group);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        configureMenu(adapter.getMenu());

        //Create the list view
        this.mListView = new ListView(context);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.mListView.setLayoutParams(params);
        this.mListView.setAdapter(adapter);

        //Create the dialog
        this.mDialog = DialogHelper.createDialog(
                                        context,
                                        R.drawable.ic_holo_light_actions,
                                        R.string.actions_dialog_title,
                                        this.mListView);
    }

    /**
     * Method that sets the listener for communicate a refresh request.
     *
     * @param onRequestRefreshListener The request refresh listener
     */
    public void setOnRequestRefreshListener(OnRequestRefreshListener onRequestRefreshListener) {
        this.mOnRequestRefreshListener = onRequestRefreshListener;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {

        //Retrieve the menu item
        MenuItem menuItem = ((TwoColumnsMenuListAdapter)parent.getAdapter()).getItemById((int)id);

        //What action was selected?
        switch ((int)id) {
            //- Create new object
            case R.id.mnu_actions_new_directory:
            case R.id.mnu_actions_new_file:
                showInputNameDialog(menuItem);
                return;

            //- Select/Deselect
            case R.id.mnu_actions_select:
            case R.id.mnu_actions_deselect:
                this.mNavigationView.toogleSelection(this.mFso);
                break;
            case R.id.mnu_actions_select_all:
                this.mNavigationView.selectedAll();
                break;
            case R.id.mnu_actions_deselect_all:
                this.mNavigationView.deselectedAll();
                break;

            default:
                break;
        }

        //Dismiss the dialog
        this.mDialog.dismiss();
    }

    /**
     * Method that show a new dialog for input a name.
     *
     * @param menuItem The item menu associated
     */
    private void showInputNameDialog(final MenuItem menuItem) {
        //Hide the dialog
        this.mDialog.hide();

        //Show the input name dialog
        final InputNameDialog inputNameDialog =
                new InputNameDialog(
                        this.mDialog.getContext(),
                        this.mNavigationView.getFiles(),
                        menuItem.getTitle().toString());
        inputNameDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void onCancel(DialogInterface dialog) {
                //Show the menu again
                ActionsDialog.this.mDialog.show();
            }
        });
        inputNameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void onDismiss(DialogInterface dialog) {
                //Retrieve the name an execute the action
                try {
                    String name = inputNameDialog.getName();
                    createNewFileSystemObject(menuItem.getItemId(), name);

                } finally {
                    ActionsDialog.this.mDialog.dismiss();
                }
            }
        });
        inputNameDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        DialogHelper.showToast(
                this.mDialog.getContext(),
                ((TextView)view).getText().toString(),
                Toast.LENGTH_SHORT);
        return true;
    }

    /**
     * Method that create the a new file system object.
     *
     * @param menuId The menu identifier (need to determine the fso type)
     * @param name The name of the file system object
     */
    private void createNewFileSystemObject(final int menuId, final String name) {

        //Create the absolute file name
        File newFso = new File(this.mNavigationView.getCurrentDir(), name);
        final String newName = newFso.getAbsolutePath();

        try {
            switch (menuId) {
                case R.id.mnu_actions_new_directory:
                    CommandHelper.createDirectory(this.mDialog.getContext(), newName, null);
                    break;
                case R.id.mnu_actions_new_file:
                    CommandHelper.createFile(this.mDialog.getContext(), newName, null);
                    break;
                default:
                    break;
            }

            //Operation complete. Show refresh
            if (ActionsDialog.this.mOnRequestRefreshListener != null) {
                FileSystemObject file = null;
                try {
                    file = CommandHelper.getFileInfo(this.mDialog.getContext(), newName, null);
                } catch (Throwable ex2) {
                    /**NON BLOCK**/
                }
                ActionsDialog.this.mOnRequestRefreshListener.onRequestRefresh(file);
            }

        } catch (Throwable ex) {
            //Capture the exception
            if (ex instanceof RelaunchableException) {
                ExceptionUtil.attachAsyncTask(ex, new AsyncTask<Object, Integer, Boolean>() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    @SuppressWarnings("synthetic-access")
                    protected Boolean doInBackground(Object... params) {
                        //Operation complete. Show refresh
                        if (ActionsDialog.this.mOnRequestRefreshListener != null) {
                            FileSystemObject file = null;
                            try {
                                file =
                                    CommandHelper.getFileInfo(
                                            ActionsDialog.this.mDialog.getContext(), newName, null);
                            } catch (Throwable ex2) {
                                /**NON BLOCK**/
                            }
                            ActionsDialog.this.mOnRequestRefreshListener.onRequestRefresh(file);
                        }
                        return Boolean.TRUE;
                    }

                });
            }
            ExceptionUtil.translateException(this.mNavigationView.getContext(), ex);
        }
    }

    /**
     * Method that configure the menu to show according the actual information,
     * the kind of request, the file selection, the mount point, ...
     *
     * @param menu The menu to configure
     */
    private void configureMenu(Menu menu) {
        //- Select/Deselect -> Only when the fso is selected or unselected
        if (this.mFso != null) {
            boolean selected =
                    SelectionHelper.isFileSystemObjectSelected(
                            this.mNavigationView.getSelectedFiles(),
                            this.mFso);
            menu.removeItem(selected ? R.id.mnu_actions_select : R.id.mnu_actions_deselect);
        }
    }
}
