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

import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link MountPointInfoCommand} command.
 *
 * @see MountPointInfoCommand
 */
public class MountPointInfoCommandTest extends AbstractConsoleTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test over the recovery of the mount points.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testMountPoint() throws Exception {
        List<MountPoint> mp = CommandHelper.getMountPoints(getContext(), getConsole());
        assertNotNull("mountpoints==null", mp); //$NON-NLS-1$
        assertTrue("no objects returned", mp.size() > 0); //$NON-NLS-1$
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
        MountPointInfoCommand cmd = new MountPointInfoCommand();
        String in = "rootfs / rootfs ro,relatime 0 0\n" + //$NON-NLS-1$
                    "tmpfs /dev tmpfs rw,nosuid,relatime,mode=755 0 0\n" + //$NON-NLS-1$
                    "devpts /dev/pts devpts rw,relatime,mode=600 0 0\n" + //$NON-NLS-1$
                    "/dev/block/vold/179:25 /mnt/emmc vfat rw,dirsync,nosuid,nodev," //$NON-NLS-1$
                    + "noexec,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702," //$NON-NLS-1$
                    + "allow_utime=0020,codepage=cp437,iocharset=iso8859-1," //$NON-NLS-1$
                    + "shortname=mixed,utf8,errors=remount-ro 0 0"; //$NON-NLS-1$
        String err = ""; //$NON-NLS-1$
        cmd.parse(in, err);
        List<MountPoint> mp = cmd.getResult();
        assertNotNull("mountpoints==null", mp); //$NON-NLS-1$
        assertTrue("length!=4", mp.size() == 4); //$NON-NLS-1$
        assertTrue(
                "mountpoints(1).device!=tmpfs", //$NON-NLS-1$
                mp.get(1).getDevice().compareTo("tmpfs") == 0); //$NON-NLS-1$
        assertTrue(
                "mountpoints(2).mountpoint!=/dev/pts",  //$NON-NLS-1$
                mp.get(2).getMountPoint().compareTo("/dev/pts") == 0); //$NON-NLS-1$
        assertTrue(
                "mountpoints(3).type!=vfat",  //$NON-NLS-1$
                mp.get(3).getType().compareTo("vfat") == 0); //$NON-NLS-1$
        assertTrue(
                "mountpoints(0).options!=ro,relatime",  //$NON-NLS-1$
                mp.get(0).getOptions().compareTo("ro,relatime") == 0); //$NON-NLS-1$
        assertTrue(
                "mountpoints(0).dump!=0", //$NON-NLS-1$
                mp.get(0).getDump() == 0);
        assertTrue(
                "mountpoints(0).pass!=0", //$NON-NLS-1$
                mp.get(0).getPass() == 0);
    }


}
