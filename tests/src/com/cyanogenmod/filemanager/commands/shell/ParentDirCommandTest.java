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

import android.os.Environment;
import android.test.suitebuilder.annotation.SmallTest;

import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the method for retrieve the value of a variable.
 *
 * @see ParentDirCommand
 */
public class ParentDirCommandTest extends AbstractConsoleTest {

    private static final String FILE =
            Environment.getDataDirectory().getAbsolutePath() + "/parentdirtest.txt"; //$NON-NLS-1$
    private static final String PARENT = Environment.getDataDirectory().getAbsolutePath();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a test to get the parent directory of a known file.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testParentDirOk() throws Exception {
        try {
            CommandHelper.createFile(getContext(), FILE, getConsole());
            String parent = CommandHelper.getParentDir(getContext(), FILE, getConsole());
            assertNotNull("parent==null", parent); //$NON-NLS-1$
            assertTrue(
                    String.format(
                            "parent!=%s", PARENT), parent.compareTo(PARENT) == 0); //$NON-NLS-1$
        } finally {
            try {
                CommandHelper.deleteFile(getContext(), FILE, getConsole());
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }


}
