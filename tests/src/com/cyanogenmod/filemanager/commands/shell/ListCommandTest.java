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
import android.test.suitebuilder.annotation.SmallTest;

import com.cyanogenmod.filemanager.model.Directory;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.RegularFile;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.util.List;

/**
 * A class for testing list command.
 *
 * @see ListCommand
 */
public class ListCommandTest extends AbstractConsoleTest {

    private static final String LS_PATH = FileHelper.ROOT_DIRECTORY;
    private static final String LS_INFOFILE = "/boot.txt"; //$NON-NLS-1$
    private static final String LS_INFOFILE_NAME = "boot.txt"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test over list command setting the
     * directory to list.
     *
     * @throws Exception If test failed
     */
    @MediumTest
    public void testList() throws Exception {
        List<FileSystemObject> files = CommandHelper.listFiles(getContext(), LS_PATH, getConsole());
        assertNotNull("files==null", files); //$NON-NLS-1$
        assertTrue("no objects returned", files.size() > 0); //$NON-NLS-1$
    }

    /**
     * Method that performs a test over list command setting the
     * directory to list.
     *
     * @throws Exception If test failed
     */
    @MediumTest
    public void testListInfo() throws Exception {
        FileSystemObject file =
                CommandHelper.getFileInfo(getContext(), LS_INFOFILE, getConsole());
        assertNotNull("file==null", file); //$NON-NLS-1$
        assertTrue(
                String.format("file!=%s", LS_INFOFILE_NAME), //$NON-NLS-1$
                file.getName().compareTo(LS_INFOFILE_NAME) == 0);
    }

    /**
     * Method that performs a test over a known parse result.
     *
     * @throws Exception If test failed
     * {@link ListCommand#parse(String, String)}
     */
    @SmallTest
    public void testParse() throws Exception {
        ListCommand cmd = new ListCommand(LS_PATH, getConsole());
        String in =
            "/acct 0 0 41ed 0 0 d 1054 3 0 0 1357390899 1357390899 1357390899 4096\n" + //$NON-NLS-1$
            "/init.cm.rc 1238 8 81e8 0 0 1 370 1 0 0 1357390899 1357390899 1357390899 4096\n" + //$NON-NLS-1$
            "/vendor 14 0 a1ff 0 0 1 1052 1 0 0 1357390899 1357390899 1357390899 4096\n" + //$NON-NLS-1$
            "/cache 4096 8 41f9 1000 2001 b307 2 5 0 0 0 1357390900 1357390900 4096\n"; //$NON-NLS-1$

        String err = ""; //$NON-NLS-1$
        cmd.parse(in, err);
        List<FileSystemObject> files = cmd.getResult();
        assertNotNull("files==null", files); //$NON-NLS-1$
        assertTrue("length!=4", files.size() == 4); //$NON-NLS-1$
        assertTrue(
                "files(0) is not a directory", //$NON-NLS-1$
                files.get(0) instanceof Directory);
        assertTrue(
                "files(1) is not a file", //$NON-NLS-1$
                files.get(1) instanceof RegularFile);
        assertTrue(
                "files(2) is not a symlink", //$NON-NLS-1$
                files.get(2) instanceof Symlink);
        assertNotNull(
                "files(2) linkref is null", //$NON-NLS-1$
                ((Symlink)files.get(2)).getLinkRef());
        assertTrue(
                "files(3) != user", //$NON-NLS-1$
                files.get(3).getUser().getName().compareTo("system") == 0); //$NON-NLS-1$
        assertTrue(
                "files(3) != group", //$NON-NLS-1$
                files.get(3).getGroup().getName().compareTo("cache") == 0); //$NON-NLS-1$
        assertTrue(
                "files(1) != size", //$NON-NLS-1$
                files.get(1).getSize() == 1238);
        assertTrue(
                "files(1) != permissions",  //$NON-NLS-1$
                files.get(1).getPermissions()
                    .toRawString().compareTo("rwxr-x---") == 0); //$NON-NLS-1$
    }

}
