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

import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the method for retrieve the value of a variable.
 *
 * @see ResolveLinkCommand
 */
public class ResolveLinkCommandTest extends AbstractConsoleTest {

    private static final String LINK = "/d"; //$NON-NLS-1$
    private static final String REAL_FILE = "/sys/kernel/debug"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test to get the real file of a symlink.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testReadLinkOk() throws Exception {
        FileSystemObject fso = CommandHelper.resolveSymlink(getContext(), LINK, getConsole());
        assertNotNull("fso==null)", fso); //$NON-NLS-1$
        assertTrue(
                String.format("parent!=%s", REAL_FILE), //$NON-NLS-1$
                fso.getFullPath().compareTo(REAL_FILE) == 0);
    }


}
