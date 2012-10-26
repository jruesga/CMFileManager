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

import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link CreateFileCommand} command.
 *
 * @see CreateFileCommand
 */
public class CreateFileCommandTest extends AbstractConsoleTest {

    private static final String PATH_NEWFILE_OK =
            Environment.getDataDirectory().getAbsolutePath() + "/newtest.txt"; //$NON-NLS-1$
    private static final String PATH_NEWFILE_ERROR = "/foo/foo121212/newtest.txt"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a test to create a new file.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testCreateFileOk() throws Exception {
        try {
            boolean ret = CommandHelper.createFile(getContext(), PATH_NEWFILE_OK, getConsole());
            assertTrue("response==false", ret); //$NON-NLS-1$
        } finally {
            try {
                CommandHelper.deleteFile(getContext(), PATH_NEWFILE_OK, getConsole());
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }

    /**
     * Method that performs a test to create a new invalid file.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testCreateFileFail() throws Exception {
        try {
            CommandHelper.createFile(getContext(), PATH_NEWFILE_ERROR, getConsole());
            assertTrue("exit code==0", false); //$NON-NLS-1$
        } catch (NoSuchFileOrDirectory error) {
          //This command must failed. exit code !=0
        } finally {
            try {
                CommandHelper.deleteFile(getContext(), PATH_NEWFILE_ERROR, getConsole());
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }


}
