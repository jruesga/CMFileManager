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

import android.test.suitebuilder.annotation.SmallTest;


/**
 * A class for testing list command.
 *
 * @see ConsoleBuilder
 */
public class ConsoleBuilderTest extends android.test.AndroidTestCase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Method that performs a test over creating a privileged console.
     *
     * @throws Exception If test failed
     * @{link {@link ConsoleBuilder#createPrivilegedConsole(android.content.Context)}
     */
    @SmallTest
    public void testCreatePrivilegedConsole() throws Exception {
        Console console = ConsoleBuilder.createPrivilegedConsole(getContext());
        try {
            assertNotNull("console==null", console); //$NON-NLS-1$
        } finally {
            try {
                console.dealloc();
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }

    /**
     * Method that performs a test over creating a non privileged console.
     *
     * @throws Exception If test failed
     * @{link {@link ConsoleBuilder#createNonPrivilegedConsole(android.content.Context)}
     */
    @SmallTest
    public void testCreateNonPrivilegedConsole() throws Exception {
        Console console = ConsoleBuilder.createNonPrivilegedConsole(getContext());
        try {
            assertNotNull("console==null", console); //$NON-NLS-1$
        } finally {
            try {
                console.dealloc();
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }


}
