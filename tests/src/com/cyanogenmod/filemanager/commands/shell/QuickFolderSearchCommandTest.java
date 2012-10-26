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

import android.os.Environment;
import android.test.suitebuilder.annotation.LargeTest;

import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the method for retrieve the value of a variable.
 *
 * @see QuickFolderSearchCommand
 */
public class QuickFolderSearchCommandTest extends AbstractConsoleTest {

    private static final String SEARCH_EXPRESSION =
            Environment.getDataDirectory().getAbsolutePath() + "/dal"; //$NON-NLS-1$
    private static final String SEARCH_CONTAINS =
            Environment.getDataDirectory().getAbsolutePath() + "/dalvik-cache/"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a test over known search results.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    public void testQuickFolderSearchOk() throws Exception {
        List<String> result =
                CommandHelper.quickFolderSearch(getContext(), SEARCH_EXPRESSION, getConsole());
        assertNotNull("result==null", result); //$NON-NLS-1$
        assertTrue("result.size==0", result.size() != 0); //$NON-NLS-1$
        assertTrue(
                String.format("result not contains %s", SEARCH_CONTAINS), //$NON-NLS-1$
                result.contains(SEARCH_CONTAINS));
    }

}
