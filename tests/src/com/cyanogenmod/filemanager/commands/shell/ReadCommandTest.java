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
import android.util.Log;

import com.cyanogenmod.filemanager.commands.AsyncResultExecutable;
import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing read command.
 *
 * @see ReadCommand
 */
public class ReadCommandTest extends AbstractConsoleTest {

    private static final String TAG = "ReadCommandTest"; //$NON-NLS-1$

    private static final String READ_FILE = "/boot.txt"; //$NON-NLS-1$

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
    boolean mNormalEnd;


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a read of a file
     *
     * @throws Exception If an exception occurs while executing the test
     */
    @MediumTest
    public void testReadWithPartialResult() throws Exception {
        this.mNewPartialData = false;
        this.mNormalEnd = false;
        final StringBuffer sb = new StringBuffer();
        AsyncResultExecutable cmd =
                CommandHelper.read(getContext(), READ_FILE, new AsyncResultListener() {
                        @Override
                        public void onAsyncStart() {
                            /**NON BLOCK**/
                        }
                        @Override
                        public void onAsyncEnd(boolean cancelled) {
                            synchronized (ReadCommandTest.this.mSync) {
                                ReadCommandTest.this.mNormalEnd = true;
                                ReadCommandTest.this.mSync.notify();
                            }
                        }
                        @Override
                        public void onAsyncExitCode(int exitCode) {
                            /**NON BLOCK**/
                        }
                        @Override
                        public void onException(Exception cause) {
                            fail(String.valueOf(cause));
                        }
                        @Override
                        public void onPartialResult(Object results) {
                            ReadCommandTest.this.mNewPartialData = true;
                            sb.append(new String((byte[])results));
                        }
                   }, getConsole());
        synchronized (ReadCommandTest.this.mSync) {
            ReadCommandTest.this.mSync.wait(15000L);
        }
        try {
            if (!this.mNormalEnd && cmd != null && cmd.isCancellable() && !cmd.isCancelled()) {
                cmd.cancel();
            }
        } catch (Exception e) {/**NON BLOCK**/}
        assertTrue("no new partial data", this.mNewPartialData); //$NON-NLS-1$
        assertNotNull("sb==null", sb); //$NON-NLS-1$
        Log.v(TAG, String.format("read data: %s", sb.toString())); //$NON-NLS-1$
        assertTrue("read.size > 0", sb.length() > 0); //$NON-NLS-1$
    }

}
