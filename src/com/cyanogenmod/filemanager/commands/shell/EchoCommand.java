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

import com.cyanogenmod.filemanager.commands.EchoExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;


/**
 * A class for retrieve the value of a variable.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?echo"}
 */
public class EchoCommand extends SyncResultProgram implements EchoExecutable {

    private static final String ID = "echo";  //$NON-NLS-1$
    private String mEcho;

    /**
     * Constructor of <code>EchoCommand</code>.
     *
     * @param msg The message to echo. This message can have one or multiple variables
     * and text. xe: "This is $VAR_1 the value of $VAR2" or simple "$PATH"
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public EchoCommand(String msg) throws InvalidCommandDefinitionException {
        super(ID, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mEcho = ""; //$NON-NLS-1$

        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = br.readLine();
            if (szLine != null) {
                this.mEcho = szLine;
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
    public String getResult() {
        return this.mEcho;
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
