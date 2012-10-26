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

import com.cyanogenmod.filemanager.commands.ChangeCurrentDirExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;


/**
 * A class for change the current directory of the shell.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?cd"}
 */
public class ChangeCurrentDirCommand
    extends SyncResultProgram implements ChangeCurrentDirExecutable {

    private static final String ID = "cd";  //$NON-NLS-1$
    private Boolean mRet;

    /**
     * Constructor of <code>ChangeCurrentDirCommand</code>.
     *
     * @param newDir The new directory to which to change
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ChangeCurrentDirCommand(String newDir) throws InvalidCommandDefinitionException {
        super(ID, newDir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mRet = Boolean.TRUE;

        // Check the in and err buffer to extract information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(err));
            String szLine = br.readLine();
            if (szLine != null) {
                if (szLine.indexOf("No such file or directory") != -1) { //$NON-NLS-1$
                    this.mRet = Boolean.FALSE;
                }
            }
            br.close();
            br = new BufferedReader(new StringReader(in));
            szLine = br.readLine();
            if (szLine != null) {
                if (szLine.indexOf("No such file or directory") != -1) { //$NON-NLS-1$
                    this.mRet = Boolean.FALSE;
                }
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
