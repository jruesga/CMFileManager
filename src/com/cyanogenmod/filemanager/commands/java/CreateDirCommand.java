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

import com.cyanogenmod.filemanager.commands.CreateDirExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.MountPointHelper;

import java.io.File;


/**
 * A class for create a directory.
 */
public class CreateDirCommand extends Program implements CreateDirExecutable {

    private static final String TAG = "CreateDirCommand"; //$NON-NLS-1$

    private final String mPath;

    /**
     * Constructor of <code>CreateDirCommand</code>.
     *
     * @param path The name of the new directory
     */
    public CreateDirCommand(String path) {
        super();
        this.mPath = path;
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
                    String.format("Creating directory: %s", this.mPath)); //$NON-NLS-1$
        }

        File f = new File(this.mPath);
        // Check that if the path exist, it need to be a directory. Otherwise something is
        // wrong
        if (f.exists() && !f.isDirectory()) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. ExecutionException"); //$NON-NLS-1$
            }
            throw new ExecutionException("the path exists but is not a folder"); //$NON-NLS-1$
        }

        // Only create the directory if the folder not exists. Otherwise mkdir will return false
        if (!f.exists()) {
            if (!f.mkdir()) {
                if (isTrace()) {
                    Log.v(TAG, "Result: FAIL. InsufficientPermissionsException"); //$NON-NLS-1$
                }
                throw new InsufficientPermissionsException();
            }
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPoint getSrcWritableMountPoint() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPoint getDstWritableMountPoint() {
        return MountPointHelper.getMountPointFromDirectory(this.mPath);
    }
}
