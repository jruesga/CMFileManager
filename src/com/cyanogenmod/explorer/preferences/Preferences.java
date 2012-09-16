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

package com.cyanogenmod.explorer.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.cyanogenmod.explorer.ExplorerApplication;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A helper class for access and manage the preferences of the application.
 */
public final class Preferences {

    private static final String TAG = "Preferences"; //$NON-NLS-1$

    /**
     * A variable that holds the term of the last textual search.
     */
    private static String sLastSearch = ""; //$NON-NLS-1$

    /**
     * The name of the explorer settings file.
     * @hide
     */
    private static final String SETTINGS_FILENAME = "explorer"; //$NON-NLS-1$

    /**
     * The list of configuration listeners.
     */
    private static final List<ConfigurationListener> CONFIGURATION_LISTENERS =
            Collections.synchronizedList(new ArrayList<ConfigurationListener>());


    /**
     * Constructor of <code>Preferences</code>.
     */
    private Preferences() {
        super();
    }

    /**
     * Method that returns the last search do in the current application execution.
     *
     * @return String The last search do in the current application execution
     */
    public static String getLastSearch() {
        return sLastSearch;
    }

    /**
     * Method that sets the last search do in the current application execution.
     *
     * @param lastSearch The last search do in the current application execution
     */
    public static void setLastSearch(String lastSearch) {
        Preferences.sLastSearch = lastSearch;
    }

    /**
     * Method that initializes the defaults preferences of the application.
     */
    public static void loadDefaults() {
        //Sets the default preferences if no value is set yet
        try {
            Map<ExplorerSettings, Object> defaultPrefs =
                    new HashMap<ExplorerSettings, Object>();
            ExplorerSettings[] values = ExplorerSettings.values();
            for (int i = 0; i < values.length; i++) {
                defaultPrefs.put(values[i], values[i].getDefaultValue());
            }
            savePreferences(defaultPrefs, false, true);
        } catch (Exception ex) {
            Log.e(TAG, "Save default settings fails", ex); //$NON-NLS-1$
        }
    }

    /**
     * Method that adds a new configuration listener.
     *
     * @param listener The new configuration listener
     */
    public static void addConfigurationListener(ConfigurationListener listener) {
        CONFIGURATION_LISTENERS.add(listener);
    }

    /**
     * Method that removes the configuration listener.
     *
     * @param listener The configuration listener to be removed
     */
    public static void removeConfigurationListener(ConfigurationListener listener) {
        CONFIGURATION_LISTENERS.remove(listener);
    }

    /**
     * Method that returns the shared preferences of the application.
     *
     * @return SharedPreferences The shared preferences of the application
     * @hide
     */
    public static SharedPreferences getSharedPreferences() {
        return ExplorerApplication.getInstance().getSharedPreferences(
                SETTINGS_FILENAME, Context.MODE_PRIVATE);
    }

    /**
     * Method that saves a preference.
     *
     * @param pref The preference identifier
     * @param value The value of the preference
     * @param applied If the preference was applied
     * @throws InvalidClassException If the value of the preference is not of the
     * type of the preference
     */
    public static void savePreference(ExplorerSettings pref, Object value, boolean applied)
            throws InvalidClassException {
        Map<ExplorerSettings, Object> prefs =
                new HashMap<ExplorerSettings, Object>();
        prefs.put(pref, value);
        savePreferences(prefs, applied);
    }

    /**
     * Method that saves the preferences passed as argument.
     *
     * @param prefs The preferences to be saved
     * @param applied If the preference was applied
     * @throws InvalidClassException If the value of a preference is not of the
     * type of the preference
     */
    public static void savePreferences(Map<ExplorerSettings, Object> prefs, boolean applied)
            throws InvalidClassException {
        savePreferences(prefs, true, applied);
    }

    /**
     * Method that saves the preferences passed as argument.
     *
     * @param prefs The preferences to be saved
     * @param noSaveIfExists No saves if the preference if has a value
     * @param applied If the preference was applied
     * @throws InvalidClassException If the value of a preference is not of the
     * type of the preference
     */
    private static void savePreferences(
            Map<ExplorerSettings, Object> prefs, boolean noSaveIfExists, boolean applied)
            throws InvalidClassException {
        //Get the preferences editor
        SharedPreferences sp = getSharedPreferences();
        Editor editor = sp.edit();

        //Save all settings
        Iterator<ExplorerSettings> it = prefs.keySet().iterator();
        while (it.hasNext()) {
            ExplorerSettings pref = it.next();
            if (!noSaveIfExists && sp.contains(pref.getId())) {
                //The preference already has a value
                continue;
            }

            //Known and valid types
            Object value = prefs.get(pref);
            if (value instanceof Boolean && pref.getDefaultValue() instanceof Boolean) {
                editor.putBoolean(pref.getId(), ((Boolean)value).booleanValue());
            } else if (value instanceof String && pref.getDefaultValue() instanceof String) {
                editor.putString(pref.getId(), (String)value);
            } else if (value instanceof ObjectIdentifier
                    && pref.getDefaultValue() instanceof ObjectIdentifier) {
                editor.putInt(pref.getId(), ((ObjectIdentifier)value).getId());
            } else {
                //The object is not of the appropriate type
                String msg = String.format(
                                    "%s: %s",  //$NON-NLS-1$
                                    pref.getId(),
                                    value.getClass().getName());
                Log.e(TAG, String.format(
                                "Configuration error. InvalidClassException: %s",  //$NON-NLS-1$
                                msg));
                throw new InvalidClassException(msg);
            }

        }

        //Commit settings
        editor.commit();

        //Now its time to communicate the configuration change
        if (CONFIGURATION_LISTENERS != null && CONFIGURATION_LISTENERS.size() > 0) {
            it = prefs.keySet().iterator();
            while (it.hasNext()) {
                ExplorerSettings pref = it.next();
                Object value = prefs.get(pref);
                for (int i = 0; i < CONFIGURATION_LISTENERS.size(); i++) {
                    CONFIGURATION_LISTENERS.get(i).onConfigurationChanged(pref, value, applied);
                }
            }
        }
    }
}
