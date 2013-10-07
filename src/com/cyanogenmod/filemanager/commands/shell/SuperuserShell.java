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

import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.console.ReadOnlyFilesystemException;
import com.cyanogenmod.filemanager.util.ShellHelper;



/**
 * A class that represent a privileged command invocation. This command
 * allow to get elevated privileged before invoke other command
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?su"}
 */
public class SuperuserShell extends Shell {

    private static final String ID = "su";  //$NON-NLS-1$

    private static final String[] MOUNT_STORAGE_ENV = new String[] {
        "MOUNT_EMULATED_STORAGE=1"
    };

    /**
     * Constructor of <code>SuperuserShell</code>.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public SuperuserShell() throws InvalidCommandDefinitionException {
        super(ID, ShellHelper.getProgramCmdLine(new BashShell()));
    }

    /**
     * {@inheritDoc}
     */
    public String[] getEnvironment() {
        return MOUNT_STORAGE_ENV;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {
        if (exitCode != 0) {
            //Check result
            super.checkExitCode(exitCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkStdErr(Program program, int exitCode, String err)
            throws InsufficientPermissionsException, NoSuchFileOrDirectory,
            CommandNotFoundException, ExecutionException, ReadOnlyFilesystemException {
        if (exitCode != 0) {
            if (err.startsWith(": not found")) { //$NON-NLS-1$
                throw new CommandNotFoundException(err);
            }
            super.checkStdErr(program, exitCode, err);
        }
    }

}
