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

package com.cyanogenmod.filemanager.commands;

import com.cyanogenmod.filemanager.model.MountPoint;

/**
 * An interface that represents an executable that writes in a filesystem.<br/>
 * All write operations must be done over a synchronous way.
 */
public interface WritableExecutable extends SyncResultExecutable {

    /**
     * Method that return the source mount point that the program use to write.
     *
     * @return MountPoint The source mount point reference.
     */
    MountPoint getSrcWritableMountPoint();

    /**
     * Method that return the destination mount point that the program use to write.
     *
     * @return MountPoint The destination mount point reference.
     */
    MountPoint getDstWritableMountPoint();
}
