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

package com.cyanogenmod.filemanager.preferences;

/**
 * An enumeration of the navigation sort modes.
 */
public enum NavigationSortMode implements ObjectIdentifier {

    /**
     * That mode sorts objects by name (ascending).
     */
    NAME_ASC(0),
    /**
     * That mode sorts objects by name (descending).
     */
    NAME_DESC(1),
    /**
     * That mode sorts objects by date (ascending).
     */
    DATE_ASC(2),
    /**
     * That mode sorts objects by date (descending).
     */
    DATE_DESC(3),
    /**
     * That mode sorts objects by size (ascending).
     */
    SIZE_ASC(4),
    /**
     * That mode sorts objects by size (descending).
     */
    SIZE_DESC(5);

    private int mId;

    /**
     * Constructor of <code>NavigationSortMode</code>.
     *
     * @param id The unique identifier of the enumeration
     */
    private NavigationSortMode(int id) {
        this.mId = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return this.mId;
    }

    /**
     * Method that returns an instance of {@link NavigationSortMode} from its
     * unique identifier.
     *
     * @param id The unique identifier
     * @return NavigationSortMode The navigation sort mode
     */
    public static NavigationSortMode fromId(int id) {
        NavigationSortMode[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId == id) {
                return values[i];
            }
        }
        return null;
    }

}
