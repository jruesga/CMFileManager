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

package com.cyanogenmod.explorer.commands.shell;

import android.util.Log;

import com.cyanogenmod.explorer.commands.AsyncResultListener;
import com.cyanogenmod.explorer.model.FolderUsage;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.MimeTypeHelper.MimeTypeCategory;

/**
 * A class for testing folder usage command.
 *
 * @see FindCommand
 */
public class FolderUsageCommandTest extends AbstractConsoleTest {

    private static final String TAG = "FolderUsageCommandTest"; //$NON-NLS-1$

    private static final String PATH = "/system"; //$NON-NLS-1$

    /**
     * @hide
     */
    final Object mSync = new Object();
    /**
     * @hide
     */
    boolean mNewPartialData;
    /**
     * @hide
     */
    FolderUsage mUsage;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return false;
    }

    /**
     * Method that performs a test over known search results.
     *
     * @throws Exception If test failed
     */
    public void testFolderUsageWithPartialResult() throws Exception {
        this.mNewPartialData = false;
        this.mUsage = null;
        CommandHelper.getFolderUsage(getContext(), PATH, new AsyncResultListener() {
                        public void onAsyncStart() {
                            /**NON BLOCK**/
                        }
                        public void onAsyncEnd(boolean cancelled) {
                            synchronized (FolderUsageCommandTest.this.mSync) {
                                FolderUsageCommandTest.this.mSync.notifyAll();
                            }
                        }
                        public void onException(Exception cause) {
                            fail(cause.toString());
                        }
                        public void onPartialResult(Object result) {
                            FolderUsageCommandTest.this.mNewPartialData = true;
                            try {
                                FolderUsageCommandTest.this.mUsage =
                                        (FolderUsage)(((FolderUsage)result).clone());
                            } catch (Exception e) {/**NON BLOCK**/}
                            Log.d(TAG, FolderUsageCommandTest.this.mUsage.toString());
                        }
                   }, getConsole());
        synchronized (FolderUsageCommandTest.this.mSync) {
            FolderUsageCommandTest.this.mSync.wait(25000L);
        }
        assertTrue("no new partial data", this.mNewPartialData); //$NON-NLS-1$
        assertNotNull("usage==null", this.mUsage); //$NON-NLS-1$
        assertTrue("no folder returned", this.mUsage.getNumberOfFolders() > 0); //$NON-NLS-1$
        assertTrue("no files returned", this.mUsage.getNumberOfFiles() > 0); //$NON-NLS-1$
        assertTrue("no size returned", this.mUsage.getTotalSize() > 0); //$NON-NLS-1$
        assertTrue("no text category returned", //$NON-NLS-1$
                this.mUsage.getStatisticsForCategory(MimeTypeCategory.TEXT) > 0);
    }

}
