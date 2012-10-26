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

import com.cyanogenmod.filemanager.commands.SIGNAL;
import com.cyanogenmod.filemanager.commands.SendSignalExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;

import java.text.ParseException;


/**
 * A class that represents a command for send signal to processes
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?kill"}
 */
public class SendSignalCommand extends SyncResultProgram implements SendSignalExecutable {

    private static final String ID_SIGNAL = "sendsignal";  //$NON-NLS-1$
    private static final String ID_TERMINATE = "terminate";  //$NON-NLS-1$

    /**
     * Constructor of <code>SendSignalCommand</code>.
     *
     * @param process The process which to send the signal
     * @param signal The signal to send
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public SendSignalCommand(int process, SIGNAL signal) throws InvalidCommandDefinitionException {
        super(ID_SIGNAL, String.valueOf(signal.getSignal()), String.valueOf(process));
    }

    /**
     * Constructor of <code>SendSignalCommand</code>. This method sends a kill (terminate)
     *
     * @param process The process which to send the signal
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public SendSignalCommand(int process) throws InvalidCommandDefinitionException {
        super(ID_TERMINATE, String.valueOf(process));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {/**NON BLOCK**/}

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getResult() {
        // Always true. If fails, the checkExitCode will raise an exception
        return Boolean.TRUE;
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
