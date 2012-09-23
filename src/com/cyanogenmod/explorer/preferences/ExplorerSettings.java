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


/**
 * The enumeration of settings of Explorer application.
 */
public enum ExplorerSettings {
    /**
     * Whether use SUPERUSER mode.
     * @hide
     */
    SETTINGS_SUPERUSER_MODE("cm_explorer_superuser_mode", Boolean.FALSE),  //$NON-NLS-1$
    /**
     * The initial directory to be used.
     * @hide
     */
    SETTINGS_INITIAL_DIR("cm_explorer_initial_dir", "/"), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The view mode to use (simple, details, or icons).
     * @hide
     */
    SETTINGS_LAYOUT_MODE("cm_explorer_layout_mode", NavigationLayoutMode.DETAILS), //$NON-NLS-1$
    /**
     * The sort mode to use (name or data, ascending or descending).
     * @hide
     */
    SETTINGS_SORT_MODE("cm_explorer_sort_mode", NavigationSortMode.NAME_ASC), //$NON-NLS-1$

    /**
     * When to sort the directories before the files.
     * @hide
     */
    SETTINGS_SHOW_DIRS_FIRST("cm_explorer_show_dirs_first", Boolean.TRUE), //$NON-NLS-1$
    /**
     * When to show the hidden files.
     * @hide
     */
    SETTINGS_SHOW_HIDDEN("cm_explorer_show_hidden", Boolean.FALSE), //$NON-NLS-1$
    /**
     * When to show the system files.
     * @hide
     */
    SETTINGS_SHOW_SYSTEM("cm_explorer_show_system", Boolean.FALSE), //$NON-NLS-1$
    /**
     * When to show the symlinks files.
     * @hide
     */
    SETTINGS_SHOW_SYMLINKS("cm_explorer_show_symlinks", Boolean.TRUE), //$NON-NLS-1$

    /**
     * When to highlight the terms of the search in the search results
     * @hide
     */
    SETTINGS_HIGHLIGHT_TERMS("cm_explorer_highlight_terms", Boolean.TRUE), //$NON-NLS-1$
    /**
     * When to show the relevance widget on searches
     * @hide
     */
    SETTINGS_SHOW_RELEVANCE_WIDGET(
            "cm_explorer_show_relevance_widget", //$NON-NLS-1$
            Boolean.TRUE),
    /**
     * How to sort the search results
     * @hide
     */
    SETTINGS_SORT_SEARCH_RESULTS_MODE(
            "cm_explorer_sort_search_results_mode", //$NON-NLS-1$
            SearchSortResultMode.RELEVANCE),
    /**
     * When to save the search terms
     * @hide
     */
    SETTINGS_SAVE_SEARCH_TERMS("cm_explorer_save_search_terms", Boolean.TRUE), //$NON-NLS-1$

    /**
     * When to show debug traces
     * @hide
     */
    SETTINGS_SHOW_TRACES("cm_explorer_show_debug_traces", Boolean.FALSE); //$NON-NLS-1$



    /**
     * A broadcast intent that is sent when a setting was changed
     */
    public final static String INTENT_SETTING_CHANGED =
                        "com.cyanogenmod.explorer.INTENT_SETTING_CHANGED"; //$NON-NLS-1$

    /**
     * The extra key with the preference key that has changed
     */
    public final static String EXTRA_SETTING_CHANGED_KEY = "preference"; //$NON-NLS-1$




    private final String mId;
    private final Object mDefaultValue;

    /**
     * Constructor of <code>ExplorerSettings</code>.
     *
     * @param id The unique identifier of the setting
     * @param defaultValue The default value of the setting
     */
    private ExplorerSettings(String id, Object defaultValue) {
        this.mId = id;
        this.mDefaultValue = defaultValue;
    }

    /**
     * Method that returns the unique identifier of the setting.
     * @return the mId
     */
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns the default value of the setting.
     *
     * @return Object The default value of the setting
     */
    public Object getDefaultValue() {
        return this.mDefaultValue;
    }

    /**
     * Method that returns an instance of {@link ExplorerSettings} from its.
     * unique identifier
     *
     * @param id The unique identifier
     * @return ExplorerSettings The navigation sort mode
     */
    public static ExplorerSettings fromId(String id) {
        ExplorerSettings[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].mId == id) {
                return values[i];
            }
        }
        return null;
    }
}
