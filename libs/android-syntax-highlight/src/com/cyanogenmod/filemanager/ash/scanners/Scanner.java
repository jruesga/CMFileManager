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


package com.cyanogenmod.filemanager.ash.scanners;


/**
 * The base class for all the scanners
 */
public abstract class Scanner {

    CharSequence mInput;

    /**
     * Constructor of <code>Scanner</code>
     *
     * @param input The input
     */
    public Scanner(CharSequence input) {
        super();
        this.mInput = input;
    }

    /**
     * Method that starts the scan process
     */
    public abstract void scan();
}
