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

import com.cyanogenmod.filemanager.commands.DiskUsageExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.DiskUsage;
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
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?df"}
 */
public class DiskUsageCommand extends SyncResultProgram implements DiskUsageExecutable {

    private static final String ID = "diskusage";  //$NON-NLS-1$
    private static final String ID_ALL = "diskusageall";  //$NON-NLS-1$

    private final List<DiskUsage> mDisksUsage;

    /**
     * Constructor of <code>DiskUsageCommand</code>.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public DiskUsageCommand() throws InvalidCommandDefinitionException {
        super(ID_ALL, new String[]{});
        this.mDisksUsage = new ArrayList<DiskUsage>();
    }

    /**
     * Constructor of <code>DiskUsageCommand</code>.
     *
     * @param dir The directory of which obtain its disk usage
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public DiskUsageCommand(String dir) throws InvalidCommandDefinitionException {
        super(ID, new String[]{dir});
        this.mDisksUsage = new ArrayList<DiskUsage>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the array
        this.mDisksUsage.clear();

        // Check the in buffer to extract information
        BufferedReader br = null;
        int line = 0;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = br.readLine();  //The first line must be ignored
            while ((szLine = br.readLine()) != null) {
                //Checks that there is some text in the line. Otherwise ignore it
                if (szLine.trim().length() == 0) {
                    break;
                }

                //Parse the line into a DiskUsage reference
                try {
                    this.mDisksUsage.add(ParseHelper.toDiskUsage(szLine));
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
    public List<DiskUsage> getResult() {
        return this.mDisksUsage;
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
