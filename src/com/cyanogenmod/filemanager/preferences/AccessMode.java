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
 * An enumeration of the access modes.
 */
public enum AccessMode implements ObjectStringIdentifier {

    /**
     * The safe mode. The app runs without privileges and the only accessible filesystem
     * are the storage volumes (sdcards and USB).
     */
    SAFE("0"), //$NON-NLS-1$
    /**
     * The prompt user mode. The app runs without privileges, with access to all the filesystem,
     * but the user is asked prior to execute a privileged action. If the user accepts then the
     * system change to a {@link AccessMode#ROOT} mode, and continues in it after execute the
     * action.
     */
    PROMPT("1"), //$NON-NLS-1$
    /**
     * the root mode. The app runs with all privileges.
     */
    ROOT("2"); //$NON-NLS-1$

    private String mId;

    /**
     * Constructor of <code>AccessMode</code>.
     *
     * @param id The unique identifier of the enumeration
     */
    private AccessMode(String id) {
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
     * Method that returns an instance of {@link AccessMode} from its
     * unique identifier.
     *
     * @param id The unique identifier
     * @return AccessMode The access mode
     */
    public static AccessMode fromId(String id) {
        AccessMode[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId.compareTo(id) == 0) {
                return values[i];
            }
        }
        return null;
    }

}
