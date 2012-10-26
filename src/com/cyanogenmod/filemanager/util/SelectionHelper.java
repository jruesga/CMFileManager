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

package com.cyanogenmod.filemanager.util;

import com.cyanogenmod.filemanager.model.FileSystemObject;

import java.util.List;

/**
 * A helper class with useful methods for deal with file selection.
 */
public final class SelectionHelper {

    /**
     * Constructor of <code>SelectionHelper</code>.
     */
    private SelectionHelper() {
        super();
    }

    /**
     * Method that returns if the file system object is in the selection list.
     *
     * @param selection The selection list
     * @param fso The file system object
     * @return boolean Indicates if the file system object is in the selection list
     */
    public static boolean isFileSystemObjectSelected(
            List<FileSystemObject> selection, FileSystemObject fso) {
        if (selection != null) {
            int cc = selection.size();
            for (int i = 0; i < cc; i++) {
                if (selection.get(i).compareTo(fso) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
