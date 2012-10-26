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

package com.cyanogenmod.filemanager.commands.shell;

import com.cyanogenmod.filemanager.commands.MountExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.MountPoint;

import java.text.ParseException;


/**
 * A class for mount filesystem.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?mount"}
 */
public class MountCommand extends SyncResultProgram implements MountExecutable {

    private static final String ID = "mount";  //$NON-NLS-1$
    private Boolean mRet;

    /**
     * Constructor of <code>MountCommand</code>.
     *
     * @param mp The mount point to mount
     * @param rw Indicates if the operation mount the device as read-write
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public MountCommand(MountPoint mp, boolean rw) throws InvalidCommandDefinitionException {
        super(ID, false, rw ? READWRITE : READONLY, mp.getDevice(), mp.getMountPoint());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mRet = Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getResult() {
        return this.mRet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {
        if (exitCode != 0) {
            throw new ExecutionException("exitcode != 0"); //$NON-NLS-1$
        }
    }
}
