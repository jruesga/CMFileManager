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
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.adapters.MenuSettingsAdapter;
import com.cyanogenmod.explorer.adapters.SimpleMenuListAdapter;
import com.cyanogenmod.explorer.console.Console;
import com.cyanogenmod.explorer.console.ConsoleAllocException;
import com.cyanogenmod.explorer.console.ConsoleBuilder;
import com.cyanogenmod.explorer.listeners.OnHistoryListener;
import com.cyanogenmod.explorer.listeners.OnRequestRefreshListener;
import com.cyanogenmod.explorer.model.DiskUsage;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.model.History;
import com.cyanogenmod.explorer.model.MountPoint;
import com.cyanogenmod.explorer.parcelables.HistoryNavigable;
import com.cyanogenmod.explorer.parcelables.NavigationInfoParcelable;
import com.cyanogenmod.explorer.parcelables.NavigationViewInfoParcelable;
import com.cyanogenmod.explorer.parcelables.SearchInfoParcelable;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Identifiable;
import com.cyanogenmod.explorer.preferences.NavigationLayoutMode;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.ui.dialogs.ActionsDialog;
import com.cyanogenmod.explorer.ui.dialogs.ChooseConsoleDialog;
import com.cyanogenmod.explorer.ui.dialogs.FilesystemInfoDialog;
import com.cyanogenmod.explorer.ui.dialogs.FilesystemInfoDialog.OnMountListener;
import com.cyanogenmod.explorer.ui.widgets.Breadcrumb;
import com.cyanogenmod.explorer.ui.widgets.NavigationCustomTitleView;
import com.cyanogenmod.explorer.ui.widgets.NavigationView;
import com.cyanogenmod.explorer.ui.widgets.NavigationView.OnNavigationRequestMenuListener;
import com.cyanogenmod.explorer.ui.widgets.NavigationView.OnNavigationSelectionChangedListener;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.FileHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main explorer activity.
 */
public class NavigationActivity extends Activity
    implements OnHistoryListener, OnRequestRefreshListener,
    OnNavigationRequestMenuListener, OnNavigationSelectionChangedListener {

    private static final String TAG = "NavigationActivity"; //$NON-NLS-1$

    /**
     * Intent code for request a bookmark selection.
     */
    public static final int INTENT_REQUEST_BOOKMARK = 10001;

    /**
     * Intent code for request a history selection.
     */
    public static final int INTENT_REQUEST_HISTORY = 20001;

    /**
     * Intent code for request a search.
     */
    public static final int INTENT_REQUEST_SEARCH = 30001;


    /**
     * Constant for extra information about selected bookmark.
     */
    public static final String EXTRA_BOOKMARK_SELECTION =
            "extra_bookmark_selection"; //$NON-NLS-1$

    /**
     * Constant for extra information about selected history entry.
     */
    public static final String EXTRA_HISTORY_ENTRY_SELECTION =
            "extra_history_entry_selection"; //$NON-NLS-1$

    /**
     * Constant for extra information about selected search entry.
     */
    public static final String EXTRA_SEARCH_ENTRY_SELECTION =
            "extra_search_entry_selection"; //$NON-NLS-1$

    /**
     * Constant for extra information about last search data.
     */
    public static final String EXTRA_SEARCH_LAST_SEARCH_DATA =
            "extra_search_last_search_data"; //$NON-NLS-1$


    //The key for the state data
    private static final String NAVIGATION_STATE = "explorer_navigation_state";  //$NON-NLS-1$



    private NavigationView[] mNavigationViews;
    private List<History> mHistory;

    private int mCurrentNavigationView;

    private ViewGroup mActionBar;

    private boolean mExitFlag = false;

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

        //Set the main layout of the activity
        setContentView(R.layout.navigation);

        //Get the navigation views
        this.mNavigationViews = new NavigationView[1];
        this.mCurrentNavigationView = 0;
        //- 0
        this.mNavigationViews[0] = (NavigationView)findViewById(R.id.navigation_view);
        this.mNavigationViews[0].setId(0);

        //Initialize activity
        init();

        //Initialize action bars
        initTitleActionBar();
        initStatusActionBar();

        //Save state
        super.onCreate(state);

        //Restore state
        boolean restore = false;
        if (state != null) {
            restore = restoreState(state);
        }

        //Initialize navigation
        for (int i = 0; i < this.mNavigationViews.length; i++) {
            if  (this.mCurrentNavigationView == i) {
                initNavigation(i, restore);
            } else {
                initNavigation(i, true);
            }
        }

        //Check the intent action
        checkIntent(getIntent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        //Initialize navigation (is restore sure)
        initNavigation(this.mCurrentNavigationView, true);

        //Check the intent action
        checkIntent(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        if (ExplorerApplication.DEBUG) {
            Log.d(TAG, "NavigationActivity.onDestroy"); //$NON-NLS-1$
        }

        //All destroy. Continue
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (ExplorerApplication.DEBUG) {
            Log.d(TAG, "NavigationActivity.onSaveInstanceState"); //$NON-NLS-1$
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
        //Create the parcel information
        NavigationInfoParcelable parcel = new NavigationInfoParcelable();
        //-
        NavigationViewInfoParcelable[] navigationViews =
                new NavigationViewInfoParcelable[this.mNavigationViews.length];
        for (int i = 0; i < this.mNavigationViews.length; i++) {
            navigationViews[i] = this.mNavigationViews[i].onSaveState();
        }
        parcel.setNavigationViews(navigationViews);
        //-
        parcel.setHistoryInfo(this.mHistory);

        //Save the parcel information
        state.putParcelable(NAVIGATION_STATE, parcel);
    }

    /**
     * Method that restore the instance of the activity.
     *
     * @param state The previous state of the activity
     * @return boolean If the restoration was successful
     */
    private boolean restoreState(Bundle state) {
        try {
            //Restore the instance
            NavigationInfoParcelable info = state.getParcelable(NAVIGATION_STATE);
            if (info != null) {
                //-
                NavigationViewInfoParcelable[] navigationViews = info.getNavigationViews();
                if (navigationViews == null) {
                    return false;
                }
                this.mNavigationViews = new NavigationView[navigationViews.length];
                for (int i = 0; i < navigationViews.length; i++) {
                    if (navigationViews[i] != null) {
                        this.mNavigationViews[i].onRestoreState(navigationViews[i]);
                    }
                }

                //-
                this.mHistory = info.getHistoryInfo();

                //Restored
                return true;
            }

        } catch (Throwable ex) {
            Log.e(TAG, "The state can't be restored", ex); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Method that returns the current navigation view.
     *
     * @return NavigationView The current navigation view
     */
    public NavigationView getCurrentNavigationView() {
        return getNavigationView(this.mCurrentNavigationView);
    }

    /**
     * Method that returns the current navigation view.
     *
     * @param viewId The view to return
     * @return NavigationView The current navigation view
     */
    public NavigationView getNavigationView(int viewId) {
        return this.mNavigationViews[viewId];
    }

    /**
     * Method that initializes the activity.
     */
    private void init() {
        this.mHistory = new ArrayList<History>();
    }

    /**
     * Method that initializes the titlebar of the activity.
     */
    private void initTitleActionBar() {
        //Inflate the view and associate breadcrumb
        NavigationCustomTitleView title =
                (NavigationCustomTitleView)getLayoutInflater().inflate(
                        R.layout.navigation_view_customtitle, null, false);
        title.setOnHistoryListener(this);
        Breadcrumb breadcrumb = (Breadcrumb)title.findViewById(R.id.breadcrumb_view);
        for (int i = 0; i < this.mNavigationViews.length; i++) {
            this.mNavigationViews[i].setBreadcrumb(breadcrumb);
            this.mNavigationViews[i].setOnHistoryListener(this);
            this.mNavigationViews[i].setOnNavigationSelectionChangedListener(this);
            this.mNavigationViews[i].setOnNavigationOnRequestMenuListener(this);
            this.mNavigationViews[i].setCustomTitle(title);
        }

        //Configure the action bar options
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.bg_holo_titlebar));
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getActionBar().setCustomView(title);
    }

    /**
     * Method that initializes the statusbar of the activity.
     */
    private void initStatusActionBar() {
        //Performs a width calculation of buttons. Buttons exceeds the width
        //of the action bar should be hidden
        //This application not use android ActionBar because the application
        //make uses of the title and bottom areas, and wants to force to show
        //the overflow button (without care of physical buttons)
        this.mActionBar = (ViewGroup)findViewById(R.id.navigation_actionbar);
        this.mActionBar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom, int oldLeft,
                    int oldTop, int oldRight, int oldBottom) {
                //Get the width of the action bar
                int w = v.getMeasuredWidth();

                //Wake through children calculation his dimensions
                int bw = (int)getResources().getDimension(R.dimen.default_buttom_width);
                int cw = 0;
                final ViewGroup abView = ((ViewGroup)v);
                for (int i = 0; i < abView.getChildCount(); i++) {
                    View child = abView.getChildAt(i);
                    child.setVisibility(cw + bw > w ? View.INVISIBLE : View.VISIBLE);
                    cw += bw;
                }
            }
        });


    }

    /**
     * Method that initializes the navigation.
     *
     * @param viewId The navigation view identifier where apply the navigation
     * @param restore Initialize from a restore info
     */
    private void initNavigation(final int viewId, final boolean restore) {
        final NavigationView navigationView = getNavigationView(viewId);
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                //Create the default console (from the preferences)
                try {
                    Console console = ConsoleBuilder.createDefaultConsole(NavigationActivity.this);
                    if (console == null) {
                        throw new ConsoleAllocException("console == null"); //$NON-NLS-1$
                    }
                } catch (Throwable ex) {
                    //Show exception and exists
                    Log.e(TAG, getString(R.string.msgs_cant_create_console), ex);
                    DialogHelper.showToast(
                            NavigationActivity.this,
                            R.string.msgs_cant_create_console, Toast.LENGTH_LONG);
                    finish();
                }

                //Is necessary navigate?
                if (!restore) {
                    //Load the preference initial directory
                    String initialDir =
                            Preferences.getSharedPreferences().getString(
                                ExplorerSettings.SETTINGS_INITIAL_DIR.getId(),
                                (String)ExplorerSettings.SETTINGS_INITIAL_DIR.getDefaultValue());

                    //Ensure initial is an absolute directory
                    try {
                        initialDir =
                                CommandHelper.getAbsolutePath(
                                        NavigationActivity.this, initialDir, null);
                    } catch (Throwable e) {
                        Log.e(TAG, "Resolve of initital directory fails", e); //$NON-NLS-1$
                        String msg =
                                getString(
                                        R.string.msgs_settings_invalid_initial_directory,
                                        initialDir);
                        DialogHelper.showToast(NavigationActivity.this, msg, Toast.LENGTH_SHORT);
                        initialDir = FileHelper.ROOT_DIRECTORY;
                    }

                    //Change the current directory to the preference initial directory
                    navigationView.changeCurrentDir(initialDir);
                }
            }
        });
    }

    /**
     * Method that verifies the intent passed to the activity, and checks
     * if a request is made like Search.
     *
     * @param intent The intent to check
     */
    private void checkIntent(Intent intent) {
        //Search action
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Intent searchIntent = new Intent(this, SearchActivity.class);
            searchIntent.setAction(Intent.ACTION_SEARCH);
            //- SearchActivity.EXTRA_SEARCH_DIRECTORY
            searchIntent.putExtra(
                    SearchActivity.EXTRA_SEARCH_DIRECTORY,
                    getCurrentNavigationView().getCurrentDir());
            //- SearchManager.APP_DATA
            if (intent.getBundleExtra(SearchManager.APP_DATA) != null) {
                Bundle bundle = new Bundle();
                bundle.putAll(intent.getBundleExtra(SearchManager.APP_DATA));
                searchIntent.putExtra(SearchManager.APP_DATA, bundle);
            }
            //-- SearchManager.QUERY
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
                searchIntent.putExtra(SearchManager.QUERY, query);
            }
            //- android.speech.RecognizerIntent.EXTRA_RESULTS
            ArrayList<String> extraResults =
                    intent.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
            if (extraResults != null) {
                searchIntent.putStringArrayListExtra(
                        android.speech.RecognizerIntent.EXTRA_RESULTS, extraResults);
            }
            startActivityForResult(searchIntent, INTENT_REQUEST_SEARCH);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showOverflowPopUp(findViewById(R.id.ab_overflow));
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (checkBackAction()) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case android.R.id.home:
              if ((getActionBar().getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP)
                      == ActionBar.DISPLAY_HOME_AS_UP) {
                  checkBackAction();
              }
              return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }

    /**
     * Method invoked when an action item is clicked.
     *
     * @param view The button pushed
     */
    public void onActionBarItemClick(View view) {
        switch (view.getId()) {
            //######################
            //Navigation Custom Title
            //######################
            case R.id.ab_configuration:
                //Show navigation view configuration toolbar
                getCurrentNavigationView().getCustomTitle().showConfigurationView();
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setHomeButtonEnabled(true);
                break;
            case R.id.ab_close:
                //Hide navigation view configuration toolbar
                getCurrentNavigationView().getCustomTitle().hideConfigurationView();
                break;

            //######################
            //Breadcrumb Actions
            //######################
            case R.id.ab_filesystem_info:
                //Show information of the filesystem
                MountPoint mp = getCurrentNavigationView().getBreadcrumb().getMountPointInfo();
                DiskUsage du = getCurrentNavigationView().getBreadcrumb().getDiskUsageInfo();
                showMountPointInfo(mp, du);
                break;

            //######################
            //Navigation view options
            //######################
            case R.id.ab_sort_mode:
                showSettingsPopUp(view,
                        Arrays.asList(new ExplorerSettings[]{ExplorerSettings.SETTINGS_SORT_MODE}));
                break;
            case R.id.ab_layout_mode:
                showSettingsPopUp(view,
                        Arrays.asList(
                                new ExplorerSettings[]{ExplorerSettings.SETTINGS_LAYOUT_MODE}));
                break;
            case R.id.ab_view_options:
                showSettingsPopUp(view,
                        Arrays.asList(new ExplorerSettings[]{
                                ExplorerSettings.SETTINGS_SHOW_DIRS_FIRST,
                                ExplorerSettings.SETTINGS_SHOW_HIDDEN,
                                ExplorerSettings.SETTINGS_SHOW_SYSTEM,
                                ExplorerSettings.SETTINGS_SHOW_SYMLINKS}));
                break;

            //######################
            //Action Bar buttons
            //######################
            case R.id.ab_actions:
                ActionsDialog dialog = new ActionsDialog(this, getCurrentNavigationView());
                dialog.setOnRequestRefreshListener(this);
                dialog.show();
                break;

            case R.id.ab_bookmarks:
                openBookmarks();
                break;

            case R.id.ab_history:
                openHistory();
                break;

            case R.id.ab_search:
                openSearch();
                break;

            case R.id.ab_overflow:
                showOverflowPopUp(view);
                break;

            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_REQUEST_BOOKMARK:
                if (resultCode == RESULT_OK) {
                    //Change
                    getCurrentNavigationView().changeCurrentDir(
                            data.getStringExtra(EXTRA_BOOKMARK_SELECTION));
                }
                break;

            case INTENT_REQUEST_HISTORY:
                if (resultCode == RESULT_OK) {
                    //Change
                    History history =
                            (History)data.getSerializableExtra(EXTRA_HISTORY_ENTRY_SELECTION);
                    navigateToHistory(history);
                }
                break;

            case INTENT_REQUEST_SEARCH:
                if (resultCode == RESULT_OK) {
                    //Change directory?
                    String newDir = data.getStringExtra(EXTRA_SEARCH_ENTRY_SELECTION);
                    SearchInfoParcelable searchInfo =
                            data.getParcelableExtra(EXTRA_SEARCH_LAST_SEARCH_DATA);
                    if (newDir != null) {
                        //Goto to new directory
                        getCurrentNavigationView().changeCurrentDir(newDir, searchInfo);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    SearchInfoParcelable searchInfo =
                            data.getParcelableExtra(EXTRA_SEARCH_LAST_SEARCH_DATA);
                    if (searchInfo != null && searchInfo.isSuccessNavigation()) {
                        //Navigate to previous history
                        back();
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewHistory(HistoryNavigable navigable) {
        //Recollect information about current status
        History history = new History(this.mHistory.size(), navigable);
        this.mHistory.add(history);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCheckHistory() {
        //Need to show HomeUp Button
        boolean enabled = this.mHistory != null && this.mHistory.size() > 0;
        getActionBar().setDisplayHomeAsUpEnabled(enabled);
        getActionBar().setHomeButtonEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestRefresh(FileSystemObject fso) {
        this.getCurrentNavigationView().refresh(fso);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSelectionChanged(NavigationView navView, List<FileSystemObject> selectedItems) {
        //FIXME Implement selection
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestMenu(NavigationView navView, FileSystemObject item) {
        ActionsDialog dialog = new ActionsDialog(this, getCurrentNavigationView(), item);
        dialog.setOnRequestRefreshListener(this);
        dialog.show();
    }

    /**
     * Method that shows a popup with a menu associated a {@link ExplorerSetting}.
     *
     * @param anchor The action button that was pressed
     * @param settings The array of settings associated with the action button
     */
    private void showSettingsPopUp(View anchor, List<ExplorerSettings> settings) {
        //Create the adapter
        final MenuSettingsAdapter adapter = new MenuSettingsAdapter(this, settings);

        //Create a show the popup menu
        final ListPopupWindow popup = DialogHelper.createListPopupWindow(this, adapter, anchor);
        popup.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ExplorerSettings setting =
                        ((MenuSettingsAdapter)parent.getAdapter()).getSetting(position);
                final int value = ((MenuSettingsAdapter)parent.getAdapter()).getId(position);
                popup.dismiss();
                try {
                    if (setting.compareTo(ExplorerSettings.SETTINGS_LAYOUT_MODE) == 0) {
                        //Need to change the layout
                        getCurrentNavigationView().changeViewMode(
                                NavigationLayoutMode.fromId(value));
                    } else {
                        //Save and refresh
                        if (setting.getDefaultValue() instanceof Enum<?>) {
                            //Enumeration
                            Preferences.savePreference(setting, new Identifiable() {
                                @Override
                                public int getId() {
                                    return value;
                                }
                            }, false);
                        } else {
                            //Boolean
                            boolean newval =
                                    Preferences.getSharedPreferences().
                                        getBoolean(
                                            setting.getId(),
                                            ((Boolean)setting.getDefaultValue()).booleanValue());
                            Preferences.savePreference(setting, new Boolean(!newval), false);
                        }
                        getCurrentNavigationView().refresh();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error applying navigation option", e); //$NON-NLS-1$
                    getCurrentNavigationView().post(new Runnable() {
                        @Override
                        public void run() {
                            DialogHelper.showToast(
                                    NavigationActivity.this,
                                    R.string.msgs_settings_save_failure, Toast.LENGTH_SHORT);
                        }
                    });

                } finally {
                    adapter.dispose();
                    getCurrentNavigationView().getCustomTitle().restoreView();
                }

            }
        });
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                adapter.dispose();
            }
        });
        popup.show();
    }

    /**
     * Method that shows a popup with the activity main menu.
     *
     * @param anchor The action button that was pressed
     */
    private void showOverflowPopUp(View anchor) {
        SimpleMenuListAdapter adapter = new SimpleMenuListAdapter(this, R.menu.navigation);
        Menu menu = adapter.getMenu();
        boolean hasActionBarMenus = false;
        int removed = 0;
        for (int i = 0, j = this.mActionBar.getChildCount() - 1;
                i < this.mActionBar.getChildCount(); i++, j--) {

            View child = this.mActionBar.getChildAt(i);
            boolean visible = child.getVisibility() == View.VISIBLE;
            if (visible) {
                menu.removeItem(menu.getItem(j).getItemId());
                removed++;
            } else {
                hasActionBarMenus = true;
            }
        }

        //Hide separator
        if (!hasActionBarMenus) {
            menu.removeItem(menu.getItem(this.mActionBar.getChildCount() - removed).getItemId());
        }

        final ListPopupWindow popup = DialogHelper.createListPopupWindow(this, adapter, anchor);
        popup.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(
                    final AdapterView<?> parent, final View v, final int position, final long id) {

                final int itemId = (int)id;
                getCurrentNavigationView().post(new Runnable() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public void run() {
                        popup.dismiss();
                        switch (itemId) {
                            case R.id.mnu_exit:
                                //Exit
                                finish();
                                break;

                            case R.id.mnu_settings:
                                //Settings
                                break;

                            case R.id.mnu_console:
                                //Console
                                ChooseConsoleDialog dialog =
                                    new ChooseConsoleDialog(NavigationActivity.this);
                                dialog.show();
                                break;

                            case R.id.mnu_history:
                                //History
                                openHistory();
                                popup.dismiss();
                                break;

                            case R.id.mnu_bookmarks:
                                //Bookmarks
                                openBookmarks();
                                popup.dismiss();
                                break;

                            case R.id.mnu_search:
                                //Search
                                openSearch();
                                popup.dismiss();
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        });
        popup.show();
    }

    /**
     * Method that show the information of a filesystem mount point.
     *
     * @param mp The mount point info
     * @param du The disk usage of the mount point
     */
    private void showMountPointInfo(MountPoint mp, DiskUsage du) {
        //Has mount point info?
        if (mp == null) {
            //There is no information
            AlertDialog alert =
                    DialogHelper.createWarningDialog(this, R.string.filesystem_info_warning_msg);
            alert.show();
            return;
        }

        //Show a the filesystem info dialog
        FilesystemInfoDialog dialog = new FilesystemInfoDialog(this, mp, du);
        dialog.setOnMountListener(new OnMountListener() {
            @Override
            public void onRemount(MountPoint mountPoint) {
                //Update the statistics of breadcrumb, only if mount point is the same
                Breadcrumb breadcrumb = getCurrentNavigationView().getBreadcrumb();
                if (breadcrumb.getMountPointInfo().compareTo(mountPoint) == 0) {
                    breadcrumb.updateMountPointInfo();
                }
            }
        });
        dialog.show();
    }

    /**
     * Method that checks the action that must be realized when the
     * back button is pushed.
     *
     * @return boolean Indicates if the action must be intercepted
     */
    private boolean checkBackAction() {
        //Check if the configuration view is showing. In this case back
        //action must be "close configuration"
        if (getCurrentNavigationView().getCustomTitle().isConfigurationViewShowing()) {
            getCurrentNavigationView().getCustomTitle().restoreView();
            return true;
        }

        //Do back operation over the navigation history
        boolean flag = this.mExitFlag;
        this.mExitFlag = !back();

        //Check if there no history and if the user was advised in the last back action
        if (this.mExitFlag && this.mExitFlag != flag) {
            //Communicate the user that the next time the application will be closed
            DialogHelper.showToast(this, R.string.msgs_push_again_to_exit, Toast.LENGTH_SHORT);
            return true;
        }

        //Back action not applied
        return !this.mExitFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSearchRequested() {
        Bundle bundle = new Bundle();
        bundle.putString(
                SearchActivity.EXTRA_SEARCH_DIRECTORY,
                getCurrentNavigationView().getCurrentDir());
        startSearch(Preferences.getLastSearch(), true, bundle, false);
        return true;
    }

    /**
     * Method that returns the history size.
     *
     * @return int The history size
     */
    public int getHistorySize() {
        return this.mHistory.size();
    }

    /**
     * Method that returns the history list.
     *
     * @return List<History> The history list
     */
    public List<History> getHistory() {
        return new ArrayList<History>(this.mHistory);
    }

    /**
     * Method that navigates to the passed history reference.
     *
     * @param history The history reference
     * @return boolean
     */
    public boolean navigateToHistory(History history) {
        try {
            //Gets the history
            History realHistory = this.mHistory.get(history.getPosition());

            //Navigate to item. Check what kind of history is
            if (realHistory.getItem() instanceof NavigationViewInfoParcelable) {
                //Navigation
                NavigationViewInfoParcelable info =
                        (NavigationViewInfoParcelable)realHistory.getItem();
                int viewId = info.getId();
                NavigationView view = getNavigationView(viewId);
                view.onRestoreState(info);

                //TODO Change to navigation view (viewpager)

            } else if (realHistory.getItem() instanceof SearchInfoParcelable) {
                //Search (open search with the search results)
                SearchInfoParcelable info = (SearchInfoParcelable)realHistory.getItem();
                Intent searchIntent = new Intent(this, SearchActivity.class);
                searchIntent.setAction(SearchActivity.ACTION_RESTORE);
                searchIntent.putExtra(SearchActivity.EXTRA_SEARCH_RESTORE, (Parcelable)info);
                startActivityForResult(searchIntent, INTENT_REQUEST_SEARCH);
            } else {
                //The type is unknown
                throw new IllegalArgumentException("Unknown history type"); //$NON-NLS-1$
            }

            //Remove the old history
            for (int i = this.mHistory.size() - 1; i >= realHistory.getPosition(); i--) {
                this.mHistory.remove(i);
            }
            if (this.mHistory.size() == 0) {
                getActionBar().setDisplayHomeAsUpEnabled(false);
                getActionBar().setHomeButtonEnabled(false);
            }

            //Navigate
            return true;

        } catch (Throwable ex) {
            if (history != null) {
                Log.e(TAG,
                        String.format("Failed to navigate to history %d: %s", //$NON-NLS-1$
                                new Integer(history.getPosition()),
                                history.getItem().getTitle()), ex);
            } else {
                Log.e(TAG,
                        String.format("Failed to navigate to history: null", ex)); //$NON-NLS-1$
            }
            getCurrentNavigationView().post(new Runnable() {
                @Override
                public void run() {
                    DialogHelper.showToast(
                            NavigationActivity.this,
                            R.string.msgs_history_unknown, Toast.LENGTH_LONG);
                }
            });

            //Not change directory
            return false;
        }
    }

    /**
     * Method that request a back action over the navigation history.
     *
     * @return boolean If a back action was applied
     */
    public boolean back() {
        //Extract a history from the
        if (this.mHistory.size() > 0) {
            //Navigate to history
            return navigateToHistory(this.mHistory.get(this.mHistory.size() - 1));
        }

        //Nothing to apply
        return false;
    }

    /**
     * Method that opens the bookmarks activity.
     */
    private void openBookmarks() {
        Intent bookmarksIntent = new Intent(this, BookmarksActivity.class);
        bookmarksIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivityForResult(bookmarksIntent, INTENT_REQUEST_BOOKMARK);
    }

    /**
     * Method that opens the history activity.
     */
    private void openHistory() {
        Intent historyIntent = new Intent(this, HistoryActivity.class);
        historyIntent.putExtra(HistoryActivity.EXTRA_HISTORY_LIST, (Serializable)this.mHistory);
        historyIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivityForResult(historyIntent, INTENT_REQUEST_HISTORY);
    }

    /**
     * Method that opens the search activity.
     */
    private void openSearch() {
        onSearchRequested();
    }

}
