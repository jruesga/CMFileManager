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

package com.cyanogenmod.explorer.commands.java;

import android.os.Process;
import android.util.Log;

import com.cyanogenmod.explorer.commands.ListExecutable;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.console.NoSuchFileOrDirectory;
import com.cyanogenmod.explorer.model.Directory;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.model.Group;
import com.cyanogenmod.explorer.model.GroupPermission;
import com.cyanogenmod.explorer.model.OthersPermission;
import com.cyanogenmod.explorer.model.ParentDirectory;
import com.cyanogenmod.explorer.model.Permissions;
import com.cyanogenmod.explorer.model.RegularFile;
import com.cyanogenmod.explorer.model.User;
import com.cyanogenmod.explorer.model.UserPermission;
import com.cyanogenmod.explorer.util.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
                    FileSystemObject fso = createFileSystemObject(files[i]);
                    Log.v(TAG, String.valueOf(fso));
                    this.mFiles.add(fso);
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
            FileSystemObject fso = createFileSystemObject(f);
            if (isTrace()) {
                Log.v(TAG, String.valueOf(fso));
            }
            this.mFiles.add(fso);
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

    /**
     * Method that creates a {@link FileSystemObject}.
     *
     * @param f The file or folder reference
     * @return FileSystemObject The file system object reference
     */
    @SuppressWarnings("static-method")
    private FileSystemObject createFileSystemObject(File f) {
        // The only information i have is if a can read, write or execute but not the groups
        // Assign all the groups the same information
        User user = new User(Process.myUid(), ""); //$NON-NLS-1$ // TODO Retrieve the name
        Group group = new Group(Process.myUid(), ""); //$NON-NLS-1$ // TODO Retrieve the name
        UserPermission u = new UserPermission(f.canRead(), f.canWrite(), f.canExecute());
        GroupPermission g = new GroupPermission(f.canRead(), f.canWrite(), f.canExecute());
        OthersPermission o = new OthersPermission(f.canRead(), f.canWrite(), f.canExecute());
        Permissions perm = new Permissions(u, g, o);

        // Build a directory?
        if (f.isDirectory()) {
            return
                new Directory(
                        f.getName(),
                        f.getParent(),
                        user, group, perm,
                        new Date(f.lastModified()));
        }

        // Build a regular file
        return
            new RegularFile(
                    f.getName(),
                    f.getParent(),
                    user, group, perm,
                    new Date(f.lastModified()),
                    f.length());
    }
}
