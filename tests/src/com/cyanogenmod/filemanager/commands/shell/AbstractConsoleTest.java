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

import com.cyanogenmod.filemanager.FileManagerApplication;
import com.cyanogenmod.filemanager.console.Console;
import com.cyanogenmod.filemanager.console.ConsoleBuilder;
import com.cyanogenmod.filemanager.console.shell.ShellConsole;
import com.cyanogenmod.filemanager.util.FileHelper;

/**
 * An abstract class that manages tests that needs a console.
 */
public abstract class AbstractConsoleTest extends android.test.AndroidTestCase {

    private static final String INITIAL_DIR = FileHelper.ROOT_DIRECTORY;

    private Console mConsole;

    /**
     * Method that returns if a root console is need to be allocated.
     *
     * @return boolean If a root console is need to be allocated
     */
    public abstract boolean isRootConsoleNeeded();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        //Setup the console
        if (isRootConsoleNeeded()) {
            FileManagerApplication.changeBackgroundConsoleToPriviligedConsole();
            this.mConsole = ConsoleBuilder.createPrivilegedConsole(getContext(), INITIAL_DIR);
        } else {
            this.mConsole = ConsoleBuilder.createNonPrivilegedConsole(getContext(), INITIAL_DIR);
        }

        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        //Deallocate console
        if (this.mConsole != null) {
            try {
                this.mConsole.dealloc();
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
        this.mConsole = null;

        super.tearDown();
    }

    /**
     * Method that returns the console created for test.
     *
     * @return Console The console created for test
     */
    public ShellConsole getConsole() {
        return (ShellConsole)this.mConsole;
    }

}
