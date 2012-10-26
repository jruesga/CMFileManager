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

import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the {@link CurrentDirCommand} command.
 *
 * @see CurrentDirCommand
 */
public class CurrentDirCommandTest extends AbstractConsoleTest {

    private static final String PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test for retrieve current directory.
     *
     * @throws Exception If test failed
     */
    @SmallTest
    public void testCurrentDir() throws Exception {
        CommandHelper.changeCurrentDir(getContext(), PATH, getConsole());
        String curDir = CommandHelper.getCurrentDir(getContext(), getConsole());
        assertTrue(
                String.format(
                        "current directory!=%s; %s", PATH, curDir),  //$NON-NLS-1$
                        curDir.compareTo(PATH) == 0);
    }


}
