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
 * An abstract class that allow the consumption of the data when it's totally recovery.
 */
public abstract class SyncResultProgram extends Program implements SyncResultProgramListener {

    /**
     * @Constructor of <code>SyncResultProgram</code>
     *
     * @param id The resource identifier of the command
     * @param args Arguments of the command (will be formatted with the arguments from
     * the command definition)
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public SyncResultProgram(String id, String... args) throws InvalidCommandDefinitionException {
        super(id, args);
    }

    /**
     * @Constructor of <code>SyncResultProgram</code>
     *
     * @param id The resource identifier of the command
     * @param prepare Indicates if the argument must be prepared
     * @param args Arguments of the command (will be formatted with the arguments from
     * the command definition)
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public SyncResultProgram(String id, boolean prepare, String... args)
            throws InvalidCommandDefinitionException {
        super(id, prepare, args);
    }

}
