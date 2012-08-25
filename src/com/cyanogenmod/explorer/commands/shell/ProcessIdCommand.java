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

package com.cyanogenmod.explorer.commands.shell;

import com.cyanogenmod.explorer.commands.ProcessIdExecutable;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;


/**
 * A class for retrieve the process identifier of a program.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?ps"}
 */
public class ProcessIdCommand extends SyncResultProgram implements ProcessIdExecutable {

    private static final String ID = "pid";  //$NON-NLS-1$
    private Integer mPID;

    /**
     * Constructor of <code>ProcessIdCommand</code>.
     *
     * @param processName The process name
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ProcessIdCommand(String processName) throws InvalidCommandDefinitionException {
        super(ID, processName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mPID = null;

        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = br.readLine();
            if (szLine == null) {
                throw new ParseException("no information", 0); //$NON-NLS-1$
            }

            //Get the PID
            this.mPID = new Integer(szLine.trim());

        } catch (IOException ioEx) {
            throw new ParseException(ioEx.getMessage(), 0);

        } catch (ParseException pEx) {
            throw pEx;

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
    public Integer getResult() {
        return this.mPID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException,
            ExecutionException {
        /**NON BLOCK**/
    }

}
