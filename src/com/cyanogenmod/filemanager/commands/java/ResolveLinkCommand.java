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

import com.cyanogenmod.filemanager.commands.ListExecutable.LIST_MODE;
import com.cyanogenmod.filemanager.commands.ResolveLinkExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.FileSystemObject;

import java.io.File;


/**
 * A class for retrieve the real file name of a symlink. This command
 * can be used too for retrieve the absolute path of a file or directory
 */
public class ResolveLinkCommand extends Program implements ResolveLinkExecutable {

    private static final String TAG = "ResolveLinkCommand"; //$NON-NLS-1$

    private final String mSrc;
    private FileSystemObject mFso;

    /**
     * Constructor of <code>ResolveLinkCommand</code>.
     *
     * @param src The file system object to read
     */
    public ResolveLinkCommand(String src) {
        super();
        this.mSrc = src;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystemObject getResult() {
        return this.mFso;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {
        if (isTrace()) {
            Log.v(TAG,
                    String.format("Resolving link of %s", //$NON-NLS-1$
                            this.mSrc));
        }

        File f = new File(this.mSrc);
        if (!f.exists()) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. InsufficientPermissionsException"); //$NON-NLS-1$
            }
            throw new InsufficientPermissionsException();
        }
        try {
            String absPath = f.getCanonicalPath();
            ListCommand cmd = new ListCommand(absPath, LIST_MODE.FILEINFO);
            cmd.execute();
            this.mFso = cmd.getSingleResult();
        } catch (Exception e) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. ExecutionException"); //$NON-NLS-1$
            }
            throw new ExecutionException("can't resolve link"); //$NON-NLS-1$
        }

        if (isTrace()) {
            Log.v(TAG,
                    String.format("Link: %s", //$NON-NLS-1$
                            this.mFso));
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

}
