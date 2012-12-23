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

package com.cyanogenmod.filemanager.util;

import com.cyanogenmod.filemanager.commands.shell.Command;

/**
 * A helper class with useful methods for deal with linux shells.
 */
public final class ShellHelper {

    /**
     * Constructor of <code>ShellHelper</code>.
     */
    private ShellHelper() {
        super();
    }


    /**
     * Method that prepares an argument to be executed. This method
     * ensures that the arguments no have malicious code
     *
     * @param arg Argument
     * @return String The prepared argument
     */
    public static String prepareArgument(final String arg) {
        if (arg == null) {
            return null;
        }
        String preparedArgs = arg.replace("\"", "\\\""); //$NON-NLS-1$//$NON-NLS-2$
        preparedArgs = preparedArgs.replace("$", "\\$"); //$NON-NLS-1$//$NON-NLS-2$
        return preparedArgs;
    }

    /**
     * Method that returns the command line of a program command to be used as part as the
     * arguments of the shell command.
     *
     * @param command The program command to parse
     * @return String The  command line of a program command
     */
    public static String getProgramCmdLine(Command command) {
        //The command line must be like "<cmd> <args>" with double quotes, replacing
        //all double quotes <args> by \"
        String args = command.getArguments();
        if (args == null) {
            args = ""; //$NON-NLS-1$
        }
        StringBuilder subcmd = new StringBuilder();
        subcmd.append(command.getCommand())
              .append(" ") //$NON-NLS-1$
              .append(args);

        //Prepare and build command string
        return ShellHelper.prepareArgument(subcmd.toString());
    }

}
