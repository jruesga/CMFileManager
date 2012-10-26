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

import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link GroupsCommand} command.
 *
 * @see GroupsCommand
 */
public class GroupsCommandTest extends AbstractConsoleTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test over groups command.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testGroups() throws Exception {
        List<Group> groups = CommandHelper.getGroups(getContext(), getConsole());
        assertNotNull("groups==null", groups); //$NON-NLS-1$
        assertTrue("size==0", groups.size() > 0); //$NON-NLS-1$
    }

    /**
     * Method that performs a test over a known parse result.
     *
     * @throws Exception If test failed
     * {@link GroupsCommand#parse(String, String)}
     */
    @SuppressWarnings("static-method")
    @SmallTest
    public void testParse() throws Exception {
        GroupsCommand cmd = new GroupsCommand();
        String in =
                "shell graphics input log mount adb sdcard_rw " //$NON-NLS-1$
                + "net_bt_admin net_bt inet net_bw_stats"; //$NON-NLS-1$
        String err = ""; //$NON-NLS-1$
        cmd.parse(in, err);
        List<Group> groups = cmd.getResult();
        assertNotNull("groups==null", groups); //$NON-NLS-1$
        assertTrue("groups length!=11", groups.size() == 11); //$NON-NLS-1$
        assertTrue(
                "groups(1)!=name",  //$NON-NLS-1$
                groups.get(1).getName().compareTo("graphics") == 0); //$NON-NLS-1$
    }


}
