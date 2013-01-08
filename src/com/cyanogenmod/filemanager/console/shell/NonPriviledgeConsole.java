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

package com.cyanogenmod.filemanager.console.shell;

import com.cyanogenmod.filemanager.commands.shell.BashShell;
import com.cyanogenmod.filemanager.commands.shell.InvalidCommandDefinitionException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A class that represents a non privileged shell console.
 *
 * @see ShellConsole
 * @see BashShell
 */
public class NonPriviledgeConsole extends ShellConsole {

    /**
     * Constructor of <code>NonPriviledgeConsole</code>.
     *
     * @throws FileNotFoundException If the default initial directory not exists
     * @throws IOException If initial directory couldn't be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public NonPriviledgeConsole()
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException {
        super(new BashShell());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivileged() {
        return false;
    }

}
