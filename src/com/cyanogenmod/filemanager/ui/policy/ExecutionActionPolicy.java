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

import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.commands.ExecExecutable;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.ui.dialogs.ExecutionDialog;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;

/**
 * A class with the convenience methods for resolve executions related actions
 */
public final class ExecutionActionPolicy extends ActionsPolicy {

    /**
     * A class that holds a listener of the execution of a program
     */
    private static class ExecutionListener implements AsyncResultListener {

        private final ExecutionDialog mDialog;

        /**
         * Constructor of <code>ExecutionListener</code>
         *
         * @param dialog The console dialog
         */
        public ExecutionListener(ExecutionDialog dialog) {
            super();
            this.mDialog = dialog;
        }

        @Override
        public void onPartialResult(Object result) {
            this.mDialog.onAppendData((String)result);
        }

        @Override
        public void onException(Exception cause) {
            this.mDialog.onAppendData(ExceptionUtil.toStackTrace(cause));
        }

        @Override
        public void onAsyncStart() {
            this.mDialog.onStart();
        }

        @Override
        public void onAsyncEnd(boolean cancelled) {/**NON BLOCK**/}

        @Override
        public void onAsyncExitCode(int exitCode) {
            this.mDialog.onEnd(exitCode);
        }
    }

    /**
     * Method that executes a {@link FileSystemObject} and show the output in the console
     * dialog.
     *
     * @param ctx The current context
     * @param fso The file system object
     */
    public static void execute(
            final Context ctx, final FileSystemObject fso) {
        try {
            // Create a console dialog for display the script output
            final ExecutionDialog dialog = new ExecutionDialog(ctx, fso);
            dialog.show();

            Thread t = new Thread() {
                @Override
                public void run() {
                    final ExecutionListener listener = new ExecutionListener(dialog);
                    try {
                        Thread.sleep(250L);

                        // Execute the script
                        ExecExecutable cmd =
                                CommandHelper.exec(
                                ctx, fso.getFullPath(), listener, null);
                        dialog.setCmd(cmd);
                    } catch (Exception e) {
                        listener.onException(e);
                    }
                }
            };
            t.start();

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }
}