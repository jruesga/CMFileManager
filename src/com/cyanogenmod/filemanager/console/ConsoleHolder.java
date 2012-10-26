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

package com.cyanogenmod.filemanager.console;

/**
 * A class that holds a console.
 */
public class ConsoleHolder {

    private final Console mConsole;
    private boolean mDispose;

    /**
     * Constructor of <code>ConsoleHolder</code>.
     *
     * @param console An allocated console to be holden
     */
    public ConsoleHolder(Console console) {
        super();
        this.mConsole = console;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    /**
     * Method that returns the console.
     *
     * @return The console
     */
    public Console getConsole() {
        return this.mConsole;
    }

    /**
     * Method that returns if the console is disposed.
     *
     * @return boolean If the console is disposed
     */
    public boolean isDispose() {
        return this.mDispose;
    }

    /**
     * Method that dispose the console.
     */
    public void dispose() {
        try {
            if (this.mConsole != null) {
                this.mConsole.dealloc();
            }
        } catch (Exception e) {
            /**NON BLOCK**/
        }
    }

}
