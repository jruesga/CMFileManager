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

package com.cyanogenmod.explorer.ui.policy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.RelaunchableException;
import com.cyanogenmod.explorer.listeners.OnRequestRefreshListener;
import com.cyanogenmod.explorer.listeners.OnSelectionListener;
import com.cyanogenmod.explorer.model.Bookmark;
import com.cyanogenmod.explorer.model.Bookmark.BOOKMARK_TYPE;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.preferences.Bookmarks;
import com.cyanogenmod.explorer.ui.dialogs.AssociationsDialog;
import com.cyanogenmod.explorer.ui.dialogs.FsoPropertiesDialog;
import com.cyanogenmod.explorer.ui.dialogs.MessageProgressDialog;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.ExceptionUtil;
import com.cyanogenmod.explorer.util.ExceptionUtil.OnRelaunchCommandResult;
import com.cyanogenmod.explorer.util.FileHelper;
import com.cyanogenmod.explorer.util.MimeTypeHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A class with the convenience methods for resolve actions
 */
public final class ActionsPolicy {

    /**
     * A class that holds a relationship between a source {@link File} and
     * his destination {@link File}
     */
    public static class LinkedResources implements Comparable<LinkedResources> {
        final File mSrc;
        final File mDst;

        /**
         * Constructor of <code>LinkedResources</code>
         *
         * @param src The source file system object
         * @param dst The destination file system object
         */
        public LinkedResources(File src, File dst) {
            super();
            this.mSrc = src;
            this.mDst = dst;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(LinkedResources another) {
            return this.mSrc.compareTo(another.mSrc);
        }
    }

    /**
     * An interface for using in conjunction with AsyncTask for have
     * a
     */
    private interface BackgroundCallable {
        /**
         * Method that returns the resource identifier of the icon of the dialog
         *
         * @return int The resource identifier of the icon of the dialog
         */
        int getDialogIcon();

        /**
         * Method that returns the resource identifier of the title of the dialog
         *
         * @return int The resource identifier of the title of the dialog
         */
        int getDialogTitle();

        /**
         * Method invoked when need to update the progress of the dialog
         *
         * @return Spanned The text to show in the progress
         */
        Spanned requestProgress();

        /**
         * The method where the operation is done in background
         *
         * @param params The parameters
         * @throws Throwable If the operation failed, must be launch and exception
         */
        void doInBackground(Object... params) throws Throwable;

        /**
         * Method invoked when the operation was successfully
         */
        void onSuccess();
    }

    /**
     * A task class for run operations in the background. It uses a dialog while
     * perform the operation.
     *
     * @see BackgroundCallable
     */
    private static class BackgroundAsyncTask
            extends AsyncTask<Object, Void, Throwable> {

        private final Context mCtx;
        private final BackgroundCallable mCallable;
        private MessageProgressDialog mDialog;

        /**
         * Constructor of <code>BackgroundAsyncTask</code>
         *
         * @param ctx The current context
         * @param callable The {@link BackgroundCallable} interface
         */
        public BackgroundAsyncTask(Context ctx, BackgroundCallable callable) {
            super();
            this.mCtx = ctx;
            this.mCallable = callable;
        }

        @Override
        protected void onPreExecute() {
            // Create the waiting dialog while doing some stuff on background
            this.mDialog = new MessageProgressDialog(
                    this.mCtx,
                    R.drawable.ic_holo_light_operation,
                    R.string.waiting_dialog_copying_title,
                    R.string.waiting_dialog_msg,
                    false);
            Spanned progress = this.mCallable.requestProgress();
            this.mDialog.setProgress(progress);
            this.mDialog.show();
        }

        @Override
        protected Throwable doInBackground(Object... params) {
            try {
                this.mCallable.doInBackground(params);

                // Success
                return null;

            } catch (Throwable ex) {
                // Capture the exception
                return ex;
            }
        }

        @Override
        protected void onPostExecute(Throwable result) {
            // Close the waiting dialog
            this.mDialog.dismiss();

            // Check the result (no relaunch, this is responsibility of callable doInBackground)
            if (result != null) {
                ExceptionUtil.translateException(this.mCtx, result, false, false);
            } else {
                //Operation complete.
                this.mCallable.onSuccess();
            }
        }

        /**
         * @hide
         */
        void onRequestProgress() {
            Spanned progress = this.mCallable.requestProgress();
            this.mDialog.setProgress(progress);
        }
    }


    private static final String TAG = "ActionPolicy"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    /**
     * Constructor of <code>ActionsPolicy</code>.
     */
    private ActionsPolicy() {
        super();
    }

    /**
     * Method that show a {@link Toast} with the content description of a {@link FileSystemObject}.
     *
     * @param ctx The current context
     * @param fso The file system object
     */
    public static void showContentDescription(final Context ctx, final FileSystemObject fso) {
        String contentDescription = fso.getFullPath();
        Toast.makeText(ctx, contentDescription, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method that show a new dialog for show {@link FileSystemObject} properties.
     *
     * @param ctx The current context
     * @param fso The file system object
     * @param onRequestRefreshListener The listener for request a refresh after properties
     * of the {@link FileSystemObject} were changed (optional)
     */
    public static void showPropertiesDialog(
            final Context ctx, final FileSystemObject fso,
            final OnRequestRefreshListener onRequestRefreshListener) {
        //Show a the filesystem info dialog
        final FsoPropertiesDialog dialog = new FsoPropertiesDialog(ctx, fso);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dlg) {
                // Any change?
                if (dialog.isHasChanged()) {
                    if (onRequestRefreshListener != null) {
                        onRequestRefreshListener.onRequestRefresh(dialog.getFso());
                    }
                }
            }
        });
        dialog.show();
    }

    /**
     * Method that opens a {@link FileSystemObject} with the default registered application
     * by the system, or ask the user for select a registered application.
     *
     * @param ctx The current context
     * @param fso The file system object
     * @param choose If allow the user to select the application to open with
     */
    public static void openFileSystemObject(
            final Context ctx, final FileSystemObject fso, final boolean choose) {
        try {
            // Create the intent to
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);

            // Obtain the mime/type and passed it to intent
            String mime = MimeTypeHelper.getMimeType(ctx, fso);
            File file = new File(fso.getFullPath());
            if (mime != null) {
                intent.setDataAndType(Uri.fromFile(file), mime);
            } else {
                intent.setData(Uri.fromFile(file));
            }

            // Resolve the intent
            resolveIntent(
                    ctx,
                    intent,
                    choose,
                    R.drawable.ic_holo_light_open,
                    R.string.associations_dialog_openwith_title,
                    R.string.associations_dialog_openwith_action,
                    true);

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }

    /**
     * Method that sends a {@link FileSystemObject} with the default registered application
     * by the system, or ask the user for select a registered application.
     *
     * @param ctx The current context
     * @param fso The file system object
     */
    public static void sendFileSystemObject(
            final Context ctx, final FileSystemObject fso) {
        try {
            // Create the intent to
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(MimeTypeHelper.getMimeType(ctx, fso));
            Uri uri = Uri.fromFile(new File(fso.getFullPath()));
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            // Resolve the intent
            resolveIntent(
                    ctx,
                    intent,
                    false,
                    R.drawable.ic_holo_light_send,
                    R.string.associations_dialog_sendwith_title,
                    R.string.associations_dialog_sendwith_action,
                    false);

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }

    /**
     * Method that resolve
     *
     * @param ctx The current context
     * @param intent The intent to resolve
     * @param choose If allow the user to select the application to select the registered
     * application. If no preferred app or more than one exists the dialog is shown.
     * @param icon The icon of the dialog
     * @param title The title of the dialog
     * @param action The button title of the dialog
     * @param allowPreferred If allow the user to mark the selected app as preferred
     */
    private static void resolveIntent(
            Context ctx, Intent intent, boolean choose,
            int icon, int title, int action, boolean allowPreferred) {
        //Retrieve the activities that can handle the file
        final PackageManager packageManager = ctx.getPackageManager();
        if (DEBUG) {
            intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        }
        List<ResolveInfo> info =
                packageManager.
                    queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // Retrieve the preferred activity that can handle the file
        final ResolveInfo mPreferredInfo = packageManager.resolveActivity(intent, 0);

        // Now we have the list of activities that can handle the file. The next steps are:
        //
        // 1.- If choose, then show open with dialog
        // 2.- If info size == 0. No default application, then show open with dialog
        // 3.- If !choose, seek inside our database the default activity for the extension
        //     and open the file with this application
        // 4.- If no default activity saved, then use system default

        // No registered application
        if (info.size() == 0) {
            Toast.makeText(ctx, R.string.msgs_not_registered_app, Toast.LENGTH_SHORT).show();
            return;
        }

        // Is a simple open and we have an application that can handle the file?
        if (!choose &&
                ((mPreferredInfo  != null && mPreferredInfo.match != 0) || info.size() == 1)) {
            ctx.startActivity(intent);
            return;
        }

        // Otherwise, we have to show the open with dialog
        AssociationsDialog dialog =
                new AssociationsDialog(
                        ctx,
                        icon,
                        ctx.getString(title),
                        ctx.getString(action),
                        intent,
                        info,
                        mPreferredInfo,
                        allowPreferred);
        dialog.show();
    }

    /**
     * Method that adds the {@link FileSystemObject} to the bookmarks database.
     *
     * @param ctx The current context
     * @param fso The file system object
     */
    public static void addToBookmarks(final Context ctx, final FileSystemObject fso) {
        try {
            // Create the bookmark
            Bookmark bookmark =
                    new Bookmark(BOOKMARK_TYPE.USER_DEFINED, fso.getName(), fso.getFullPath());
            bookmark = Bookmarks.addBookmark(ctx, bookmark);
            if (bookmark == null) {
                // The operation fails
                Toast.makeText(
                        ctx,
                        R.string.msgs_operation_failure,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Success
                Toast.makeText(
                        ctx,
                        R.string.bookmarks_msgs_add_success,
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }

    /**
     * Method that create the a new file system object.
     *
     * @param ctx The current context
     * @param name The name of the file to be created
     * @param onSelectionListener The selection listener (required)
     * @param onRequestRefreshListener The listener for request a refresh after the new
     * file was created (option)
     */
    public static void createNewFile(
            final Context ctx, final String name,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {
        createNewFileSystemObject(ctx, name, false, onSelectionListener, onRequestRefreshListener);
    }

    /**
     * Method that create the a new folder system object.
     *
     * @param ctx The current context
     * @param name The name of the file to be created
     * @param onSelectionListener The selection listener (required)
     * @param onRequestRefreshListener The listener for request a refresh after the new
     * folder was created (option)
     */
    public static void createNewDirectory(
            final Context ctx, final String name,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {
        createNewFileSystemObject(ctx, name, true, onSelectionListener, onRequestRefreshListener);
    }

    /**
     * Method that create the a new file system object.
     *
     * @param ctx The current context
     * @param name The name of the file to be created
     * @param folder If the new {@link FileSystemObject} to create is a folder (true) or a
     * file (false).
     * @param onSelectionListener The selection listener (required)
     * @param onRequestRefreshListener The listener for request a refresh after the new
     * folder was created (option)
     */
    private static void createNewFileSystemObject(
            final Context ctx, final String name, final boolean folder,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        //Create the absolute file name
        File newFso = new File(
                onSelectionListener.onRequestCurrentDir(), name);
        final String newName = newFso.getAbsolutePath();

        try {
            if (folder) {
                if (DEBUG) {
                    Log.d(TAG, String.format("Creating new directory: %s", newName)); //$NON-NLS-1$
                }
                CommandHelper.createDirectory(ctx, newName, null);
            } else {
                if (DEBUG) {
                    Log.d(TAG, String.format("Creating new file: %s", newName)); //$NON-NLS-1$
                }
                CommandHelper.createFile(ctx, newName, null);
            }

            //Operation complete. Show refresh
            if (onRequestRefreshListener != null) {
                FileSystemObject fso = null;
                try {
                    fso = CommandHelper.getFileInfo(ctx, newName, null);
                } catch (Throwable ex2) {
                    /**NON BLOCK**/
                }
                onRequestRefreshListener.onRequestRefresh(fso);
            }

        } catch (Throwable ex) {
            //Capture the exception
            if (ex instanceof RelaunchableException) {
                ExceptionUtil.attachAsyncTask(ex, new AsyncTask<Object, Integer, Boolean>() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected Boolean doInBackground(Object... params) {
                        //Operation complete. Show refresh
                        if (onRequestRefreshListener != null) {
                            FileSystemObject fso = null;
                            try {
                                fso =
                                    CommandHelper.getFileInfo(ctx, newName, null);
                            } catch (Throwable ex2) {
                                /**NON BLOCK**/
                            }
                            onRequestRefreshListener.onRequestRefresh(fso);
                        }
                        return Boolean.TRUE;
                    }

                });
            }
            ExceptionUtil.translateException(ctx, ex);
        }
    }

    /**
     * Method that remove an existing file system object.
     *
     * @param ctx The current context
     * @param fso The file system object to remove
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void removeFileSystemObject(
            final Context ctx, final FileSystemObject fso,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {
        // Generate an array and invoke internal method
        List<FileSystemObject> files = new ArrayList<FileSystemObject>(1);
        files.add(fso);
        removeFileSystemObjects(ctx, files, onSelectionListener, onRequestRefreshListener);
    }

    /**
     * Method that remove an existing file system object.
     *
     * @param ctx The current context
     * @param files The list of files to remove
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void removeFileSystemObjects(
            final Context ctx, final List<FileSystemObject> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Ask the user before remove
        AlertDialog dialog =DialogHelper.createYesNoDialog(
            ctx, R.string.actions_ask_undone_operation,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alertDialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        // Remove the items
                        removeFileSystemObjectsInBackground(
                                ctx,
                                files,
                                onSelectionListener,
                                onRequestRefreshListener);
                    }
                }
           });
        dialog.show();
    }

    /**
     * Method that remove an existing file system object in background.
     *
     * @param ctx The current context
     * @param files The list of files to remove
     * @param onSelectionListener The listener for obtain selection information (optional)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     * @hide
     */
    static void removeFileSystemObjectsInBackground(
            final Context ctx, final List<FileSystemObject> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Some previous checks prior to execute
        // 1.- Check the operation consistency (only if it is viable)
        if (onSelectionListener != null) {
            final String currentDirectory = onSelectionListener.onRequestCurrentDir();
            if (!checkRemoveConsistency(ctx, files, currentDirectory)) {
                return;
            }
        }

        // The callable interface
        final BackgroundCallable callable = new BackgroundCallable() {
            // The current items
            private int mCurrent = 0;
            final Context mCtx = ctx;
            final List<FileSystemObject> mFiles = files;
            final OnRequestRefreshListener mOnRequestRefreshListener = onRequestRefreshListener;

            final Object mSync = new Object();
            Throwable mCause;

            @Override
            public int getDialogTitle() {
                return R.string.waiting_dialog_deleting_title;
            }
            @Override
            public int getDialogIcon() {
                return R.drawable.ic_holo_light_operation;
            }

            @Override
            public Spanned requestProgress() {
                FileSystemObject fso = this.mFiles.get(this.mCurrent);

                // Return the current operation
                String progress =
                      this.mCtx.getResources().
                          getString(
                              R.string.waiting_dialog_deleting_msg,
                              fso.getFullPath());
                return Html.fromHtml(progress);
            }

            @Override
            public void onSuccess() {
                //Operation complete. Refresh
                if (this.mOnRequestRefreshListener != null) {
                  // The reference is not the same, so refresh the complete navigation view
                  this.mOnRequestRefreshListener.onRequestRefresh(null);
                }
                ActionsPolicy.showOperationSuccessMsg(ctx);
            }

            @Override
            public void doInBackground(Object... params) throws Throwable {
                this.mCause = null;

                // This method expect to receive
                // 1.- BackgroundAsyncTask
                BackgroundAsyncTask task = (BackgroundAsyncTask)params[0];

                for (int i = 0; i < this.mFiles.size(); i++) {
                    FileSystemObject fso = this.mFiles.get(i);

                    doOperation(this.mCtx, fso);

                    // Next file
                    this.mCurrent++;
                    if (this.mCurrent < this.mFiles.size()) {
                        task.onRequestProgress();
                    }
                }
            }

            /**
             * Method that copies on file to other location
             *
             * @param ctx The current context
             * @param src The source file
             * @param dst The destination file
             * @param move Indicates if the files are going to be moved (true) or copied (false)
             */
            @SuppressWarnings("hiding")
            private void doOperation(
                    final Context ctx, final FileSystemObject fso) throws Throwable {
                try {
                    // Remove the item
                    if (FileHelper.isDirectory(fso)) {
                        CommandHelper.deleteDirectory(ctx, fso.getFullPath(), null);
                    } else {
                        CommandHelper.deleteFile(ctx, fso.getFullPath(), null);
                    }
                } catch (Exception e) {
                    // Need to be relaunched?
                    if (e instanceof RelaunchableException) {
                        OnRelaunchCommandResult rl = new OnRelaunchCommandResult() {
                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onSuccess() {
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }

                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onFailed(Throwable cause) {
                                mCause = cause;
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }
                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onCancelled() {
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }
                        };

                        // Translate the exception (and wait for the result)
                        ExceptionUtil.translateException(ctx, e, false, true, rl);
                        synchronized (this.mSync) {
                            this.mSync.wait();
                        }

                        // Persist the exception?
                        if (this.mCause != null) {
                            // The exception must be elevated
                            throw this.mCause;
                        }

                    } else {
                        // The exception must be elevated
                        throw e;
                    }
                }

                // Check that the operation was completed retrieving the deleted fso
                boolean failed = false;
                try {
                    CommandHelper.getFileInfo(ctx, fso.getFullPath(), null);

                    // Failed. The file still exists
                    failed = true;

                } catch (Throwable e) {
                    // Operation complete successfully
                }
                if (failed) {
                    throw new ExecutionException(
                            String.format(
                                    "Failed to delete file: %s", fso.getFullPath())); //$NON-NLS-1$
                }
            }
        };
        final BackgroundAsyncTask task = new BackgroundAsyncTask(ctx, callable);

        // Execute background task
        task.execute(task);
    }

    /**
     * Method that remove an existing file system object.
     *
     * @param ctx The current context
     * @param fso The file system object
     * @param newName The new name of the object
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void renameFileSystemObject(
            final Context ctx,
            final FileSystemObject fso,
            final String newName,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Create the destination filename
        File dst = new File(fso.getParent(), newName);
        File src = new File(fso.getFullPath());

     // Create arguments
        LinkedResources linkRes = new LinkedResources(src, dst);
        List<LinkedResources> files = new ArrayList<ActionsPolicy.LinkedResources>(1);
        files.add(linkRes);

        // Internal copy
        copyOrMoveFileSystemObjects(
                ctx,
                true,
                files,
                onSelectionListener,
                onRequestRefreshListener);
    }

    /**
     * Method that copy an existing file system object.
     *
     * @param ctx The current context
     * @param fso The file system object
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void createCopyFileSystemObject(
            final Context ctx,
            final FileSystemObject fso,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Create a non-existing name
        List<FileSystemObject> curFiles = onSelectionListener.onRequestCurrentItems();
        String  newName = FileHelper.createNonExistingName(ctx, curFiles, fso);
        final File dst = new File(fso.getParent(), newName);
        File src = new File(fso.getFullPath());

        // Create arguments
        LinkedResources linkRes = new LinkedResources(src, dst);
        List<LinkedResources> files = new ArrayList<ActionsPolicy.LinkedResources>(1);
        files.add(linkRes);

        // Internal copy
        copyOrMoveFileSystemObjects(
                ctx,
                false,
                files,
                onSelectionListener,
                onRequestRefreshListener);
    }

    /**
     * Method that copy an existing file system object.
     *
     * @param ctx The current context
     * @param files The list of files to copy
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void copyFileSystemObjects(
            final Context ctx,
            final List<LinkedResources> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {
        // Internal copy
        copyOrMoveFileSystemObjects(
                ctx,
                false,
                files,
                onSelectionListener,
                onRequestRefreshListener);
    }

    /**
     * Method that copy an existing file system object.
     *
     * @param ctx The current context
     * @param files The list of files to move
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void moveFileSystemObjects(
            final Context ctx,
            final List<LinkedResources> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {
        // Internal move
        copyOrMoveFileSystemObjects(
                ctx,
                true,
                files,
                onSelectionListener,
                onRequestRefreshListener);
    }

    /**
     * Method that copy an existing file system object.
     *
     * @param ctx The current context
     * @param move Indicates if the files are going to be moved (true) or copied (false)
     * @param files The list of source/destination files to copy
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    private static void copyOrMoveFileSystemObjects(
            final Context ctx,
            final boolean move,
            final List<LinkedResources> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Some previous checks prior to execute
        // 1.- Listener can't not be null
        if (onSelectionListener == null) {
            AlertDialog dialog =
                    DialogHelper.createErrorDialog(ctx, R.string.msgs_illegal_argument);
            dialog.show();
            return;
        }
        // 2.- All the destination files must have the same parent and it must be currentDirectory,
        // and not be null
        final String currentDirectory = onSelectionListener.onRequestCurrentDir();
        for (int i = 0; i < files.size(); i++) {
            LinkedResources linkedRes = files.get(i);
            if (linkedRes.mSrc == null || linkedRes.mDst == null) {
                AlertDialog dialog =
                        DialogHelper.createErrorDialog(ctx, R.string.msgs_illegal_argument);
                dialog.show();
                return;
            }
            if (linkedRes.mDst.getParent() == null ||
                linkedRes.mDst.getParent().compareTo(currentDirectory) != 0) {
                AlertDialog dialog =
                        DialogHelper.createErrorDialog(ctx, R.string.msgs_illegal_argument);
                dialog.show();
                return;
            }
        }
        // 3.- Check the operation consistency
        if (move) {
            if (!checkMoveConsistency(ctx, files, currentDirectory)) {
                return;
            }
        }

        // The callable interface
        final BackgroundCallable callable = new BackgroundCallable() {
            // The current items
            private int mCurrent = 0;
            final Context mCtx = ctx;
            final boolean mMove = move;
            final List<LinkedResources> mFiles = files;
            final OnRequestRefreshListener mOnRequestRefreshListener = onRequestRefreshListener;

            final Object mSync = new Object();
            Throwable mCause;

            @Override
            public int getDialogTitle() {
                return this.mMove ?
                        R.string.waiting_dialog_moving_title :
                        R.string.waiting_dialog_copying_title;
            }
            @Override
            public int getDialogIcon() {
                return R.drawable.ic_holo_light_operation;
            }

            @Override
            public Spanned requestProgress() {
                File src = this.mFiles.get(this.mCurrent).mSrc;
                File dst = this.mFiles.get(this.mCurrent).mDst;

                // Return the current operation
                String progress =
                      this.mCtx.getResources().
                          getString(
                              this.mMove ?
                                   R.string.waiting_dialog_moving_msg :
                                   R.string.waiting_dialog_copying_msg,
                              src.getAbsolutePath(),
                              dst.getAbsolutePath());
                return Html.fromHtml(progress);
            }

            @Override
            public void onSuccess() {
                //Operation complete. Refresh
                if (this.mOnRequestRefreshListener != null) {
                  // The reference is not the same, so refresh the complete navigation view
                  this.mOnRequestRefreshListener.onRequestRefresh(null);
                }
                ActionsPolicy.showOperationSuccessMsg(ctx);
            }

            @Override
            public void doInBackground(Object... params) throws Throwable {
                this.mCause = null;

                // This method expect to receive
                // 1.- BackgroundAsyncTask
                BackgroundAsyncTask task = (BackgroundAsyncTask)params[0];

                for (int i = 0; i < this.mFiles.size(); i++) {
                    File src = this.mFiles.get(i).mSrc;
                    File dst = this.mFiles.get(i).mDst;

                    doOperation(this.mCtx, src, dst, this.mMove);

                    // Next file
                    this.mCurrent++;
                    if (this.mCurrent < this.mFiles.size()) {
                        task.onRequestProgress();
                    }
                }
            }

            /**
             * Method that copies on file to other location
             *
             * @param ctx The current context
             * @param src The source file
             * @param dst The destination file
             * @param move Indicates if the files are going to be moved (true) or copied (false)
             */
            @SuppressWarnings("hiding")
            private void doOperation(
                    Context ctx, File src, File dst, boolean move) throws Throwable {
                // If the source is the same as destiny then don't do the operation
                if (src.compareTo(dst) == 0) return;

                try {
                    // Copy or move?
                    if (move) {
                        CommandHelper.move(
                                ctx,
                                src.getAbsolutePath(),
                                dst.getAbsolutePath(),
                                null);
                    } else {
                        CommandHelper.copy(
                                ctx,
                                src.getAbsolutePath(),
                                dst.getAbsolutePath(),
                                null);
                    }
                } catch (Exception e) {
                    // Need to be relaunched?
                    if (e instanceof RelaunchableException) {
                        OnRelaunchCommandResult rl = new OnRelaunchCommandResult() {
                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onSuccess() {
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }

                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onFailed(Throwable cause) {
                                mCause = cause;
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }
                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onCancelled() {
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }
                        };

                        // Translate the exception (and wait for the result)
                        ExceptionUtil.translateException(ctx, e, false, true, rl);
                        synchronized (this.mSync) {
                            this.mSync.wait();
                        }

                        // Persist the exception?
                        if (this.mCause != null) {
                            // The exception must be elevated
                            throw this.mCause;
                        }

                    } else {
                        // The exception must be elevated
                        throw e;
                    }
                }

                // Check that the operation was completed retrieving the fso modified
                CommandHelper.getFileInfo(ctx, dst.getAbsolutePath(), null);
            }
        };
        final BackgroundAsyncTask task = new BackgroundAsyncTask(ctx, callable);

        // Prior to execute, we need to check if some of the files will be overwritten
        List<FileSystemObject> curFiles = onSelectionListener.onRequestCurrentItems();
        if (curFiles != null) {
            // Is necessary to ask the user?
            if (isOverwriteNeeded(files, curFiles)) {
                //Show a dialog asking the user for overwrite the files
                AlertDialog dialog =
                        DialogHelper.createTwoButtonsQuestionDialog(
                                ctx,
                                android.R.string.cancel,
                                R.string.overwrite,
                                ctx.getString(R.string.msgs_overwrite_files),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface alertDialog, int which) {
                                        // NEGATIVE (overwrite)  POSITIVE (cancel)
                                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                                            // Execute background task
                                            task.execute(task);
                                        }
                                    }
                               });
                dialog.show();
                return;
            }
        }

        // Execute background task
        task.execute(task);
    }

    /**
     * Method that check if is needed to prompt the user for overwrite prior to do
     * the operation.
     *
     * @param files The list of source/destination files.
     * @param currentFiles The list of the current files in the destination directory.
     * @return boolean If is needed to prompt the user for overwrite
     */
    private static boolean isOverwriteNeeded(
            List<LinkedResources> files, List<FileSystemObject> currentFiles) {
        boolean askUser = false;
        for (int i = 0; i < currentFiles.size(); i++) {
            for (int j = 0; j < files.size(); j++) {
                FileSystemObject dst1 =  currentFiles.get(i);
                File dst2 = files.get(j).mDst;

                // The file exists in the destination directory
                if (dst1.getFullPath().compareTo(dst2.getAbsolutePath()) == 0) {
                    askUser = true;
                    break;
                }
            }
            if (askUser) break;
        }
        return askUser;
    }

    /**
     * Method that check the consistency of move operations.<br/>
     * <br/>
     * The method checks the following rules:<br/>
     * <ul>
     * <li>Any of the files of the move operation can not include the
     * current directory.</li>
     * <li>Any of the files of the move operation can not include the
     * current directory.</li>
     * </ul>
     *
     * @param ctx The current context
     * @param files The list of source/destination files
     * @param currentDirectory The current directory
     * @return boolean If the consistency is validate successfully
     */
    private static boolean checkMoveConsistency(
            Context ctx, List<LinkedResources> files, String currentDirectory) {
        for (int i = 0; i < files.size(); i++) {
            LinkedResources linkRes = files.get(i);
            String src = linkRes.mSrc.getAbsolutePath();
            String dst = linkRes.mDst.getAbsolutePath();

            // 1.- Current directory can't be moved
            if (currentDirectory.startsWith(src)) {
                // Operation not allowed
                AlertDialog dialog =
                        DialogHelper.createErrorDialog(
                                ctx, R.string.msgs_unresolved_inconsistencies);
                dialog.show();
                return false;
            }

            // 2.- Destination can't be a child of source
            if (dst.startsWith(src)) {
                // Operation not allowed
                AlertDialog dialog =
                        DialogHelper.createErrorDialog(
                                ctx, R.string.msgs_unresolved_inconsistencies);
                dialog.show();
                return false;
            }
        }
        return true;
    }

    /**
     * Method that check the consistency of delete operations.<br/>
     * <br/>
     * The method checks the following rules:<br/>
     * <ul>
     * <li>Any of the files of the move or delete operation can not include the
     * current directory.</li>
     * </ul>
     *
     * @param ctx The current context
     * @param files The list of source/destination files
     * @param currentDirectory The current directory
     * @return boolean If the consistency is validate successfully
     */
    private static boolean checkRemoveConsistency(
            Context ctx, List<FileSystemObject> files, String currentDirectory) {
        for (int i = 0; i < files.size(); i++) {
            FileSystemObject fso = files.get(i);

            // 1.- Current directory can't be deleted
            if (currentDirectory.startsWith(fso.getFullPath())) {
                // Operation not allowed
                AlertDialog dialog =
                        DialogHelper.createErrorDialog(
                                ctx, R.string.msgs_unresolved_inconsistencies);
                dialog.show();
                return false;
            }
        }
        return true;
    }

    /**
     * Method that shows a message when the operation is complete successfully
     *
     * @param ctx The current context
     * @hide
     */
    static void showOperationSuccessMsg(Context ctx) {
        Toast.makeText(ctx, R.string.msgs_success, Toast.LENGTH_SHORT).show();
    }
}