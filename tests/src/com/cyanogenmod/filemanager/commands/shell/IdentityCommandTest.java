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

import com.cyanogenmod.filemanager.model.Identity;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link IdentityCommand} command.
 *
 * @see IdentityCommand
 */
public class IdentityCommandTest extends AbstractConsoleTest {

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
    @SmallTest
    public void testId() throws Exception {
        Identity identity = CommandHelper.getIdentity(getContext(), getConsole());
        assertNotNull("identity==null", identity); //$NON-NLS-1$
        assertNotNull("user==null", identity.getUser()); //$NON-NLS-1$
        assertTrue("user!=-1", identity.getUser().getId() != -1); //$NON-NLS-1$
        assertNotNull("group==null", identity.getGroup()); //$NON-NLS-1$
        assertTrue("group!=-1", identity.getGroup().getId() != -1); //$NON-NLS-1$
    }

    /**
     * Method that performs a test over a known parse result.
     *
     * @throws Exception If test failed
     * {@link IdentityCommand#parse(String, String)}
     */
    @SuppressWarnings("static-method")
    @SmallTest
    public void testParse() throws Exception {
        IdentityCommand cmd = new IdentityCommand();
        String in = "uid=2000(shell) gid=2000(shell2) groups=1003(graphics)," //$NON-NLS-1$
                    + "1004(input),1007(log),1009(mount)," +  //$NON-NLS-1$
                    "1011(adb),1015(sdcard_rw),3001(net_bt_admin),3002(net_bt)," //$NON-NLS-1$
                    + "3003(inet),3006(net_bw_stats)"; //$NON-NLS-1$
        String err = ""; //$NON-NLS-1$
        cmd.parse(in, err);
        Identity identity = cmd.getResult();
        assertNotNull("identity==null", identity); //$NON-NLS-1$
        assertNotNull("user==null", identity.getUser()); //$NON-NLS-1$
        assertNotNull("group==null", identity.getGroup()); //$NON-NLS-1$
        assertNotNull("groups==null", identity.getGroups()); //$NON-NLS-1$
        assertTrue("user uid!=2000", identity.getUser().getId() == 2000); //$NON-NLS-1$
        assertTrue(
                "user name!=shell", //$NON-NLS-1$
                identity.getUser().getName().compareTo("shell") == 0); //$NON-NLS-1$
        assertTrue("group gid!=2000", identity.getGroup().getId() == 2000); //$NON-NLS-1$
        assertTrue(
                "group name!=shell", //$NON-NLS-1$
                identity.getGroup().getName().compareTo("shell2") == 0); //$NON-NLS-1$
        assertTrue("groups length!=10", identity.getGroups().size() == 10); //$NON-NLS-1$
    }


}
