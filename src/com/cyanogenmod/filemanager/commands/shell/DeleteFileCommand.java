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

import com.cyanogenmod.filemanager.commands.DeleteFileExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.MountPointHelper;

import java.text.ParseException;


/**
 * A class for delete a file (regular file).
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?rm"}
 */
public class DeleteFileCommand extends SyncResultProgram implements DeleteFileExecutable {

    private static final String ID = "rm";  //$NON-NLS-1$
    private Boolean mRet;
    private final String mFileName;

    /**
     * Constructor of <code>DeleteFileCommand</code>.
     *
     * @param fileName The name of the file to be deleted
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public DeleteFileCommand(String fileName) throws InvalidCommandDefinitionException {
        super(ID, fileName);
        this.mFileName = fileName;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPoint getSrcWritableMountPoint() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPoint getDstWritableMountPoint() {
        return MountPointHelper.getMountPointFromDirectory(this.mFileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIndefinitelyWait() {
        return true;
    }
}
