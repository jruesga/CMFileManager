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

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.cyanogenmod.filemanager.model.BlockDevice;
import com.cyanogenmod.filemanager.model.CharacterDevice;
import com.cyanogenmod.filemanager.model.Directory;
import com.cyanogenmod.filemanager.model.DomainSocket;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.NamedPipe;
import com.cyanogenmod.filemanager.model.RegularFile;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.FileHelper;

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
            "drwxr-xr-x root     root              2012-05-04 01:51 acct\n" + //$NON-NLS-1$
            "-rw-r--r-- root     root2         229 2012-05-04 01:51 boot.txt\n" + //$NON-NLS-1$
            "lrwxrwxrwx root     root              2012-05-04 01:51 d -> " //$NON-NLS-1$
            + "/sys/kernel/debug\n" + //$NON-NLS-1$
            "prw-r--r-- root     root            0 2012-05-04 01:51 pipe\n" + //$NON-NLS-1$
            "srw-r--r-- root     root            0 2012-05-04 01:51 socket\n" + //$NON-NLS-1$
            "brw------- root     root       7,   0 2012-05-04 01:51 loop0\n" + //$NON-NLS-1$
            "crw------- root     root       4,  64 2012-05-04 01:51 ttyS0\n" + //$NON-NLS-1$
            "-rwsr-sr-t root     root          229 2012-05-04 01:51 permission1\n" + //$NON-NLS-1$
            "-rwSr-Sr-T root     root          229 2012-05-04 01:51 permission2"; //$NON-NLS-1$
        String err = ""; //$NON-NLS-1$
        cmd.parse(in, err);
        List<FileSystemObject> files = cmd.getResult();
        assertNotNull("files==null", files); //$NON-NLS-1$
        assertTrue("length!=9", files.size() == 9); //$NON-NLS-1$
        assertTrue(
                "files(0) is not a directory", //$NON-NLS-1$
                files.get(0) instanceof Directory);
        assertTrue(
                "files(1) is not a file", //$NON-NLS-1$
                files.get(1) instanceof RegularFile);
        assertTrue(
                "files(2) is not a symlink", //$NON-NLS-1$
                files.get(2) instanceof Symlink);
        assertTrue(
                "files(3) is not a named pipe", //$NON-NLS-1$
                files.get(3) instanceof NamedPipe);
        assertTrue(
                "files(4) is not a domain socket", //$NON-NLS-1$
                files.get(4) instanceof DomainSocket);
        assertTrue(
                "files(5) is not a block device", //$NON-NLS-1$
                files.get(5) instanceof BlockDevice);
        assertTrue(
                "files(6) is not a character device", //$NON-NLS-1$
                files.get(6) instanceof CharacterDevice);
        assertTrue(
                "files(0) != name", //$NON-NLS-1$
                files.get(0).getName().compareTo("acct") == 0); //$NON-NLS-1$
        assertTrue(
                "files(2) != name", //$NON-NLS-1$
                files.get(2).getName().compareTo("d") == 0); //$NON-NLS-1$
        assertTrue(
                "files(2) != link", //$NON-NLS-1$
                ((Symlink)files.get(2)).getLink().compareTo(
                        "/sys/kernel/debug") == 0); //$NON-NLS-1$
        assertTrue(
                "files(1) != user", //$NON-NLS-1$
                files.get(1).getUser().getName().compareTo("root") == 0); //$NON-NLS-1$
        assertTrue(
                "files(1) != group", //$NON-NLS-1$
                files.get(1).getGroup().getName().compareTo("root2") == 0); //$NON-NLS-1$
        assertTrue(
                "files(1) != size", //$NON-NLS-1$
                files.get(1).getSize() == 229);
        assertTrue(
                "files(1) != permissions",  //$NON-NLS-1$
                files.get(1).getPermissions()
                    .toRawString().compareTo("rw-r--r--") == 0); //$NON-NLS-1$
        assertTrue(
                "files(7) != setuid", //$NON-NLS-1$
                files.get(7).getPermissions().getUser().isSetUID());
        assertTrue(
                "files(7) != setgid", //$NON-NLS-1$
                files.get(7).getPermissions().getGroup().isSetGID());
        assertTrue(
                "files(7) != stickybit", //$NON-NLS-1$
                files.get(7).getPermissions().getOthers().isStickybit());
        assertTrue(
                "files(7) != setuid+execute", //$NON-NLS-1$
                files.get(7).getPermissions().getUser().isExecute());
        assertTrue(
                "files(7) != setgid+execute", //$NON-NLS-1$
                files.get(7).getPermissions().getGroup().isExecute());
        assertTrue(
                "files(7) != stickybit+execute", //$NON-NLS-1$
                files.get(7).getPermissions().getOthers().isExecute());
        assertTrue(
                "files(8) != setuid", //$NON-NLS-1$
                files.get(8).getPermissions().getUser().isSetUID());
        assertTrue(
                "files(8) != setgid", //$NON-NLS-1$
                files.get(8).getPermissions().getGroup().isSetGID());
        assertTrue(
                "files(8) != stickybit", //$NON-NLS-1$
                files.get(8).getPermissions().getOthers().isStickybit());
        assertTrue(
                "files(8) != setuid+execute", //$NON-NLS-1$
                !files.get(8).getPermissions().getUser().isExecute());
        assertTrue(
                "files(8) != setgid+execute", //$NON-NLS-1$
                !files.get(8).getPermissions().getGroup().isExecute());
        assertTrue(
                "files(8) != stickybit+execute", //$NON-NLS-1$
                !files.get(8).getPermissions().getOthers().isExecute());
    }

}
