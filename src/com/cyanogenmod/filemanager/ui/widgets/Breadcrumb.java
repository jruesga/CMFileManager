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

package com.cyanogenmod.filemanager.ui.widgets;

import com.cyanogenmod.filemanager.model.DiskUsage;
import com.cyanogenmod.filemanager.model.MountPoint;

/**
 * An interface that defines the breadcrumb operations.
 */
public interface Breadcrumb {

    /**
     * Method that initializes the loading of data.
     */
    void startLoading();

    /**
     * Method that finalizes the loading of data.
     */
    void endLoading();

    /**
     * Method that changes the path of the breadcrumb.
     *
     * @param newPath The new path
     * @param chRooted If the breadcrumb should be in a ChRooted environment
     */
    void changeBreadcrumbPath(final String newPath, boolean chRooted);

    /**
     * Method that adds a new breadcrumb listener.
     *
     * @param listener The breadcrumb listener to add
     */
    void addBreadcrumbListener(BreadcrumbListener listener);

    /**
     * Method that adds an active breadcrumb listener.
     *
     * @param listener The breadcrumb listener to remove
     */
    void removeBreadcrumbListener(BreadcrumbListener listener);

    /**
     * Method that updates the info and statistics of the current mount point.
     */
    void updateMountPointInfo();

    /**
     * Method that sets the free disk space percentage after the widget change his color
     * to advise the user
     *
     * @param percentage The free disk space percentage
     */
    void setFreeDiskSpaceWarningLevel(int percentage);

    /**
     * Method that returns the active {@link MountPoint} reference.
     *
     * @return MountPoint The active {@link MountPoint}
     */
    MountPoint getMountPointInfo();

    /**
     * Method that returns the active {@link DiskUsage} reference.
     *
     * @return DiskUsage The active {@link DiskUsage}
     */
    DiskUsage getDiskUsageInfo();

    /**
     * Method that applies the current theme to the breadcrumb
     */
    void applyTheme();
}
