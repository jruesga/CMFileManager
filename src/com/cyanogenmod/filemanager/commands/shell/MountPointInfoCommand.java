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

import com.cyanogenmod.filemanager.commands.MountPointInfoExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.ParseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for get information about disk usage.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?cat"}
 * {@link
 *     "http://linux.web.cern.ch/linux/scientific5/docs/rhel/Deployment_Guide/s2-proc-mounts.html"}
 */
public class MountPointInfoCommand extends SyncResultProgram implements MountPointInfoExecutable {

    private static final String ID = "mountpointinfo";  //$NON-NLS-1$

    private final List<MountPoint> mMountPoints;

    /**
     * Constructor of <code>MountPointInfoCommand</code>.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public MountPointInfoCommand() throws InvalidCommandDefinitionException {
        super(ID, new String[]{});
        this.mMountPoints = new ArrayList<MountPoint>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the array
        this.mMountPoints.clear();

        // Check the in buffer to extract information
        BufferedReader br = null;
        int line = 0;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = null;
            while ((szLine = br.readLine()) != null) {
                //Checks that there is some text in the line. Otherwise ignore it
                if (szLine.trim().length() == 0) {
                    break;
                }

                //Parse the line into a MountPoint reference
                try {
                    this.mMountPoints.add(ParseHelper.toMountPoint(szLine));
                } catch (ParseException pEx) {
                    //Ignore
                }

                line++;
            }

        } catch (IOException ioEx) {
            throw new ParseException(ioEx.getMessage(), line);

        } catch (Exception ex) {
            throw new ParseException(ex.getMessage(), line);

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
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
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {
        if (exitCode != 0 && exitCode != 1) {  //Permission denied
            throw new ExecutionException("exitcode != 0 && != 1"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIgnoreShellStdErrCheck() {
        return true;
    }

}
