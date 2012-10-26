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

package com.cyanogenmod.filemanager.commands.java;

import android.util.Log;

import com.cyanogenmod.filemanager.commands.CurrentDirExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.console.java.JavaConsole;


/**
 * A class for returns the current directory.
 */
public class CurrentDirCommand extends Program implements CurrentDirExecutable {

    private static final String TAG = "CurrentDirCommand"; //$NON-NLS-1$

    private final JavaConsole mConsole;
    private String mCurrentDir;

    /**
     * Constructor of <code>CurrentDirCommand</code>.
     *
     * @param console The console
     */
    public CurrentDirCommand(JavaConsole console) {
        super();
        this.mConsole = console;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResult() {
        return this.mCurrentDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {
        if (isTrace()) {
            Log.v(TAG, "Obtaing current directory"); //$NON-NLS-1$
        }

        this.mCurrentDir = this.mConsole.getCurrentDir();

        if (isTrace()) {
            Log.v(TAG,
                    String.format(
                            "Result: OK. Current directory: %s", this.mCurrentDir)); //$NON-NLS-1$
        }
    }

}
