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

import com.cyanogenmod.filemanager.commands.ResolveLinkExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.ParseHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.ParseException;


/**
 * A class for retrieve the real file name of a symlink. This command
 * can be used too for retrieve the absolute path of a file or directory
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?readlink"}
 */
public class ResolveLinkCommand extends SyncResultProgram implements ResolveLinkExecutable {

    private static final String ID = "readlink";  //$NON-NLS-1$
    private FileSystemObject mFso;

    /**
     * Constructor of <code>ResolveLinkCommand</code>.
     *
     * @param src The file system object to read
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ResolveLinkCommand(String src) throws InvalidCommandDefinitionException {
        super(ID, src,
                (src.compareTo(FileHelper.ROOT_DIRECTORY) == 0) ?
                   FileHelper.ROOT_DIRECTORY :
                   new File(src).getParentFile().getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(in));

            // Extract and parse the stat output
            String line = br.readLine();
            this.mFso = ParseHelper.parseStatOutput(line);

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
    public FileSystemObject getResult() {
        return this.mFso;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {
        /**NON BLOCK**/
    }
}
