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

package com.cyanogenmod.explorer.console;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.commands.shell.InvalidCommandDefinitionException;
import com.cyanogenmod.explorer.console.shell.NonPriviledgeConsole;
import com.cyanogenmod.explorer.console.shell.PrivilegedConsole;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.FileHelper;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class responsible for creating consoles.
 */
public final class ConsoleBuilder {

    private static final String TAG = "ConsoleBuilder"; //$NON-NLS-1$

    private static final Object SYNC = new Object();
    private static ConsoleHolder sHolder;

    private static final int ROOT_UID = 0;

    /**
     * Constructor of <code>ConsoleBuilder</code>.
     */
    private ConsoleBuilder() {
        super();
    }

    /**
     * Method that returns a console, and creates a new console
     * if no console is allocated. The console is create if not exists.
     *
     * @param context The current context
     * @return Console An allocated console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If the console created is not a privileged console
     */
    public static Console getConsole(Context context)
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException,
            ConsoleAllocException, InsufficientPermissionsException {
        return getConsole(context, true);
    }

    /**
     * Method that returns a console. If {@linkplain "createIfNotExists"} is specified
     * a new console will be created
     *
     * @param context The current context
     * @param createIfNotExists Indicates that the console should be create if not exists
     * @return Console An allocated console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If the console created is not a privileged console
     */
    public static Console getConsole(Context context, boolean createIfNotExists)
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException,
            ConsoleAllocException, InsufficientPermissionsException {

        //Check if has a console. Otherwise create a new console
        if (sHolder == null || sHolder.getConsole() == null) {
            if (!createIfNotExists) {
                return null;
            }
            createDefaultConsole(context);
        }
        return sHolder.getConsole();
    }

    /**
     * Method that changes the current console to a non-privileged console.
     *
     * @param context The current context
     * @return boolean If the operation was successfully
     */
    public static boolean changeToNonPrivilegedConsole(Context context) {

        //Check the current console
        if (sHolder.getConsole() instanceof NonPriviledgeConsole) {
            //The current console is non-privileged. Not needed
            return true;
        }

        //Create the console
        ConsoleHolder holder = null;
        try {
            //Create the console, destroy the current console, and marks as current
            holder = new ConsoleHolder(
                    createNonPrivilegedConsole(context, FileHelper.ROOT_DIRECTORY));
            destroyConsole();
            sHolder = holder;
            return true;

        } catch (Throwable e) {
            if (holder != null) {
                holder.dispose();
            }
        }
        return false;
    }

    /**
     * Method that changes the current console to a privileged console.
     *
     * @param context The current context
     * @return boolean If the operation was successfully
     */
    public static boolean changeToPrivilegedConsole(Context context) {

        //Destroy and create the new console
        if (sHolder.getConsole() instanceof PrivilegedConsole) {
            //The current console is privileged. Not needed
            return true;
        }

        //Create the console
        ConsoleHolder holder = null;
        try {
            //Create the console, destroy the current console, and marks as current
            holder = new ConsoleHolder(createPrivilegedConsole(context, FileHelper.ROOT_DIRECTORY));
            destroyConsole();
            sHolder = holder;
            return sHolder.getConsole() instanceof PrivilegedConsole;

        } catch (Throwable e) {
            destroyConsole();
            if (holder != null) {
                holder.dispose();
            }
        }
        return false;
    }

    /**
     * Method that returns a console, and creates a new console if no
     * console is allocated or if the settings preferences has changed.
     *
     * @param context The current context
     * @return Console An allocated console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If the console created is not a privileged console
     */
    //IMP! This must be invoked from the main activity creation
    public static Console createDefaultConsole(Context context)
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException,
            ConsoleAllocException, InsufficientPermissionsException {

        synchronized (ConsoleBuilder.SYNC) {
            //Gets superuser mode settings
            boolean requiredSuConsole =
                    Preferences.getSharedPreferences().getBoolean(
                            ExplorerSettings.SETTINGS_SUPERUSER_MODE.getId(),
                            ((Boolean)ExplorerSettings.SETTINGS_SUPERUSER_MODE.
                                    getDefaultValue()).booleanValue());
            boolean allowConsoleSelection = Preferences.getSharedPreferences().getBoolean(
                    ExplorerSettings.SETTINGS_ALLOW_CONSOLE_SELECTION.getId(),
                    ((Boolean)ExplorerSettings.
                            SETTINGS_ALLOW_CONSOLE_SELECTION.
                                getDefaultValue()).booleanValue());
            if (!requiredSuConsole && !allowConsoleSelection) {
                // allowConsoleSelection forces the su console
                try {
                    Preferences.savePreference(
                            ExplorerSettings.SETTINGS_SUPERUSER_MODE, Boolean.TRUE, true);
                } catch (Throwable ex) {
                    Log.w(TAG, "Can't save console preference", ex); //$NON-NLS-1$
                }
                requiredSuConsole = true;
            }

            //Check if console settings has changed
            if (sHolder != null) {
                if (
                    (sHolder.getConsole() instanceof NonPriviledgeConsole && requiredSuConsole)
                    || (sHolder.getConsole() instanceof PrivilegedConsole && !requiredSuConsole)) {
                    //Deallocate actual console
                    sHolder.dispose();
                    sHolder = null;
                }
            }

            //Is there a console allocated
            if (sHolder == null) {
                sHolder = (requiredSuConsole)
                        ? new ConsoleHolder(
                                createPrivilegedConsole(context, FileHelper.ROOT_DIRECTORY))
                        : new ConsoleHolder(
                                createNonPrivilegedConsole(context, FileHelper.ROOT_DIRECTORY));
            }
            return sHolder.getConsole();
        }
    }

    /**
     * Method that destroy the current console.
     */
    public static void destroyConsole() {
        try {
            if (sHolder != null) {
                sHolder.dispose();
            }
        } catch (Exception e) {
            /**NON BLOCK**/
        }
        sHolder = null;
    }

    /**
     * Method that creates a new non privileged console.
     *
     * @param context The current context
     * @param initialDirectory The initial directory of the console
     * @return Console The non privileged console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws ConsoleAllocException If the console can't be allocated
     * @see NonPriviledgeConsole
     */
    public static Console createNonPrivilegedConsole(Context context, String initialDirectory)
            throws FileNotFoundException, IOException,
            InvalidCommandDefinitionException, ConsoleAllocException {
        NonPriviledgeConsole console = new NonPriviledgeConsole(initialDirectory);
        console.setBufferSize(context.getResources().getInteger(R.integer.buffer_size));
        console.alloc();
        return console;
    }

    /**
     * Method that creates a new privileged console. If the allocation of the
     * privileged console fails, the a non privileged console
     *
     * @param context The current context
     * @param initialDirectory The initial directory of the console
     * @return Console The privileged console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If the console created is not a privileged console
     * @see PrivilegedConsole
     */
    public static Console createPrivilegedConsole(Context context, String initialDirectory)
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException,
            ConsoleAllocException, InsufficientPermissionsException {
        return createPrivilegedConsole(context, initialDirectory, true);
    }

    /**
     * Method that creates a new privileged console. If the allocation of the
     * privileged console fails, the a non privileged console
     *
     * @param context The current context
     * @param initialDirectory The initial directory of the console
     * @param silent Indicates that no message have to be displayed
     * @return Console The privileged console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If the console created is not a privileged console
     * @see PrivilegedConsole
     */
    public static Console createPrivilegedConsole(
            Context context, String initialDirectory, boolean silent)
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException,
            ConsoleAllocException, InsufficientPermissionsException {
        try {
            PrivilegedConsole console = new PrivilegedConsole(initialDirectory);
            console.setBufferSize(context.getResources().getInteger(R.integer.buffer_size));
            console.alloc();
            if (console.getIdentity().getUser().getId() != ROOT_UID) {
                //The console is not a privileged console
                try {
                    console.dealloc();
                } catch (Throwable ex) {
                    /**NON BLOCK**/
                }
                throw new InsufficientPermissionsException(null);
            }
            return console;
        } catch (ConsoleAllocException caEx) {
            //Show a message with the problem?
            Log.w(TAG, context.getString(R.string.msgs_privileged_console_alloc_failed), caEx);
            if (!silent) {
                try {
                    DialogHelper.showToast(context,
                            R.string.msgs_privileged_console_alloc_failed, Toast.LENGTH_LONG);
                } catch (Exception ex) {
                    Log.e(TAG, "Can't show toast", ex);  //$NON-NLS-1$
                }
            }

            boolean allowConsoleSelection = Preferences.getSharedPreferences().getBoolean(
                    ExplorerSettings.SETTINGS_ALLOW_CONSOLE_SELECTION.getId(),
                    ((Boolean)ExplorerSettings.
                            SETTINGS_ALLOW_CONSOLE_SELECTION.
                                getDefaultValue()).booleanValue());
            if (allowConsoleSelection) {
                //Save settings
                try {
                    Editor editor = Preferences.getSharedPreferences().edit();
                    editor.putBoolean(ExplorerSettings.SETTINGS_SUPERUSER_MODE.getId(), false);
                    editor.commit();
                } catch (Exception ex) {
                    Log.e(TAG,
                            String.format("Failed to save %s property",  //$NON-NLS-1$
                            ExplorerSettings.SETTINGS_SUPERUSER_MODE.getId()), ex);
                }

                //Create the non-privileged console
                NonPriviledgeConsole console = new NonPriviledgeConsole(initialDirectory);
                console.alloc();
                return console;
            }

            // Rethrow the exception
            throw caEx;
        }

    }

}
