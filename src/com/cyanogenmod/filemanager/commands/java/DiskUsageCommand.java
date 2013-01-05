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

import com.cyanogenmod.filemanager.commands.DiskUsageExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.DiskUsage;
import com.cyanogenmod.filemanager.model.MountPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for get information about disk usage.
 */
public class DiskUsageCommand extends Program implements DiskUsageExecutable {

    private static final String TAG = "DiskUsageCommand"; //$NON-NLS-1$

    private final String mMountsFile;
    private final String mSrc;
    private final List<DiskUsage> mDisksUsage;

    /**
     * Constructor of <code>DiskUsageCommand</code>.
     *
     * @param mountsFile The system mounts file
     */
    public DiskUsageCommand(String mountsFile) {
        this(mountsFile, null);
    }

    /**
     * Constructor of <code>DiskUsageCommand</code>.
     *
     * @param mountsFile The system mounts file
     * @param dir The directory of which obtain its disk usage
     */
    public DiskUsageCommand(String mountsFile, String dir) {
        super();
        this.mMountsFile = mountsFile;
        this.mSrc = null;
        this.mDisksUsage = new ArrayList<DiskUsage>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiskUsage> getResult() {
        return this.mDisksUsage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {

        if (isTrace()) {
            Log.v(TAG,
                    String.format("Getting usage for: %s", //$NON-NLS-1$
                            this.mSrc == null ? "all" : this.mSrc)); //$NON-NLS-1$
        }

        if (this.mSrc == null) {
            // Retrieve the mount points
            MountPointInfoCommand cmd = new MountPointInfoCommand(this.mMountsFile);
            cmd.setBufferSize(getBufferSize());
            cmd.setTrace(isTrace());
            cmd.execute();
            List<MountPoint> mp = cmd.getResult();

            // Get every disk usage
            for (int i = 0; i < mp.size(); i++) {
                File root = new File(mp.get(i).getMountPoint());
                this.mDisksUsage.add(createDiskUsuage(root));
            }
        } else {
            this.mDisksUsage.add(createDiskUsuage(new File(this.mSrc)));
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

    /**
     * Method that create a reference of the disk usage from the root file
     *
     * @param root The root file
     * @return DiskUsage The disk usage
     */
    private DiskUsage createDiskUsuage(File file) {
        DiskUsage du = new DiskUsage(
                                file.getAbsolutePath(),
                                file.getTotalSpace(),
                                file.getTotalSpace() - file.getFreeSpace(),
                                file.getFreeSpace());
        if (isTrace()) {
            Log.v(TAG, du.toString());
        }
        return du;
    }

}
