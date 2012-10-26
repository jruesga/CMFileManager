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

import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.model.User;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link ChangeOwnerCommand} command.
 *
 * @see ChangeOwnerCommand
 */
public class ChangeOwnerCommandTest extends AbstractConsoleTest {

    private static final String PATH_FILE =
            Environment.getDataDirectory().getAbsolutePath() + "/chowntest.txt"; //$NON-NLS-1$
    private static final String NEW_GROUP = "graphics"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a test to change permissions of a file.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testChangeOwnerOk() throws Exception {
        try {
            //Create and list the file
            CommandHelper.createFile(getContext(), PATH_FILE, getConsole());
            FileSystemObject file =
                    CommandHelper.getFileInfo(getContext(), PATH_FILE, false, getConsole());

            //Change the permissions
            User oldUser = file.getUser();
            Group newGroup = new Group(-1, NEW_GROUP);
            boolean ret =
                    CommandHelper.changeOwner(
                            getContext(), PATH_FILE, oldUser, newGroup, getConsole());
            assertTrue("response==false", ret); //$NON-NLS-1$

            //List the file again
            file = CommandHelper.getFileInfo(getContext(), PATH_FILE, false, getConsole());
            Group lsGroup = file.getGroup();
            assertTrue("set group!=list group",  //$NON-NLS-1$
                    newGroup.getName().compareTo(lsGroup.getName()) == 0);

        } finally {
            try {
                CommandHelper.deleteFile(getContext(), PATH_FILE, getConsole());
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }

}
