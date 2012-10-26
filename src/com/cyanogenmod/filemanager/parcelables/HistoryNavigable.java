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

package com.cyanogenmod.filemanager.parcelables;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * An interfaces that define a class as navigable from history.
 */
public abstract class HistoryNavigable implements Serializable, Parcelable {

    private static final long serialVersionUID = -6176658075322430461L;

    /**
     * Method that return the title.
     *
     * @return String The title
     */
    public abstract String getTitle();

    /**
     * Method that return the description.
     *
     * @return String The description
     */
    public abstract String getDescription();
}
