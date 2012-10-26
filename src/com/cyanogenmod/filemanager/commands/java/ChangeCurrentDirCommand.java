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

import com.cyanogenmod.filemanager.commands.ChangeCurrentDirExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.console.java.JavaConsole;

import java.io.File;


/**
 * A class for change the current directory.
 */
public class ChangeCurrentDirCommand extends Program implements ChangeCurrentDirExecutable {

    private static final String TAG = "ChangeCurrentDirCommand"; //$NON-NLS-1$

    private final JavaConsole mConsole;
    private final String mNewDir;

    /**
     * Constructor of <code>ChangeCurrentDirCommand</code>.
     *
     * @param console The console
     * @param newDir The new directory to which to change
     */
    public ChangeCurrentDirCommand(JavaConsole console, String newDir) {
        super();
        this.mNewDir = newDir;
        this.mConsole = console;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getResult() {
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {
        if (isTrace()) {
            Log.v(TAG,
                    String.format("Changing current directory to %s", this.mNewDir)); //$NON-NLS-1$
        }

        // Check that the file exists and is a directory
        File f = new File(this.mNewDir);
        if (!f.exists() || !f.isDirectory()) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. NoSuchFileOrDirectory"); //$NON-NLS-1$
            }
            throw new NoSuchFileOrDirectory(this.mNewDir);
        }

        // Check that we have the access to the directory
        if (!f.canRead() || !f.canExecute()) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. InsufficientPermissionsException"); //$NON-NLS-1$
            }
            throw new InsufficientPermissionsException();
        }

        // Set the new current directory
        this.mConsole.setCurrentDir(this.mNewDir);

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

}
