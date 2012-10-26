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
 * An enumeration of the view layout modes.
 */
public enum NavigationLayoutMode implements ObjectIdentifier {

    /**
     * That mode shows a icon based view (icon + name) on a {@link "GridView"}.
     */
    ICONS(0),
    /**
     * That mode shows a simple item view (icon + name) on a {@link "ListView"}.
     */
    SIMPLE(1),
    /**
     * That mode shows a detail item view (icon + name + last modification + permissions + size)
     * on a {@link "ListView"}.
     */
    DETAILS(2);

    private int mId;

    /**
     * Constructor of <code>NavigationLayoutMode</code>.
     *
     * @param id The unique identifier of the enumeration
     */
    private NavigationLayoutMode(int id) {
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
     * Method that returns an instance of {@link NavigationLayoutMode} from its
     * unique identifier.
     *
     * @param id The unique identifier
     * @return NavigationLayoutMode The navigation view mode
     */
    public static NavigationLayoutMode fromId(int id) {
        NavigationLayoutMode[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId == id) {
                return values[i];
            }
        }
        return null;
    }

}
