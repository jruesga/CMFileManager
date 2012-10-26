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

import com.cyanogenmod.filemanager.commands.MountPointInfoExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.ParseHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for get information about disk usage.
 */
public class MountPointInfoCommand extends Program implements MountPointInfoExecutable {

    private static final String TAG = "MountPointInfoCommand"; //$NON-NLS-1$

    private final String mMountsFile;
    private final List<MountPoint> mMountPoints;

    /**
     * Constructor of <code>MountPointInfoCommand</code>.
     *
     * @param mountsFile The system mounts file
     */
    public MountPointInfoCommand(String mountsFile) {
        super();
        this.mMountPoints = new ArrayList<MountPoint>();
        this.mMountsFile = mountsFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MountPoint> getResult() {
        return this.mMountPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {

        if (isTrace()) {
            Log.v(TAG,
                    String.format("Getting usage from %s", //$NON-NLS-1$
                            this.mMountsFile));
        }

        // Read the file with the mount information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(this.mMountsFile)), getBufferSize());
            StringBuilder sb = new StringBuilder();
            int read = 0;
            char[] data = new char[getBufferSize()];
            while ((read = br.read(data, 0, getBufferSize())) != -1) {
                sb.append(data, 0, read);
            }

            // Send to parse
            String[] lines = sb.toString().split("\n"); //$NON-NLS-1$
            for (int i = 0; i < lines.length; i++) {
                MountPoint mp = ParseHelper.toMountPoint(lines[i]);
                if (isTrace()) {
                    Log.v(TAG, String.valueOf(mp));
                }
                this.mMountPoints.add(mp);
            }

        } catch (Exception e) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. InsufficientPermissionsException"); //$NON-NLS-1$
            }
            throw new InsufficientPermissionsException();

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Throwable _throw) {/**NON BLOCK**/}
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

}
