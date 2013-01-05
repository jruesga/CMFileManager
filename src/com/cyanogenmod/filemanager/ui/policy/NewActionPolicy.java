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

package com.cyanogenmod.filemanager.ui.policy;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cyanogenmod.filemanager.console.RelaunchableException;
import com.cyanogenmod.filemanager.listeners.OnRequestRefreshListener;
import com.cyanogenmod.filemanager.listeners.OnSelectionListener;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;

import java.io.File;

/**
 * A class with the convenience methods for resolve new related actions
 */
public final class NewActionPolicy extends ActionsPolicy {

    private static final String TAG = "NewActionPolicy"; //$NON-NLS-1$

    private static boolean DEBUG = false;

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
                    fso = CommandHelper.getFileInfo(ctx, newName, false, null);
                } catch (Throwable ex2) {/**NON BLOCK**/}
                onRequestRefreshListener.onRequestRefresh(fso, false);
            }
            showOperationSuccessMsg(ctx);

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
                                fso = CommandHelper.getFileInfo(ctx, newName, false, null);
                            } catch (Throwable ex2) {/**NON BLOCK**/}
                            onRequestRefreshListener.onRequestRefresh(fso, false);
                        }
                        return Boolean.TRUE;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result != null && result.booleanValue()) {
                            showOperationSuccessMsg(ctx);
                        }
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
     * @param src The source file system object
     * @param lnkName The new name of the link
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void createSymlink(
            final Context ctx,
            final FileSystemObject src,
            final String lnkName,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        //Create the absolute file name
        File newFso = new File(
                onSelectionListener.onRequestCurrentDir(), lnkName);
        final String link = newFso.getAbsolutePath();

        try {
            if (DEBUG) {
                Log.d(TAG, String.format(
                        "Creating new symlink: %s -> %s", src.getFullPath(), link)); //$NON-NLS-1$
            }
            CommandHelper.createLink(ctx, src.getFullPath(), link, null);

            //Operation complete. Show refresh
            if (onRequestRefreshListener != null) {
                FileSystemObject fso = null;
                try {
                    fso = CommandHelper.getFileInfo(ctx, link, false, null);
                } catch (Throwable ex2) {
                    /**NON BLOCK**/
                }
                onRequestRefreshListener.onRequestRefresh(fso, false);
            }
            showOperationSuccessMsg(ctx);

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
                                fso = CommandHelper.getFileInfo(ctx, link, false, null);
                            } catch (Throwable ex2) {/**NON BLOCK**/}
                            onRequestRefreshListener.onRequestRefresh(fso, false);
                        }
                        return Boolean.TRUE;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result != null && result.booleanValue()) {
                            showOperationSuccessMsg(ctx);
                        }
                    }

                });
            }
            ExceptionUtil.translateException(ctx, ex);
        }
    }
}