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

import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.FileHelper;

/**
 * A class for testing the {@link ChangeCurrentDirCommand} command.
 *
 * @see ChangeCurrentDirCommand
 */
public class ChangeCurrentDirCommandTest extends AbstractConsoleTest {

    private static final String PATH_OK = FileHelper.ROOT_DIRECTORY;
    private static final String PATH_ERROR = "/foo/foo121212"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test to change the directory.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testChangeDirOk() throws Exception {
        boolean ret = CommandHelper.changeCurrentDir(getContext(), PATH_OK, getConsole());
        assertTrue("response==false", ret); //$NON-NLS-1$

        //Verify that current directory is PATH_OK
        String curDir = CommandHelper.getCurrentDir(getContext(), getConsole());
        assertTrue(
                String.format(
                        "curDir!=%s", PATH_OK), curDir.compareTo(PATH_OK) == 0); //$NON-NLS-1$
    }

    /**
     * Method that performs a test to change the fake directory.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testChangeDirFail() throws Exception {
        String oldPwd = CommandHelper.getCurrentDir(getContext(), getConsole());
        try {
            CommandHelper.changeCurrentDir(getContext(), PATH_ERROR, getConsole());
            assertTrue("exit code==0", false); //$NON-NLS-1$
        } catch (NoSuchFileOrDirectory error) {
          //This command must failed. exit code !=0
        }

        //Verify that current directory is PATH_OK
        String newPwd = CommandHelper.getCurrentDir(getContext(), getConsole());
        assertTrue(
                String.format(
                        "curDir!=%s", oldPwd), newPwd.compareTo(oldPwd) == 0); //$NON-NLS-1$
    }


}
