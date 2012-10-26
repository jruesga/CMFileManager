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


/**
 * A class that represent a privileged command invocation. This command
 * allow to get elevated privileged before invoke other command
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?sh"}
 */
public class BashShell extends Shell {

    private static final String ID = "bash";  //$NON-NLS-1$

    /**
     * Constructor of <code>BashShell</code>.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public BashShell() throws InvalidCommandDefinitionException {
        super(ID);
    }

}
