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

import com.cyanogenmod.filemanager.commands.ListExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.ParentDirectory;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for list information about files and directories.
 */
public class ListCommand extends Program implements ListExecutable {

    private static final String TAG = "ListCommand"; //$NON-NLS-1$

    private final String mSrc;
    private final LIST_MODE mMode;
    private final List<FileSystemObject> mFiles;

    /**
     * Constructor of <code>ListCommand</code>. List mode.
     *
     * @param src The file system object to be listed
     * @param mode The mode of listing
     */
    public ListCommand(String src, LIST_MODE mode) {
        super();
        this.mSrc = src;
        this.mMode = mode;
        this.mFiles = new ArrayList<FileSystemObject>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileSystemObject> getResult() {
        return this.mFiles;
    }

    /**
     * Method that returns a single result of the program invocation.
     * Only must be called within a <code>FILEINFO</code> mode listing.
     *
     * @return FileSystemObject The file system object reference
     */
    public FileSystemObject getSingleResult() {
        return this.mFiles.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {
        if (isTrace()) {
            Log.v(TAG,
                    String.format("Listing %s. Mode: %s", //$NON-NLS-1$
                            this.mSrc, this.mMode));
        }

        File f = new File(this.mSrc);
        if (!f.exists()) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. NoSuchFileOrDirectory"); //$NON-NLS-1$
            }
            throw new NoSuchFileOrDirectory(this.mSrc);
        }
        if (this.mMode.compareTo(LIST_MODE.DIRECTORY) == 0) {
            File[] files = f.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    FileSystemObject fso = FileHelper.createFileSystemObject(files[i]);
                    if (fso != null) {
                        if (isTrace()) {
                            Log.v(TAG, String.valueOf(fso));
                        }
                        this.mFiles.add(fso);
                    }
                }
            }

            //Now if not is the root directory
            if (this.mSrc != null &&
                    this.mSrc.compareTo(FileHelper.ROOT_DIRECTORY) != 0 &&
                    this.mMode.compareTo(LIST_MODE.DIRECTORY) == 0) {
                this.mFiles.add(0, new ParentDirectory(new File(this.mSrc).getParent()));
            }

        } else {
            // Build the parent information
            FileSystemObject fso = FileHelper.createFileSystemObject(f);
            if (fso != null) {
                if (isTrace()) {
                    Log.v(TAG, String.valueOf(fso));
                }
                this.mFiles.add(fso);
            }
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

}
