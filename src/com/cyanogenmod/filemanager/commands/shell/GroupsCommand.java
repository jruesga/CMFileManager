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

import com.cyanogenmod.filemanager.commands.GroupsExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for retrieve information of the groups of the current user
 * (user associated to the active shell).
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?groups"}
 */
public class GroupsCommand extends SyncResultProgram implements GroupsExecutable {

    private static final String ID = "groups";  //$NON-NLS-1$
    private List<Group> mGroups;

    /**
     * Constructor of <code>GroupsCommand</code>.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public GroupsCommand() throws InvalidCommandDefinitionException {
        super(ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mGroups = new ArrayList<Group>();

        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            if (in == null) {
                return;
            }
            String szIn = in.replaceAll(" ", FileHelper.NEWLINE); //$NON-NLS-1$
            br = new BufferedReader(new StringReader(szIn));
            String szLine = null;
            while ((szLine = br.readLine()) != null) {
                this.mGroups.add(new Group(-1, szLine.trim()));
            }

        } catch (IOException ioEx) {
            throw new ParseException(ioEx.getMessage(), 0);

        } catch (Exception ex) {
            throw new ParseException(ex.getMessage(), 0);

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
    public List<Group> getResult() {
        return this.mGroups;
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
