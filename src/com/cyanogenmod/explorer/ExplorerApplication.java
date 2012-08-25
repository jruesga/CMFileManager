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

package com.cyanogenmod.explorer;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.cyanogenmod.explorer.console.Console;
import com.cyanogenmod.explorer.console.ConsoleBuilder;
import com.cyanogenmod.explorer.console.ConsoleHolder;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.util.FileHelper;
import com.cyanogenmod.explorer.util.MimeTypeHelper;

/**
 * A class that wraps the information of the application (constants,
 * identifiers, statics variables, ...).
 * @hide
 */
public final class ExplorerApplication extends Application {

    private static final String TAG = "ExplorerApplication"; //$NON-NLS-1$

    /**
     * A constant that indicates whether the application is running in debug mode.
     * @hide
     */
    public static final boolean DEBUG = true;

    /**
     * A constant that contains the main process name.
     * @hide
     */
    public static final String MAIN_PROCESS = "com.cyanogenmod.explorer:main"; //$NON-NLS-1$

    //Static resources
    private static ExplorerApplication sApp;
    private static ConsoleHolder sBackgroundConsole;


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        if (DEBUG) {
            Log.d(TAG, "ExplorerApplication.onCreate"); //$NON-NLS-1$
        }
        register();
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (DEBUG) {
            Log.d(TAG, "ExplorerApplication.onConfigurationChanged"); //$NON-NLS-1$
        }
        register();
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTerminate() {
        if (DEBUG) {
            Log.d(TAG, "onTerminate"); //$NON-NLS-1$
        }
        try {
            sBackgroundConsole.dispose();
        } catch (Throwable ex) {
            /**NON BLOCK**/
        }
        try {
            ConsoleBuilder.destroyConsole();
        } catch (Throwable ex) {
            /**NON BLOCK**/
        }
        super.onTerminate();
    }

    /**
     * Method that register the application context.
     */
    private void register() {
        //Save the static application reference
        sApp = this;
    }

    /**
     * Method that initializes the application.
     */
    private void init() {
        //Sets the default preferences if no value is set yet
        Preferences.loadDefaults();

        //Create a non-privileged console for background non-privileged tasks
        try {
            sBackgroundConsole =
                    new ConsoleHolder(
                            ConsoleBuilder.createNonPrivilegedConsole(FileHelper.ROOT_DIRECTORY));
        } catch (Exception e) {
            Log.e(TAG,
                    "Background console creation failed. " +  //$NON-NLS-1$
                    "This probably will cause a force close.", e); //$NON-NLS-1$
        }

        //Force the load of mime types
        try {
            MimeTypeHelper.loadMimeTypes(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Mime-types failed.", e); //$NON-NLS-1$
        }
    }

    /**
     * Method that returns the singleton reference of the application.
     *
     * @return Application The application singleton reference
     * @hide
     */
    public static Application getInstance() {
        return sApp;
    }

    /**
     * Method that returns the application background console.
     *
     * @return Console The background console
     */
    public static Console getBackgroundConsole() {
        return sBackgroundConsole.getConsole();
    }

}
