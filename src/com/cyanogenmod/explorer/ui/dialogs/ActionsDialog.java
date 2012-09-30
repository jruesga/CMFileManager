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
import com.cyanogenmod.explorer.listeners.OnRequestRefreshListener;
import com.cyanogenmod.explorer.listeners.OnSelectionListener;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.ui.policy.ActionsPolicy;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.FileHelper;
import com.cyanogenmod.explorer.util.SelectionHelper;

/**
 * A class that wraps a dialog for showing the list of actions that
 * the user can do.
 */
public class ActionsDialog implements OnItemClickListener, OnItemLongClickListener {

    private final Context mContext;
    private final boolean mGlobal;

    private AlertDialog mDialog;
    private ListView mListView;
    private final FileSystemObject mFso;

    private OnRequestRefreshListener mOnRequestRefreshListener;

    private OnSelectionListener mOnSelectionListener;

    /**
     * Constructor of <code>ActionsDialog</code>.
     *
     * @param context The current context
     * @param fso The file system object associated
     * @param global If the menu to display will be the global one (Global actions)
     */
    public ActionsDialog(Context context, FileSystemObject fso, boolean global) {
        super();

        //Save the data
        this.mFso = fso;
        this.mContext = context;
        this.mGlobal = global;

        //Initialize dialog
        init(context, global ? R.id.mnu_actions_global : R.id.mnu_actions_fso);
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
     * Method that sets the listener for requesting selection data
     *
     * @param onSelectionListener The request selection data  listener
     */
    public void setOnSelectionListener(OnSelectionListener onSelectionListener) {
        this.mOnSelectionListener = onSelectionListener;
    }

    /**
     * Method that shows the dialog.
     */
    public void show() {
        TwoColumnsMenuListAdapter adapter =
                (TwoColumnsMenuListAdapter)this.mListView.getAdapter();
        configureMenu(adapter.getMenu());
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
                if (this.mOnSelectionListener != null) {
                    showInputNameDialog(menuItem);
                }
                return;

            //- Delete
            case R.id.mnu_actions_delete:
                this.mDialog.dismiss();
                ActionsPolicy.removeFileSystemObject(
                        this.mContext, this.mFso, this.mOnRequestRefreshListener);
                return;

            //- Delete
            case R.id.mnu_actions_refresh:
                this.mDialog.dismiss();
                if (this.mOnRequestRefreshListener != null) {
                    this.mOnRequestRefreshListener.onRequestRefresh(null); //Refresh all
                }
                return;

            //- Select/Deselect
            case R.id.mnu_actions_select:
            case R.id.mnu_actions_deselect:
                if (this.mOnSelectionListener != null) {
                    this.mOnSelectionListener.onToggleSelection(this.mFso);
                }
                break;
            case R.id.mnu_actions_select_all:
                if (this.mOnSelectionListener != null) {
                    this.mOnSelectionListener.onSelectAllVisibleItems();
                }
                break;
            case R.id.mnu_actions_deselect_all:
                if (this.mOnSelectionListener != null) {
                    this.mOnSelectionListener.onDeselectAllVisibleItems();
                }
                break;

            //- Properties
            case R.id.mnu_actions_properties:
            case R.id.mnu_actions_properties_current_folder:
                ActionsPolicy.showPropertiesDialog(
                        this.mContext, this.mFso, this.mOnRequestRefreshListener);
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
                        this.mContext,
                        this.mOnSelectionListener.onRequestSelectedFiles(),
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
                this.mContext,
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
        switch (menuId) {
            case R.id.mnu_actions_new_directory:
                ActionsPolicy.createNewDirectory(
                        this.mContext, name,
                        this.mOnSelectionListener, this.mOnRequestRefreshListener);
                break;
            case R.id.mnu_actions_new_file:
                ActionsPolicy.createNewFile(
                        this.mContext, name,
                        this.mOnSelectionListener, this.mOnRequestRefreshListener);
                break;
            default:
                break;
        }
    }

    /**
     * Method that configure the menu to show according the actual information,
     * the kind of request, the file selection, the mount point, ...
     *
     * @param menu The menu to configure
     */
    private void configureMenu(Menu menu) {
        // Check actions that needs a valid reference
        if (!this.mGlobal && this.mFso != null) {
            //- Select/Deselect -> Only one of them
            if (this.mOnSelectionListener != null) {
                boolean selected =
                        SelectionHelper.isFileSystemObjectSelected(
                                this.mOnSelectionListener.onRequestSelectedFiles(),
                                this.mFso);
                menu.removeItem(selected ? R.id.mnu_actions_select : R.id.mnu_actions_deselect);
            } else {
                // Remove both menus
                menu.removeItem(R.id.mnu_actions_select);
                menu.removeItem(R.id.mnu_actions_deselect);
            }

            //- Open/Open with -> Only when the fso is not a folder or is not a system file
            if (FileHelper.isDirectory(this.mFso) || FileHelper.isSystemFile(this.mFso)) {
                menu.removeItem(R.id.mnu_actions_open);
                menu.removeItem(R.id.mnu_actions_open_with);
            }
        }
    }
}
