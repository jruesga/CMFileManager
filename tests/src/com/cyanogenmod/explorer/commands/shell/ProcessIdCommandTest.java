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

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.util.CommandHelper;

/**
 * A class for testing the {@link ProcessIdCommand} command.
 *
 * @see ProcessIdCommand
 */
public class ProcessIdCommandTest extends AbstractConsoleTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test over id command.
     *
     * @throws Exception If test failed
     */
    public void testId() throws Exception {
        Integer main = new Integer(android.os.Process.myPid());
        Integer pid =
                CommandHelper.getProcessId(
                        getContext(),
                        ExplorerApplication.MAIN_PROCESS, getConsole());
        assertNotNull("pid==null", pid); //$NON-NLS-1$
        assertTrue(
                String.format("pid != main process id (%d)", main),  //$NON-NLS-1$
                pid.compareTo(main) == 0);
    }

}
