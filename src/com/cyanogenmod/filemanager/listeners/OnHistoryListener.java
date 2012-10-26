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

import com.cyanogenmod.filemanager.parcelables.HistoryNavigable;

/**
 * A listener for communicate history changes.
 */
public interface OnHistoryListener {

    /**
     * Invoked when a new history item must be created.
     *
     * @param navigable The data to saved
     */
    void onNewHistory(HistoryNavigable navigable);

    /**
     * Invoked when a check of history items must be checked.
     */
    void onCheckHistory();
}
