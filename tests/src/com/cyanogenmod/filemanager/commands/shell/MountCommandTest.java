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

import android.test.suitebuilder.annotation.MediumTest;

import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.MountPointHelper;

/**
 * A class for testing the {@link MountCommand} command.
 *
 * @see MountCommand
 */
public class MountCommandTest extends AbstractConsoleTest {

    private static final String MOUNT_POINT_DIR = "/efs"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a test to mount a filesystem.
     *
     * @throws Exception If test failed
     */
    @MediumTest
    public void testRemountOk() throws Exception {
        MountPoint mp = MountPointHelper.getMountPointFromDirectory(getConsole(), MOUNT_POINT_DIR);
        boolean rw = MountPointHelper.isReadWrite(mp);
        boolean ret = CommandHelper.remount(getContext(), mp, !rw, getConsole());
        MountPoint mp2 = MountPointHelper.getMountPointFromDirectory(getConsole(), MOUNT_POINT_DIR);
        boolean rw2 = MountPointHelper.isReadWrite(mp2);

        assertTrue("response==false", ret); //$NON-NLS-1$
        assertTrue(
                String.format("remount failed: expected: %s, obtain: %s", //$NON-NLS-1$
                        rw ? "rw" : "ro",  //$NON-NLS-1$ //$NON-NLS-2$
                        rw2 ? "rw " : "ro"), //$NON-NLS-1$ //$NON-NLS-2$
                rw != rw2);
    }


}
