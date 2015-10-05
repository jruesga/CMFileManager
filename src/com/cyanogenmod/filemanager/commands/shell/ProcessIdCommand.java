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

import com.cyanogenmod.filemanager.commands.ProcessIdExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for retrieve the process identifier of a program.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?ps"}
 */
public class ProcessIdCommand extends SyncResultProgram implements ProcessIdExecutable {

    private static final String ID_SHELL = "pid_shell";  //$NON-NLS-1$
    private static final String ID_SHELL_CMDS = "pid_shell_cmds";  //$NON-NLS-1$
    private static final String ID_CMD = "pid_cmd";  //$NON-NLS-1$
    private List<Integer> mPIDs;

    /**
     * Constructor of <code>ProcessIdCommand</code>.<br/>
     * Use this to retrieve the PID of a shell.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ProcessIdCommand() throws InvalidCommandDefinitionException {
        super(ID_SHELL);
    }

    /**
     * Constructor of <code>ProcessIdCommand</code>.<br/>
     * Use this to retrieve all PIDs running on a shell.
     *
     * @param pid The process identifier of the shell when the process is running
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ProcessIdCommand(int pid) throws InvalidCommandDefinitionException {
        super(ID_SHELL_CMDS, new String[]{String.valueOf(pid)});
    }

    /**
     * Constructor of <code>ProcessIdCommand</code>.<br/>
     * Use this to retrieve the PID of a command running on a shell.
     *
     * @param pid The process identifier of the shell when the process is running
     * @param processName The process name
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ProcessIdCommand(int pid, String processName) throws InvalidCommandDefinitionException {
        super(ID_CMD, new String[]{processName, String.valueOf(pid)});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mPIDs = new ArrayList<Integer>();

        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = br.readLine();
            if (szLine == null) {
                throw new ParseException("no information", 0); //$NON-NLS-1$
            }
            do {
                // Add every PID
                this.mPIDs.add(Integer.valueOf(szLine.trim()));

                // Next line
                szLine = br.readLine();
            } while (szLine != null);

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
    public List<Integer> getResult() {
        return this.mPIDs;
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
