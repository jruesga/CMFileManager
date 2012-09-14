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

package com.cyanogenmod.explorer.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.commands.SyncResultExecutable;
import com.cyanogenmod.explorer.commands.shell.InvalidCommandDefinitionException;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.ConsoleAllocException;
import com.cyanogenmod.explorer.console.ConsoleBuilder;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.console.NoSuchFileOrDirectory;
import com.cyanogenmod.explorer.console.OperationTimeoutException;
import com.cyanogenmod.explorer.console.ReadOnlyFilesystemException;
import com.cyanogenmod.explorer.console.RelaunchableException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * A helper class with useful methods for deal with exceptions.
 */
public final class ExceptionUtil {

    /**
     * Constructor of <code>ExceptionUtil</code>.
     */
    private ExceptionUtil() {
        super();
    }

    //Definition of known exceptions and his representation mode and resource identifiers
    private static final Class<?>[] KNOWN_EXCEPTIONS = {
                                                FileNotFoundException.class,
                                                IOException.class,
                                                InvalidCommandDefinitionException.class,
                                                ConsoleAllocException.class,
                                                NoSuchFileOrDirectory.class,
                                                ReadOnlyFilesystemException.class,
                                                InsufficientPermissionsException.class,
                                                CommandNotFoundException.class,
                                                OperationTimeoutException.class,
                                                ExecutionException.class,
                                                ParseException.class
                                                      };
    private static final int[] KNOWN_EXCEPTIONS_IDS = {
                                                R.string.msgs_file_not_found,
                                                R.string.msgs_io_failed,
                                                R.string.msgs_command_not_found,
                                                R.string.msgs_console_alloc_failure,
                                                R.string.msgs_file_not_found,
                                                R.string.msgs_read_only_filesystem,
                                                R.string.msgs_insufficient_permissions,
                                                R.string.msgs_command_not_found,
                                                R.string.msgs_operation_timeout,
                                                R.string.msgs_operation_failure,
                                                R.string.msgs_operation_failure
                                                     };
    private static final boolean[] KNOWN_EXCEPTIONS_TOAST = {
                                                            false,
                                                            false,
                                                            false,
                                                            false,
                                                            false,
                                                            true,
                                                            true,
                                                            false,
                                                            true,
                                                            true,
                                                            true
                                                            };

    /**
     * Method that attach a asynchronous task for executing when exception need
     * to be re-executed.
     *
     * @param ex The exception
     * @param task The task
     * @see RelaunchableException
     */
    public static void attachAsyncTask(Throwable ex, AsyncTask<Object, Integer, Boolean> task) {
        if (ex instanceof RelaunchableException) {
            ((RelaunchableException)ex).setTask(task);
        }
    }

    /**
     * Method that captures and translate an exception, showing a
     * toast or a alert, according to the importance.
     *
     * @param context The current context
     * @param ex The exception
     */
    public static synchronized void translateException(
            final Context context, Throwable ex) {
        translateException(context, ex, false);
    }

    /**
     * Method that captures and translate an exception, showing a
     * toast or a alert, according to the importance.
     *
     * @param context The current context.
     * @param ex The exception
     * @param quiet Don't ask the user
     */
    private static synchronized void translateException(
            final Context context, final Throwable ex, final boolean quiet) {

        //Get the appropriate message for the exception
        int msgResId = R.string.msgs_unknown;
        boolean toast = true;
        for (int i = 0; i < KNOWN_EXCEPTIONS.length; i++) {
            if (KNOWN_EXCEPTIONS[i].getCanonicalName().compareTo(
                    ex.getClass().getCanonicalName()) == 0) {
                msgResId = KNOWN_EXCEPTIONS_IDS[i];
                toast = KNOWN_EXCEPTIONS_TOAST[i];
                break;
            }
        }

        //Check exceptions that can be asked to user
        if (ex instanceof RelaunchableException && !quiet) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    askUser(context, (RelaunchableException)ex);
                }
            });
            return;
        }

        //Audit the exception
        Log.e(context.getClass().getSimpleName(), "Error dectected", ex); //$NON-NLS-1$

        //Build the alert
        final int fMsgResId = msgResId;
        final boolean fToast = toast;
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fToast) {
                    DialogHelper.showToast(context, fMsgResId, Toast.LENGTH_SHORT);
                } else {
                    DialogHelper.createErrorDialog(context, fMsgResId).show();
                }
            }
        });
    }

    /**
     * Method that ask the user for an operation and re-execution of the command.
     *
     * @param context The current context
     * @param relaunchable The exception that contains the command that must be re-executed.
     *
     */
    private static void askUser(
            final Context context, final RelaunchableException relaunchable) {

        //Create a yes/no dialog and ask the user
        AlertDialog alert = DialogHelper.createYesNoDialog(
                    context,
                    relaunchable.getQuestionResourceId(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        @SuppressWarnings("synthetic-access")
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                //Run the executable again
                                try {
                                    //Prepare the system before re-launch the command
                                    prepare(context, relaunchable);

                                    //Re-execute the program
                                    List<SyncResultExecutable> executables =
                                            relaunchable.getExecutables();
                                    for (int i = 0; i < executables.size(); i++) {
                                        SyncResultExecutable executable = executables.get(i);
                                        Object result = CommandHelper.reexecute(
                                                context, executable, null);
                                        if (relaunchable.getTask() != null) {
                                            relaunchable.getTask().execute(result);
                                        }
                                    }

                                } catch (Throwable ex) {
                                    //Capture the exception, this time in quiet mode, if the
                                    //exception is the same
                                    boolean quiet =
                                            ex.getClass().getName().compareTo(
                                                    relaunchable.getClass().getName()) == 0;
                                    translateException(
                                            context, ex, quiet);
                                }
                            }
                        }
                    });
        alert.show();
    }

    /**
     * Method that prepares the system for re-execute the command.
     *
     * @param context The current context
     * @param relaunchable The {@link RelaunchableException} reference
     */
    private static void prepare(final Context context, final RelaunchableException relaunchable) {
        //- This exception need change the console before re-execute
        if (relaunchable instanceof InsufficientPermissionsException) {
            ConsoleBuilder.changeToPrivilegedConsole(context);
        }
    }
}
