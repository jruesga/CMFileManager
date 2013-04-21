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
 * An enumeration of the search result sort modes.
 */
public enum FileTimeFormatMode implements ObjectStringIdentifier {

    /**
     * System-defined.
     */
    SYSTEM("0", null), //$NON-NLS-1$
    /**
     * Locale dependent
     */
    LOCALE("1", null), //$NON-NLS-1$
    /**
     * dd/MM/yyyy HH:mm:ss
     */
    DDMMYYYY_HHMMSS("2", "dd/MM/yyyy HH:mm:ss"), //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * MM/dd/yyyy HH:mm:ss
     */
    MMDDYYYY_HHMMSS("3", "MM/dd/yyyy HH:mm:ss"), //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    YYYYMMDD_HHMMSS("4", "yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$

    private String mId;
    private String mFormat;

    /**
     * Constructor of <code>FileTimeFormatMode</code>.
     *
     * @param id The unique identifier of the enumeration
     * @param format The format (if apply)
     */
    private FileTimeFormatMode(String id, String format) {
        this.mId = id;
        this.mFormat = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns the format of the filetime.
     *
     * @return String The format of the filetime.
     */
    public String getFormat() {
        return this.mFormat;
    }

    /**
     * Method that returns an instance of {@link FileTimeFormatMode} from its
     * unique identifier.
     *
     * @param id The unique identifier
     * @return FileTimeFormatMode The filetime format mode
     */
    public static FileTimeFormatMode fromId(String id) {
        FileTimeFormatMode[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId.compareTo(id) == 0) {
                return values[i];
            }
        }
        return null;
    }

}
