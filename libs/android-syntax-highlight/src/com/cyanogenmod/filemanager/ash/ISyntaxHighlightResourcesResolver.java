/*
 * Copyright (C) 2013 The CyanogenMod Project
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

package com.cyanogenmod.filemanager.ash;

/**
 * An interface that should be implemented by the library caller, to
 * resolve resources needed by the syntax processors.
 *
 * @see HighlightColors
 */
public interface ISyntaxHighlightResourcesResolver {

    /**
     * Method that returns a string
     *
     * @param id The color unique id
     * @param resid The resource identifier
     * @return CharSequence The string
     */
    CharSequence getString(String id, String resid);

    /**
     * Method that returns an integer
     *
     * @param id The color unique id
     * @param resid The resource identifier
     * @param def The default value
     * @return int The integer value
     */
    int getInteger(String id, String resid, int def);

    /**
     * Method that returns a color
     *
     * @param id The color unique id
     * @param resid The resource identifier
     * @param def The default value
     * @return int The color
     */
    int getColor(String id, String resid, int def);
}
