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

import android.util.Log;

import com.cyanogenmod.explorer.commands.ChangeOwnerExecutable;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.console.NoSuchFileOrDirectory;
import com.cyanogenmod.explorer.model.Group;
import com.cyanogenmod.explorer.model.MountPoint;
import com.cyanogenmod.explorer.model.User;
import com.cyanogenmod.explorer.util.MountPointHelper;


/**
 * A class for change the owner of an object.
 */
public class ChangeOwnerCommand extends Program implements ChangeOwnerExecutable {

    private static final String TAG = "ChangeOwnerCommand"; //$NON-NLS-1$

    private final String mPath;
    private final User mNewUser;
    private final Group mNewGroup;

    /**
     * Constructor of <code>ChangeOwnerCommand</code>.
     *
     * @param path The name of the file or directory to be moved
     * @param newUser The new user owner of the object
     * @param newGroup The new group owner of the object
     */
    public ChangeOwnerCommand(String path, User newUser, Group newGroup) {
        super();
        this.mPath = path;
        this.mNewUser = newUser;
        this.mNewGroup = newGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getResult() {
        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {
        if (isTrace()) {
            Log.v(TAG,
                    String.format(
                            "Changing owner to %s. New user: %s. New group: %s", //$NON-NLS-1$
                            this.mPath, this.mNewUser.toString(), this.mNewGroup.toString()));
        }

        // Java doesn't allow to change the owner
        if (isTrace()) {
            Log.v(TAG, "Result: FAIL. InsufficientPermissionsException"); //$NON-NLS-1$
        }
        throw new InsufficientPermissionsException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPoint getWritableMountPoint() {
        return MountPointHelper.getMountPointFromDirectory(this.mPath);
    }

}
