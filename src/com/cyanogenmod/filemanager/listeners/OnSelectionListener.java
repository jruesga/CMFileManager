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

package com.cyanogenmod.filemanager.listeners;

import com.cyanogenmod.filemanager.model.FileSystemObject;

import java.util.List;

/**
 * A listener for requesting selection actions
 */
public interface OnSelectionListener {
    /**
     * Method that request to toggle selection of the {@link FileSystemObject} object.
     *
     * @param fso The file system object
     */
    void onToggleSelection(FileSystemObject fso);

    /**
     *Method that request the deselection all items.
     */
    void onDeselectAll();

    /**
     * Method that request the selection all visible items.
     */
    void onSelectAllVisibleItems();

    /**
     *Method that request the deselection all visible items.
     */
    void onDeselectAllVisibleItems();

    /**
     * Method that request the current {@link FileSystemObject} selection
     *
     * @return List<FileSystemObject> The array of {@link FileSystemObject} objects selected.
     */
    List<FileSystemObject> onRequestSelectedFiles();

    /**
     * Method that request the current directory items
     *
     * @return List<FileSystemObject> The array of {@link FileSystemObject} objects of the
     * current directory.
     */
    List<FileSystemObject> onRequestCurrentItems();

    /**
     * Method that request the current directory.
     *
     * @return String The current directory.
     */
    String onRequestCurrentDir();
}
