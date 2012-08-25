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

import com.cyanogenmod.explorer.commands.Executable;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.console.NoSuchFileOrDirectory;


/**
 * An abstract class that represents a command that need a shell
 * to be executed.
 */
public abstract class Program extends Command implements Executable {

    /**
     * @Constructor of <code>Program</code>
     *
     * @param id The resource identifier of the command
     * @param args Arguments of the command (will be formatted with the arguments from
     * the command definition)
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public Program(String id, String... args) throws InvalidCommandDefinitionException {
        super(id, args);
    }

    /**
     * @Constructor of <code>Program</code>
     *
     * @param id The resource identifier of the command
     * @param prepare Indicates if the argument must be prepared
     * @param args Arguments of the command (will be formatted with the arguments from
     * the command definition)
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public Program(String id, boolean prepare, String... args)
            throws InvalidCommandDefinitionException {
        super(id, prepare, args);
    }

    /**
     * Method that returns if the standard error must be
     * ignored safely by the shell, and don't check for errors
     * like <code>NoSuchFileOrDirectory</code> or
     * <code>Permission denied</code> by the shell.
     *
     * @return boolean If the standard error must be ignored
     * @hide
     */
    @SuppressWarnings("static-method")
    public boolean isIgnoreShellStdErrCheck() {
        return false;
    }

    /**
     * Method that checks if the standard errors has exceptions.
     *
     * @param exitCode Program exit code
     * @param err Standard Error buffer
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws CommandNotFoundException If the command was not found
     * @throws ExecutionException If the another exception is detected in the standard error
     * @hide
     */
    @SuppressWarnings("unused")
    public void checkStdErr(int exitCode, String err)
            throws InsufficientPermissionsException, NoSuchFileOrDirectory,
            CommandNotFoundException, ExecutionException {
        /**NON BLOCK**/
    }

}
