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

import java.util.List;

import android.test.suitebuilder.annotation.SmallTest;

import com.cyanogenmod.filemanager.model.DiskUsage;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link DiskUsageCommand} command.
 *
 * @see DiskUsageCommand
 */
public class DiskUsageCommandTest extends AbstractConsoleTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test over the recovery of disk usage.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testDiskUsage() throws Exception {
        List<DiskUsage> du = CommandHelper.getDiskUsage(getContext(), getConsole());
        assertNotNull("diskusage==null", du); //$NON-NLS-1$
        assertTrue("no objects returned", du.size() > 0); //$NON-NLS-1$
    }

    /**
     * Method that performs a test over a known parse result.
     *
     * @throws Exception If test failed
     * {@link ListCommand#parse(String, String)}
     */
    @SuppressWarnings("static-method")
    @SmallTest
    public void testParse() throws Exception {
        DiskUsageCommand cmd = new DiskUsageCommand();
        String in = "Filesystem             Size   Used   Free   Blksize\n" + //$NON-NLS-1$
                    "/dev                   414M    48K   414M   4096\n" + //$NON-NLS-1$
                    "/mnt/asec              412M    0K    412M   4096\n" + //$NON-NLS-1$
                    "/mnt/secure/asec: Permission denied"; //$NON-NLS-1$
        String err = ""; //$NON-NLS-1$
        cmd.parse(in, err);
        List<DiskUsage> du = cmd.getResult();
        assertNotNull("diskusage==null", du); //$NON-NLS-1$
        assertTrue("length!=2", du.size() == 2); //$NON-NLS-1$
        assertTrue(
                "diskusage(1).mountpoint!=/mnt/asec", //$NON-NLS-1$
                du.get(1).getMountPoint().compareTo("/mnt/asec") == 0); //$NON-NLS-1$
        assertTrue("diskusage(0).total!=414M", du.get(0).getTotal() == 434110464L); //$NON-NLS-1$
        assertTrue("diskusage(1).used!=0M", du.get(1).getUsed() == 0L); //$NON-NLS-1$
        assertTrue("diskusage(1).free!=412M", du.get(1).getFree() == 432013312L); //$NON-NLS-1$
    }


}
