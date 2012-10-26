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

package com.cyanogenmod.filemanager.console;

import com.cyanogenmod.filemanager.model.MountPoint;

/**
 * An exception thrown when an operation is writing in a read-only filesystem.
 */
public class ReadOnlyFilesystemException extends Exception {

    private static final long serialVersionUID = -9105723411032174730L;

    private final MountPoint mMountPoint;

    /**
     * Constructor of <code>ReadOnlyFilesystemException</code>.
     *
     * @param mp The read-only mount point that causes the {@link ReadOnlyFilesystemException}
     */
    public ReadOnlyFilesystemException(MountPoint mp) {
        super(mp.getMountPoint());
        this.mMountPoint = mp;
    }

    /**
     * Method that returns the read-only mount point that
     * causes the {@link ReadOnlyFilesystemException}.
     *
     * @return MountPoint The read-only mount point that causes
     * the {@link ReadOnlyFilesystemException}
     */
    public MountPoint getMountPoint() {
        return this.mMountPoint;
    }



}
