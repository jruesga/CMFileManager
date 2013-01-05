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
package com.cyanogenmod.filemanager.util;

import android.util.Log;

import com.cyanogenmod.filemanager.FileManagerApplication;
import com.cyanogenmod.filemanager.commands.MountExecutable;
import com.cyanogenmod.filemanager.console.Console;
import com.cyanogenmod.filemanager.model.DiskUsage;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.MountPoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A helper class with useful methods for deal with mount points.
 */
public final class MountPointHelper {

    private static final String TAG = "MountPointHelper"; //$NON-NLS-1$

    private static final List<String> RESTRICTED_FS_TYPE = Arrays.asList(new String[]{
                                                "devpts", //$NON-NLS-1$
                                                "proc", //$NON-NLS-1$
                                                "sysfs", //$NON-NLS-1$
                                                "debugfs", //$NON-NLS-1$
                                                "cgroup", //$NON-NLS-1$
                                                "tmpfs" //$NON-NLS-1$
                                                    });

    private static final long MAX_CACHED_TIME = 60000L * 5;

    private static List<MountPoint> sMountPoints;
    private static long sLastCachedTime;

    /**
     * Constructor of <code>MountPointHelper</code>.
     */
    private MountPointHelper() {
        super();
    }

    /**
     * Method that retrieve the mount point information for a directory.
     *
     * @param dir The directory of which recovers his mount point information
     * @return MountPoint The mount point information
     */
    public static MountPoint getMountPointFromDirectory(FileSystemObject dir) {
        return getMountPointFromDirectory(dir.getFullPath());
    }

    /**
     * Method that retrieve the mount point information for a directory.
     *
     * @param dir The directory of which recovers his mount point information
     * @return MountPoint The mount point information
     */
    public static MountPoint getMountPointFromDirectory(String dir) {
        try {
            return getMountPointFromDirectory(FileManagerApplication.getBackgroundConsole(), dir);
        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve mount point information.", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Method that retrieve the mount point information for a directory.
     *
     * @param console The console in which realize the operation
     * @param dir The directory of which recovers his mount point information
     * @return MountPoint The mount point information
     */
    public synchronized static MountPoint getMountPointFromDirectory(Console console, String dir) {
        try {
            // For non-rooted devices, which console is java and runs under a chrooted
            // device, mount point info mustn't be a main objective. Caching the status
            // should be enough and operation runs smoothly.
            // Refresh mount points after some time (5 minutes should be enough)
            long now = System.currentTimeMillis();
            if (sMountPoints == null || (now - sLastCachedTime) > MAX_CACHED_TIME ||
                FileManagerApplication.isDeviceRooted()) {
                //Retrieve the mount points
                List<MountPoint> mps =
                        CommandHelper.getMountPoints(null, console);
                sMountPoints = mps;
                sLastCachedTime = now;
            }

            //Sort mount points in reverse order, needed for avoid
            //found an incorrect mount point that matches the name
            Collections.sort(sMountPoints, new Comparator<MountPoint>() {
                @Override
                public int compare(MountPoint lhs, MountPoint rhs) {
                    return lhs.compareTo(rhs) * -1;
                }
            });

            //Search for the mount point information
            int cc = sMountPoints.size();
            for (int i = 0; i < cc; i++) {
                MountPoint mp = sMountPoints.get(i);
                if (dir.startsWith(mp.getMountPoint())) {
                    return mp;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve the mount point information", e); //$NON-NLS-1$
        }

        //No mount point found
        return null;
    }

    /**
     * Method that retrieve information about the disk usage of the mount point.
     *
     * @param mp The mount point
     * @return DiskUsage The disk usage information
     */
    public static DiskUsage getMountPointDiskUsage(MountPoint mp) {
        return getMountPointDiskUsage(FileManagerApplication.getBackgroundConsole(), mp);
    }

    /**
     * Method that retrieve information about the disk usage of the mount point.
     *
     * @param console The console in which realize the operation
     * @param mp The mount point
     * @return DiskUsage The disk usage information
     */
    public static DiskUsage getMountPointDiskUsage(Console console, MountPoint mp) {
        try {
            //Retrieve the mount points
            return CommandHelper.getDiskUsage(null, mp.getMountPoint(), console);

        } catch (Exception e) {
            Log.e(TAG,
                    String.format("Fail to load disk usage of mount point: %s",  //$NON-NLS-1$
                            mp.getMountPoint()), e);
        }

        //No mount point found
        return null;
    }

    /**
     * Method that returns if the filesystem is mounted as readonly.
     *
     * @param mp The mount point to check
     * @return boolean If the mount point is mounted as readonly
     */
    public static boolean isReadOnly(MountPoint mp) {
        try {
            return mp.getOptions().startsWith(MountExecutable.READONLY);
        } catch (Exception e) {
            Log.e(TAG, "Method \"isReadOnly\" failed.", e); //$NON-NLS-1$
        }

        //On fail is more secure consider it as read-only
        return true;
    }

    /**
     * Method that returns if the filesystem is mounted as read-write.
     *
     * @param mp The mount point to check
     * @return boolean If the mount point is mounted as read-write
     */
    public static boolean isReadWrite(MountPoint mp) {
        try {
            return mp.getOptions().startsWith(MountExecutable.READWRITE);
        } catch (Exception e) {
            Log.e(TAG, "Method \"isReadWrite\" failed.", e); //$NON-NLS-1$
        }

        //On fail is more secure consider it as read-only
        return false;
    }

    /**
     * Method that returns if a filesystem is allowed to be mounted/unmounted (rw/ro).
     *
     * @param mp The mount point to check
     * @return boolean If the mount point can be mounted/unmount (rw/ro)
     */
    public static boolean isMountAllowed(MountPoint mp) {
        return !RESTRICTED_FS_TYPE.contains(mp.getType());
    }
}
