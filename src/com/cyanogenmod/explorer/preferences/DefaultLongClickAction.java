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
 * An enumeration of the long-click action possibilities.
 */
public enum DefaultLongClickAction implements ObjectStringIdentifier {

    /**
     * No action.
     */
    NONE("0"), //$NON-NLS-1$
    /**
     * Show the item content description (item name).
     */
    SHOW_CONTENT_DESCRIPTION("1"), //$NON-NLS-1$
    /**
     * Select/deselect the item
     */
    SELECT_DESELECT("2"), //$NON-NLS-1$T
    /**
     * Open the item with ...
     */
    OPEN_WITH("3"), //$NON-NLS-1$
    /**
     * Show the item properties
     */
    SHOW_PROPERTIES("4"), //$NON-NLS-1$
    /**
     * Show the item actions
     */
    SHOW_ACTIONS("5"); //$NON-NLS-1$

    private String mId;

    /**
     * Constructor of <code>DefaultLongClickAction</code>.
     *
     * @param id The unique identifier of the enumeration
     */
    private DefaultLongClickAction(String id) {
        this.mId = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns an instance of {@link DefaultLongClickAction} from its
     * unique identifier.
     *
     * @param id The unique identifier
     * @return DefaultLongClickAction The default long click action
     */
    public static DefaultLongClickAction fromId(String id) {
        DefaultLongClickAction[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId.compareTo(id) == 0) {
                return values[i];
            }
        }
        return null;
    }

}
