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

import com.cyanogenmod.filemanager.commands.QuickFolderSearchExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A class for quickly get the name of directories that matches a string.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?ls"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?grep"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?awk"}
 */
public class QuickFolderSearchCommand
    extends SyncResultProgram implements QuickFolderSearchExecutable {

    private static final String ID = "quickfoldersearch";  //$NON-NLS-1$

    private final List<String> mQuickFolders;

    /**
     * Constructor of <code>QuickFolderSearchCommand</code>.
     *
     * @param regexp The expression to search
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public QuickFolderSearchCommand(String regexp) throws InvalidCommandDefinitionException {
        super(ID, regexp);
        this.mQuickFolders = new ArrayList<String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the array
        this.mQuickFolders.clear();

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
                this.mQuickFolders.add(szLine + File.separator);
                line++;
            }

            //Sort the data
            Collections.sort(this.mQuickFolders);

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
    public List<String> getResult() {
        return this.mQuickFolders;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWaitOnNewDataReceipt() {
        return true;
    }

}
