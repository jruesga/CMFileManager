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
import com.cyanogenmod.filemanager.model.Permissions;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link ChangePermissionsCommand} command.
 *
 * @see ChangePermissionsCommand
 */
public class ChangePermissionsCommandTest extends AbstractConsoleTest {

    private static final String PATH_FILE =
            Environment.getDataDirectory().getAbsolutePath() + "/chmodtest.txt"; //$NON-NLS-1$

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
    public void testChangePermissionsOk() throws Exception {
        try {
            //Create and list the file
            CommandHelper.createFile(getContext(), PATH_FILE, getConsole());
            FileSystemObject file =
                    CommandHelper.getFileInfo(getContext(), PATH_FILE, false, getConsole());

            //Change the permissions
            Permissions oldpermissions = file.getPermissions();
            String oldOctalPermissions = oldpermissions.toOctalString();
            boolean read = oldpermissions.getUser().isRead();
            oldpermissions.getUser().setRead(!read);
            boolean ret =
                    CommandHelper.changePermissions(
                            getContext(), PATH_FILE, oldpermissions, getConsole());
            assertTrue("response==false", ret); //$NON-NLS-1$

            //List the files again
            file = CommandHelper.getFileInfo(getContext(), PATH_FILE, false, getConsole());
            Permissions newpermissions = file.getPermissions();
            String newOctalPermissions = newpermissions.toOctalString();
            assertTrue("newpermissions==oldpermissions",  //$NON-NLS-1$
                    newOctalPermissions.compareTo(oldOctalPermissions) != 0);

        } finally {
            try {
                CommandHelper.deleteFile(getContext(), PATH_FILE, getConsole());
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }

}
