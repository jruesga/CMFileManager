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

import android.test.suitebuilder.annotation.SmallTest;

import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the method for retrieve the value of a variable.
 *
 * @see EchoCommand
 */
public class EchoCommandTest extends AbstractConsoleTest {

    private static final String ECHO_MSG = "$PATH"; //$NON-NLS-1$
    private static final String ECHO_MSG_VOID = "$PATH1234"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test to get the a variable.
     * list command
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testEchoOk() throws Exception {
        String echo = CommandHelper.getVariable(getContext(), ECHO_MSG, getConsole());
        assertNotNull("echo==null", echo); //$NON-NLS-1$
    }

    /**
     * Method that performs a test to a void variable.
     * list command
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testEchoVoid() throws Exception {
        String echo = CommandHelper.getVariable(getContext(), ECHO_MSG_VOID, getConsole());
        assertNotNull("exitCode==null", echo); //$NON-NLS-1$
        assertTrue("echo!=0", echo.length() == 0); //$NON-NLS-1$
    }


}
