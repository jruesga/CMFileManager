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
import android.text.Html;
import android.text.Spanned;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.commands.AsyncResultListener;
import com.cyanogenmod.explorer.commands.UncompressExecutable;
import com.cyanogenmod.explorer.console.ConsoleBuilder;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.RelaunchableException;
import com.cyanogenmod.explorer.listeners.OnRequestRefreshListener;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.ExceptionUtil;
import com.cyanogenmod.explorer.util.ExceptionUtil.OnRelaunchCommandResult;
import com.cyanogenmod.explorer.util.FileHelper;
import com.cyanogenmod.explorer.util.FixedQueue;

import java.util.List;

/**
 * A class with the convenience methods for resolve compress/uncompress related actions
 */
public final class CompressActionPolicy extends ActionsPolicy {

    /**
     * A class that holds a listener for compression/uncompression operations
     */
    private static class CompressListener implements AsyncResultListener {

        Object mSync;
        final FixedQueue<String> mQueue;
        boolean mEnd;
        Throwable mCause;

        /**
         * Constructor of <code>CompressListener</code>
         */
        public CompressListener() {
            super();
            this.mEnd = false;
            this.mSync = new Object();
            this.mQueue = new FixedQueue<String>(2); //Holds only one item
            this.mCause = null;
        }

        @Override
        public void onPartialResult(Object result) {
            synchronized (this.mSync) {
                this.mQueue.insert((String)result);
            }
        }

        @Override
        public void onException(Exception cause) {
            synchronized (this.mSync) {
                this.mCause = cause;
            }
        }

        @Override
        public void onAsyncStart() {/**NON BLOCK**/}

        @Override
        public void onAsyncEnd(boolean canceled) {/**NON BLOCK**/}

        @Override
        public void onAsyncExitCode(int exitCode) {
            synchronized (this.mSync) {
                this.mEnd = true;
            }
        }
    }

    /**
     * Method that uncompress a compressed file.
     *
     * @param ctx The current context
     * @param fso The compressed file
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     * @hide
     */
    public static void uncompress(
            final Context ctx, final FileSystemObject fso,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // The callable interface
        final BackgroundCallable callable = new BackgroundCallable() {
            // The current items
            final Context mCtx = ctx;
            final FileSystemObject mFso = fso;
            final OnRequestRefreshListener mOnRequestRefreshListener = onRequestRefreshListener;

            final Object mSync = new Object();
            Throwable mCause;

            final CompressListener mListener =
                                new CompressListener();
            private String mMsg;
            private boolean mStarted = false;

            @Override
            public int getDialogTitle() {
                return R.string.waiting_dialog_extracting_title;
            }
            @Override
            public int getDialogIcon() {
                return R.drawable.ic_holo_light_operation;
            }

            @Override
            public Spanned requestProgress() {
                // Initializing the dialog
                if (!this.mStarted) {
                    String progress =
                            this.mCtx.getResources().
                                getString(
                                    R.string.waiting_dialog_extracting_analizing_msg);
                    return Html.fromHtml(progress);
                }

                // Return the current operation
                String msg = (this.mMsg == null) ? "" : this.mMsg; //$NON-NLS-1$
                String progress =
                      this.mCtx.getResources().
                          getString(
                              R.string.waiting_dialog_extracting_msg,
                              msg);
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
                this.mStarted = true;

                // This method expect to receive
                // 1.- BackgroundAsyncTask
                BackgroundAsyncTask task = (BackgroundAsyncTask)params[0];
                String out = null;
                try {
                    UncompressExecutable cmd =
                            CommandHelper.uncompress(
                                    ctx,
                                    this.mFso.getFullPath(),
                                    this.mListener, null);
                    out = cmd.getOutUncompressedFile();

                    // Request paint the
                    this.mListener.mQueue.insert(out);
                    task.onRequestProgress();

                    // Don't use an active blocking because this suppose that all message
                    // will be processed by the UI. Instead, refresh with a delay and
                    // display the active file
                    while (!this.mListener.mEnd) {
                        // Sleep to don't saturate the UI thread
                        Thread.sleep(50L);

                        List<String> msgs = this.mListener.mQueue.peekAll();
                        if (msgs.size() > 0) {
                            this.mMsg = msgs.get(msgs.size()-1);
                            task.onRequestProgress();
                        }
                    }

                    // Dialog is ended. Force the last redraw
                    List<String> msgs = this.mListener.mQueue.peekAll();
                    if (msgs.size() > 0) {
                        this.mMsg = msgs.get(msgs.size()-1);
                        task.onRequestProgress();
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
                            public void onCanceled() {
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


                // Any exception?
                if (this.mListener.mCause != null) {
                    throw this.mListener.mCause;
                }

                // Check that the operation was completed retrieving the extracted file or folder
                boolean failed = true;
                try {
                    CommandHelper.getFileInfo(ctx, out, false, null);

                    // Failed. The file exists
                    failed = false;

                } catch (Throwable e) {
                    // Operation complete successfully
                }
                if (failed) {
                    throw new ExecutionException(
                            String.format(
                                    "Failed to extract file: %s", //$NON-NLS-1$
                                    this.mFso.getFullPath()));
                }
            }
        };
        final BackgroundAsyncTask task = new BackgroundAsyncTask(ctx, callable);

        // Check if the output exists
        boolean askUser = false;
        try {
            UncompressExecutable ucmd =
                    ExplorerApplication.getBackgroundConsole().
                        getExecutableFactory().newCreator().
                            createUncompressExecutable(fso.getFullPath(), null);
            String dst = ucmd.getOutUncompressedFile();
            FileSystemObject info = CommandHelper.getFileInfo(ctx, dst, null);
            if (info != null) {
                askUser = true;
            }
        } catch (Exception e) {/**NON BLOCK**/}

        // Ask the user because the destination file or folder exists
        if (askUser) {
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
                                        // Check if the necessary to display a warning because
                                        // security issues
                                        checkZipSecurityWarning(ctx, task, fso);
                                    }
                                }
                           });
            dialog.show();
        } else {
            // Execute background task
            task.execute(task);
        }
    }

    /**
     * Method that checks if it is necessary to display a warning dialog because
     * the privileged extraction of a zip file.
     *
     * @param ctx The current context
     * @param task The task
     * @param fso The zip file
     * @hide
     */
    static void checkZipSecurityWarning(
            final Context ctx, final BackgroundAsyncTask task, FileSystemObject fso) {
        // WARNING! Extracting a ZIP file with relatives or absolutes path could break
        // the system and is need a security alert that the user can confirm prior to
        // make the extraction
        String ext = FileHelper.getExtension(fso);
        if (ConsoleBuilder.isPrivileged() && ext.compareTo("zip") == 0) { //$NON-NLS-1$
            AlertDialog dialog =DialogHelper.createYesNoDialog(
                ctx, R.string.security_warning_extract,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface alertDialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            // Execute background task
                            task.execute(task);
                        }
                    }
               });
            dialog.show();
        } else {
            // Execute background task
            task.execute(task);
        }
    }
}